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

    fun getInvoiceData() = buildBillinggroupToTenantMap().flatMap { (billingGroupId, tenant) ->
        getInvoices(billingGroupId).flatMap { invoiceId ->
            toInvoiceLines(
                billingGroupdId = billingGroupId,
                tenant = tenant,
                invoiceId = invoiceId
            )
        }
    }

    private fun buildBillinggroupToTenantMap(): Map<String, String> {
        val projects = callAivenWithJsonPath<List<Map<String, String>>>("/v1/project", "$.projects[*]").orEmpty()
        log.info("Fetched projects list with  ${projects.size} items")
        val billingGroupToProjectMap = projects.associate { it["billing_group_id"] to it["project_name"] }
        log.info("Built billing group to project map with  ${billingGroupToProjectMap.entries.size} items")
        val billingGroupTenantMap =
            billingGroupToProjectMap.map { it.key!! to getTenantFromProjectName(it.value) }.toMap()
        log.info("Built billinggroup to tenant map with ${billingGroupTenantMap.entries.size} entries")
        return billingGroupTenantMap

    }

    private fun getTenantFromProjectName(projectName: String?): String {
        return callAivenWithJsonPath<String>(
            "/v1/project/${projectName}/service",
            "$.services[0].tags.tenant"
        ).orEmpty()
    }


    fun getInvoiceDataWithoutTenants() = getBillingGroups().flatMap { billingGroupId ->
        getInvoices(billingGroupId).flatMap { invoiceId -> toInvoiceLines(billingGroupId, "", invoiceId) }
    }

    private fun toInvoiceLines(billingGroupdId: String, tenant: String, invoiceId: String): List<InvoiceLine> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoiceId/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, Any>>>("$.lines[*]").orEmpty()
        val invoiceLines = list.map { InvoiceLine(invoiceId = invoiceId, tenant = tenant, input = it) }.toList()
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

    inline fun <reified T : Any> callAivenWithJsonPath(aivenApiUrl: String, jsonPath: String): T? {
        val x = callAiven(aivenApiUrl)
        return JsonPath.parse(x)?.read(jsonPath)
    }


    fun callAiven(aivenApiUrl: String): String {
        val request = Request.Builder()
            .url("$hostAndPort$aivenApiUrl")
            .addHeader("authorization", "aivenv1 $token")
            .build()
        log.info(request.toString())
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful || response.body == null) throw IOException("Unexpected code $response")
            val json = response.body!!.string()
            log.info("called aiven at $hostAndPort$aivenApiUrl, got ${json.length} length response.")
            json

        }
    }
}
