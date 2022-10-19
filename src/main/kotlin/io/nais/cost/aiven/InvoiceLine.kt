package io.nais.cost.aiven

data class InvoiceLine(
    val invoiceId: String,
    val tenant: String,
    private val input: Map<String, Any>
) {
    val projectName = "project_name".mapValueAsString()
    val lineTotal = "line_total_usd".mapValueAsString()
    val lineType = "line_type".mapValueAsString()
    val serviceName = "service_name".mapValueAsString()
    val serviceType = "service_type".mapValueAsString()
    val beginTimestamp = "timestamp_begin".mapValueAsString()
    val endTimestamp = "timestamp_end".mapValueAsString()

    private fun String.mapValueAsString() =
        if (input.containsKey(this)) input[this] as String else ""
}


