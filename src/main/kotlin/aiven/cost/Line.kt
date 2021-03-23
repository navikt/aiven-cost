package aiven.cost

import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue


data class Line (
    private val timestamp_begin: LocalDateTime,
    private val timestamp_end: LocalDateTime,
    private val line_total_local: Number,
    private val project_name: String,
    private val service_type: String
        )


fun parseInvoiceLines (invoiceLine: String): List<Line> {
    val mapper = jacksonObjectMapper()
    return mapper.readValue<List<Line>>(invoiceLine)
}