package io.nais.cost.aiven

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private companion object {
        private val log = LoggerFactory.getLogger(Aiven::class.java)
    }

    private val client = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build()

    fun getInvoiceData(): List<InvoiceLine> =
        getBillingGroup()?.let {
            getInvoices(it)?.let { invoices -> flattenToInvoiceLines(invoices, it) }
        }.orEmpty()

    private fun flattenToInvoiceLines(invoices: List<String>, billingGroupdId: String): List<InvoiceLine> =
        invoices.flatMap { invoiceId -> getInvoiceLines(billingGroupdId, invoiceId) }

    private fun getInvoiceLines(billingGroupdId: String, invoiceId: String): List<InvoiceLine> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoiceId/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, Any>>>("$.lines[*]").orEmpty()
        val invoiceLines = list.map { InvoiceLine(invoiceId, it) }.toList()
        invoiceLines.forEach { line -> log.info("Invoiceline ${line.invoiceId} to ${line.endTimestamp}") }
        return invoiceLines
    }

    private fun getInvoices(billingGroupdId: String): List<String>? {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice")
        val invoices: List<String>? = JsonPath.parse(body)?.read("$.invoices[*].invoice_number")
        log.info("fetched invoices, got ${invoices?.size} ")
        return invoices
    }

    private fun getBillingGroup(): String? {
        val body = callAiven("/v1/billing-group")
        return JsonPath.parse(body)?.read("$.billing_groups[0].billing_group_id")
    }


    private fun callAiven(aivenApiUrl: String): String {
        return client.newCall(
            Request.Builder()
                .url("$hostAndPort$aivenApiUrl")
                .addHeader("authorization", "aivenv1 $token")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful || response.body == null) throw IOException("Unexpected code $response")
            response.body!!.string()
        }
    }
}
