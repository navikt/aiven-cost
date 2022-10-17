package io.nais.cost.aiven

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private val map = buildBillinggroupToTenantMap()

    private fun buildBillinggroupToTenantMap(): Map<String,String> {
        val body = callAiven("v1/project")
        val list = JsonPath.parse(body)?.read<List<Map<String, Any>>>("$.projects[*]").orEmpty()
        val billingGroupToProjectMap = list.map { it["billing_group_id"] to it["project_name"] }.toMap()
        billingGroupToProjectMap.map { it.key to callAiven(it.value) }

    }

    private companion object {
        private val log = LoggerFactory.getLogger(Aiven::class.java)
    }

    private val client = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build()

    fun getInvoiceData() = getBillingGroups().flatMap { billingGroupId ->
        getInvoices(billingGroupId).flatMap { invoiceId -> getInvoiceLines(billingGroupId, invoiceId) }
    }


    private fun getInvoiceLines(billingGroupdId: String, invoiceId: String): List<InvoiceLine> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoiceId/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, Any>>>("$.lines[*]").orEmpty()
        val tenant = getTenant(billingGroupdId)
        val invoiceLines = list.map { InvoiceLine(invoiceId, tenant, it) }.toList()
        invoiceLines.forEach { line -> log.info("Invoiceline ${line.invoiceId} to ${line.endTimestamp}") }
        return invoiceLines
    }

    private fun getInvoices(billingGroupdId: String): List<String> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice")
        val invoices: List<String>? = JsonPath.parse(body)?.read("$.invoices[*].invoice_number")
        log.info("fetched invoices, got ${invoices?.size} ")
        return invoices.orEmpty()
    }

    private fun getBillingGroups(): List<String> = callAiven("/v1/billing-group").getBillingGroupIds().orEmpty()

    fun String.getBillingGroupIds(): List<String>? {
        return JsonPath.parse(this)?.read("$.billing_groups[*].billing_group_id")
    }

    fun getTenant(billingGroupdId: String) = map[billingGroupdId].orEmpty()

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
