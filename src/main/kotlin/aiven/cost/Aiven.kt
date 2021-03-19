package aiven.cost

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class Aiven(val token: String) {

    private val client = OkHttpClient()

    fun getInvoiceData(): String {

        val billingGroupdId = getBillingGroup()


        val invoices = when {
            billingGroupdId.isEmpty() -> emptyList<String>()
            else -> getInvoices(billingGroupdId)
        }
        println(invoices)
        getInvoiceLines(invoices, billingGroupdId)
        return ""
    }

    private fun getInvoiceLines(invoices: List<String>, billingGroupdId: String) {
        val invoiceMap = invoices.map { invoice_id ->
            invoice_id to
                    client.newCall(
                        Request.Builder()
                            .url("https://api.aiven.io/v1/billing-group/$billingGroupdId/invoice/$invoice_id/lines")
                            .addHeader("authorization", "aivenv1 $token")
                            .build()
                    ).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        response.body!!.string()
                    }
        }.toMap()
    }

    private fun getInvoices(billingGroupdId: String): List<String> {
        client.newCall(
            Request.Builder()
                .url("https://api.aiven.io/v1/billing-group/$billingGroupdId/invoice")
                .addHeader("authorization", "aivenv1 $token")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body!!.string()
            return JsonPath.parse(body)?.read<List<String>>("$.invoices[*].invoice_number").orEmpty()
        }
    }

    private fun getBillingGroup(): String {
        client.newCall(
            Request.Builder()
                .url("https://api.aiven.io/v1/billing-group")
                .addHeader("authorization", "aivenv1 $token")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body!!.string()
            return JsonPath.parse(body)?.read<String>("$.billing_groups[0].billing_group_id").orEmpty()
        }
    }
}
