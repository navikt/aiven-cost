package aiven.cost

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class BillingGroups(
    val billingGroupId: String
)

data class Invoice(
    val invoiceNumber: String
)

fun parseBillingGroup (billingGroupResponse: String): List<BillingGroups>
    = jacksonObjectMapper().readValue(billingGroupResponse)

fun parseInvoice (invoiceResponse: String): List<Invoice>
        = jacksonObjectMapper().readValue(invoiceResponse)
