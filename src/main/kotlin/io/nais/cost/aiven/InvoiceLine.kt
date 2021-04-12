package io.nais.cost.aiven

data class InvoiceLine(
    val invoiceId: String,
    private val input: Map<String, String>
) {
    fun getProjectName() = input["project_name"].orEmpty()
    fun getLineTotal() = input["line_total_usd"].orEmpty()
    fun getLineType() = input["line_type"].orEmpty()
    fun getServiceName() = input["service_name"].orEmpty()
    fun getServiceType() = input["service_type"].orEmpty()
    fun getBeginTimestamp() = input["timestamp_begin"].orEmpty()
    fun getEndTimestamp() = input["timestamp_end"].orEmpty()
}

