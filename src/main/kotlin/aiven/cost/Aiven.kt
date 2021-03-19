package aiven.cost

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class Aiven(val token: String) {

    private val client = OkHttpClient()

    fun getInvoiceData(): String {


        val request = Request.Builder()
            .url("https://api.aiven.io/v1/billing-group")
            .addHeader("authorization", "aivenv1 $token")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }

            val response = response.body!!.string()
            println(response)
            return response

        }

    }
}