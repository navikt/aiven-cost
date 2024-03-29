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

    private val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()

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
        val projects = callAivenWithJsonPath<List<Map<String, Any>>>("/v1/project", "$.projects[*]").orEmpty()
        val billingGroupToProjectPairs =
            projects.map { it["billing_group_id"] as String to it["project_name"] as String }
        log.info("Billing group to project pair:")
        log.info(billingGroupToProjectPairs.joinToString { "${it.first}:${it.second}\n" })
        val billingGroupTenantPairs = billingGroupToProjectPairs.map { it.first to getTenantFromProjectName(it.second) }
        log.info("Billing group to tenant pairs:")
        log.info(billingGroupTenantPairs.joinToString { "${it.first}:${it.second}\n" })
        val billingGroupTenantMap = billingGroupTenantPairs.filterNot{ it.second.isBlank() }.toMap()
        log.info("Built billinggroup to tenant map with ${billingGroupTenantMap.entries.size} entries")
        log.info(billingGroupTenantMap.entries.joinToString { "${it.key}: ${it.value}\n" })
        return billingGroupTenantMap
    }

    private fun getTenantFromProjectName(projectName: String?): String {
        val tenantList = callAivenWithJsonPath<List<String>>(
            "/v1/project/${projectName}/service",
            "$.services[*].tags.tenant"
        )
        val tenant = tenantList?.filterNot { it.isEmpty()}?.firstOrNull()
        tenant ?: log.warn("Tenant for $projectName is empty")
        return tenant.orEmpty()
    }


    fun getInvoiceDataWithoutTenants() = getBillingGroups().flatMap { billingGroupId ->
        getInvoices(billingGroupId).flatMap { invoiceId -> toInvoiceLines(billingGroupId, "", invoiceId) }
    }

    private fun toInvoiceLines(billingGroupdId: String, tenant: String, invoiceId: String): List<InvoiceLine> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice/$invoiceId/lines")
        val list = JsonPath.parse(body)?.read<List<Map<String, Any>>>("$.lines[*]").orEmpty()
        if (tenant.isBlank()) log.warn("Empty tenant for invoiceLine with invoiceId $invoiceId, billing group $billingGroupdId")
        return list.map { InvoiceLine(invoiceId = invoiceId, tenant = tenant, input = it) }.toList()
    }

    private fun getInvoices(billingGroupdId: String): List<String> {
        val body = callAiven("/v1/billing-group/$billingGroupdId/invoice")
        val invoices: List<String>? = JsonPath.parse(body)?.read("$.invoices[*].invoice_number")
        log.info("fetched invoices, got ${invoices?.size} ")
        return invoices.orEmpty()
    }

    private fun getBillingGroups(): List<String> = callAiven("/v1/billing-group")?.getBillingGroupIds().orEmpty()

    fun String.getBillingGroupIds(): List<String>? {
        return JsonPath.parse(this)?.read("$.billing_groups[*].billing_group_id")
    }

    inline fun <reified T : Any> callAivenWithJsonPath(aivenApiUrl: String, jsonPath: String): T? {
        val x = callAiven(aivenApiUrl)
        return JsonPath.parse(x)!!.read(jsonPath)
    }


    fun callAiven(aivenApiUrl: String): String? {
        val request = Request.Builder()
            .url("$hostAndPort$aivenApiUrl")
            .addHeader("authorization", "aivenv1 $token")
            .build()
        return client.newCall(request).execute().use { response ->
            if (response.code == 403) {
                log.error(response.message)
                return null
            }
            if (!response.isSuccessful || response.body == null) {
                throw IOException("Unexpected code $response")
            }

            val json = response.body!!.string()
            log.info("called aiven at $hostAndPort$aivenApiUrl, got ${json.length} length response.")
            json

        }
    }
}
