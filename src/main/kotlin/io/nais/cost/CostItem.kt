package io.nais.cost

import io.nais.cost.aiven.InvoiceLine
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class CostItem(
    val invoiceId: String,
    val date: String,
    val team: String,
    val service: String,
    val environment: String,
    val costInEuros: BigDecimal
)

fun fromInvoiceLine(invoiceLine: InvoiceLine): List<CostItem> {
    val range =
        getDateRangeFromInvoiceLine(invoiceLine.getBeginTimestamp(), invoiceLine.getEndTimestamp())
    val noOfDaysInRage = range.size.toLong()
    return range
        .map { currentDate ->
            CostItem(
                invoiceId = invoiceLine.invoiceId,
                costInEuros =
                    invoiceLine.getLineTotal().toEuros().divide(BigDecimal.valueOf(noOfDaysInRage), RoundingMode.HALF_UP),
                    service = invoiceLine.getServiceType().getService(invoiceLine.getLineType()),
                    date = DateTimeFormatter.ISO_DATE.format(currentDate),
                    team = invoiceLine.getServiceName().getTeamName(
                        invoiceLine.getServiceType(),
                        invoiceLine.getLineType()
                    ),
                    environment = invoiceLine.getProjectName().toEnvironment()
                )
        }
}

fun getDateRangeFromInvoiceLine(beginTimestamp: String, endTimestamp: String): List<LocalDate> {
    val begin = ZonedDateTime.parse(beginTimestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    val end = ZonedDateTime.parse(endTimestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    val dates = mutableListOf<LocalDate>()

    for (date in begin..end.minusSeconds(1) step 1) {
        dates.add(date.toLocalDate())
    }
    return dates

}

fun String.toEnvironment(): String {
    return if (!this.contains("prod")) "dev" else "prod"
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

fun String.getService(lineType: String = "") =
    when (lineType) {
        "extra_charge" -> "support"
        "credit_consumption" -> "credit"
        else -> this
    }

fun String.isPlatform() = this == "kafka"


class DateIterator(
    val startDate: ZonedDateTime,
    val endDateInclusive: ZonedDateTime,
    val stepDays: Long
) : Iterator<ZonedDateTime> {
    private var currentDate = startDate
    override fun hasNext() = currentDate <= endDateInclusive
    override fun next(): ZonedDateTime {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}

class DateProgression(
    override val start: ZonedDateTime,
    override val endInclusive: ZonedDateTime,
    val stepDays: Long = 1
) :
    Iterable<ZonedDateTime>, ClosedRange<ZonedDateTime> {
    override fun iterator(): Iterator<ZonedDateTime> =
        DateIterator(start, endInclusive, stepDays)

    infix fun step(days: Long) = DateProgression(start, endInclusive, days)
}

operator fun ZonedDateTime.rangeTo(other: ZonedDateTime) = DateProgression(this, other)
