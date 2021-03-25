package aiven.cost

import java.time.LocalDateTime

data class InvoiceLine(val input: Map<String, String>) {
    fun getProjectName() = input.get("project_name")
    fun getLineTotal() = input.get("line_total_usd")?.toDouble()
    fun getLineType() = input.get("line_type")
    fun getServiceName() = input.get("service_name")
    fun getServiceType() = input.get("service_type")
    fun getBeginTimestamp() = input.get("timestamp_begin")
    fun getEndTimestamp() = input.get("timestamp_end")
}

