package aiven.cost

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private val client = OkHttpClient.Builder().callTimeout(5, TimeUnit.SECONDS).build()

    fun getInvoiceData(): Map<String, List<Line>> {

        val billingGroup = getBillingGroup()

        val invoices = getInvoices(billingGroup)

        return getInvoiceLines(invoices, billingGroup)
    }

    private fun getInvoiceLines(invoices: List<Invoice>, billingGroups: BillingGroups): Map<String, List<Line>> {
        return invoices.map { invoice ->
            invoice.invoiceNumber to
                    client.newCall(
                        Request.Builder()
                            .url("$hostAndPort/v1/billing-group/${billingGroups.billingGroupId}/invoice/${invoice.invoiceNumber}/lines")
                            .addHeader("authorization", "aivenv1 $token")
                            .build()
                    ).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val body = response.body!!.string()
                        parseInvoiceLines(body)
                    }
        }.toMap()
    }

    private fun getInvoices(billingGroups: BillingGroups): List<Invoice> {
        client.newCall(
            Request.Builder()
                .url("$hostAndPort/v1/billing-group/${billingGroups.billingGroupId}/invoice")
                .addHeader("authorization", "aivenv1 $token")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body!!.string()
            return parseInvoice(body)
        }
    }

    private fun getBillingGroup(): BillingGroups {
        client.newCall(
            Request.Builder()
                .url("$hostAndPort/v1/billing-group")
                .addHeader("authorization", "aivenv1 $token")
                .build()
        ).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val body = response.body!!.string()
            return parseBillingGroup(body).getOrElse(0){
                throw IllegalArgumentException("billing group empty")
            }
        }
    }
}
