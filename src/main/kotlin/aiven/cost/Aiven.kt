package aiven.cost

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private val client = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build()

    fun getInvoiceData(): Map<String, List<InvoiceLine>?> {

        val billingGroupdId = getBillingGroup()

        val invoices = when {
            billingGroupdId.isEmpty() -> emptyList()
            else -> getInvoices(billingGroupdId)
        }
        return toInvoiceMap(invoices, billingGroupdId)
    }

    private fun toInvoiceMap(invoices: List<String>, billingGroupdId: String): Map<String, List<InvoiceLine>?> {
        return invoices.map { invoice_id -> invoice_id to getInvoiceLines(billingGroupdId, invoice_id) }.toMap()
    }

    private fun getInvoiceLines(
        billingGroupdId: String,
        invoice_id: String
    ): List<InvoiceLine>? {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoice_id/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, String>>>("$.lines[*]")
        return list?.map { InvoiceLine(it) }?.toList()
    }

    private fun getInvoices(billingGroupdId: String): List<String> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice")
        return JsonPath.parse(body)?.read<List<String>>("$.invoices[*].invoice_number").orEmpty()
    }

    private fun getBillingGroup(): String {
        val body = callAiven("/v1/billing-group")
        return JsonPath.parse(body)?.read<String>("$.billing_groups[0].billing_group_id").orEmpty()
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
