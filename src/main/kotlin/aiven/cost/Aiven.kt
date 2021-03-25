package aiven.cost

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private val client = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build()

    fun getInvoiceData(): Map<String, List<InvoiceLine>> =
        getBillingGroup()?.let {
            getInvoices(it)?.let { invoices -> toInvoiceMap(invoices, it) }
        }.orEmpty()

    private fun toInvoiceMap(invoices: List<String>, billingGroupdId: String): Map<String, List<InvoiceLine>> =
        invoices.map { invoice_id -> invoice_id to getInvoiceLines(billingGroupdId, invoice_id) }.toMap()

    private fun getInvoiceLines(billingGroupdId: String, invoice_id: String): List<InvoiceLine> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoice_id/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, String>>>("$.lines[*]").orEmpty()
        return list.map { InvoiceLine(it) }.toList()
    }

    private fun getInvoices(billingGroupdId: String): List<String>? {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice")
        return JsonPath.parse(body)?.read("$.invoices[*].invoice_number")
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
