package io.nais.cost.aiven

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class Aiven(val token: String, val hostAndPort: String = "https://api.aiven.io") {

    private val billingGroupToTenantMap = buildBillinggroupToTenantMap()

    private fun buildBillinggroupToTenantMap(): Map<String, String> {
        val projects = callAivenWithJsonPath<List<Map<String, String>>>("v1/project", "$.projects[*]").orEmpty()
        val bg2ProjectMap = projects.associate { it["billing_group_id"] to it["project_name"] }
        val bg2ServiceMap =
            bg2ProjectMap.map { it.key to (it.value to getServiceNameFromProjectName(it.value)) }.toMap()
        return bg2ServiceMap.map {
            it.key.orEmpty() to getTenantNameFromService(it.value.first, it.value.second)
        }.toMap()
    }

    private fun getServiceNameFromProjectName(projectName: String?) =
        callAivenWithJsonPath<String>("v1/project/${projectName}/service", "$.services[*].service_name")

    private fun getTenantNameFromService(projectName: String?, serviceName: String?) =
        callAivenWithJsonPath<String>(
            "v1/project/${projectName}/service/${serviceName}/tags",
            "$.tags[*].tenant"
        ).orEmpty()


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
        val tenant = billingGroupToTenantMap[billingGroupdId].orEmpty()
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

    inline fun <reified T : Any> callAivenWithJsonPath(aivenApiUrl: String, jsonPath: String): T? =
        JsonPath.parse(callAiven(aivenApiUrl))?.read(jsonPath)


    fun callAiven(aivenApiUrl: String): String {
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
