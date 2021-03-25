package aiven.cost

import io.ktor.util.*
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CostItem(
    val month: YearMonth,
    val team: String,
    val service: String,
    val environment: String,
    val costInEuros: String
) {

}

fun fromInvoiceLine(invoiceLine: InvoiceLine): CostItem {
    return CostItem(
        costInEuros = invoiceLine.getLineTotal().toEuros(),
        service = invoiceLine.getServiceType().getService(invoiceLine.getLineType()),
        month = invoiceLine.getBeginTimestamp().toYearMonth(),
        team = invoiceLine.getServiceName().getTeamName(
            invoiceLine.getServiceType(),
            invoiceLine.getLineType()),
        environment = invoiceLine.getProjectName().toEnvironment()
    )
}

fun String.toEnvironment(): String {
    //"project_name":"nav-dev",
    return this.split("-")[1]
}

fun String.toEuros(): String {
    return this.toDouble().times(0.85).toString()
}

fun String.toYearMonth(): YearMonth {
    //"timestamp_begin":"2021-03-01T00:00:00Z",
    return YearMonth.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)
}

fun String.getTeamName(serviceType: String = "", lineType: String = ""): String {
    //"service_name":"elastic-dolly-testdata-gjeter",
    if (serviceType.isPlatform()) return "nais"
    if (lineType.equals("extra_charge")) return "nais"
    return this.split("-")[1]
}

fun String.isPlatform(): Boolean {
    return this.equals("kafka")
}

fun String.getService(lineType: String = ""): String {
    if (lineType.equals("extra_charge")) return "support"
    return this
}