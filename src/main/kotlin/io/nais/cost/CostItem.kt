package io.nais.cost

import io.nais.cost.aiven.InvoiceLine
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CostItem(
    val invoiceId: String,
    val month: String,
    val team: String,
    val service: String,
    val environment: String,
    val costInEuros: BigDecimal
)

fun fromInvoiceLine(invoiceLine: InvoiceLine): CostItem {
    return CostItem(
        invoiceId = invoiceLine.invoiceId,
        costInEuros = invoiceLine.getLineTotal().toEuros(),
        service = invoiceLine.getServiceType().getService(invoiceLine.getLineType()),
        month = invoiceLine.getBeginTimestamp().toYearMonth(),
        team = invoiceLine.getServiceName().getTeamName(
            invoiceLine.getServiceType(),
            invoiceLine.getLineType()
        ),
        environment = invoiceLine.getProjectName().toEnvironment()
    )
}

fun String.toEnvironment(): String {
    val environment = this.split("-")[1]
    return if (environment != "prod") "dev" else "prod"
}

fun String.toEuros(): BigDecimal = BigDecimal.valueOf(this.toDouble().times(0.85))

fun String.toYearMonth() = YearMonth.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME).toString()

fun String.getTeamName(serviceType: String = "", lineType: String = "") =
    when {
        serviceType.isPlatform() -> "nais"
        lineType == "extra_charge" -> "nais"
        lineType == "credit_consumption" -> "nais"
        !this.contains("-") -> this
        else -> this.split("-")[1]
    }

fun String.isPlatform() = this == "kafka"

fun String.getService(lineType: String = "") =
    when (lineType) {
        "extra_charge" -> "support"
        "credit_consumption" -> "credit"
        else -> this
    }

