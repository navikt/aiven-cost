package io.nais.cost.bigquery

import io.nais.cost.CostItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*

class CostItemDatasetTest {
    @Test
    fun `cost item can be converted to rowcontent`(){
        val costItem = CostItem(
            invoiceId = "abc",
            environment = "dev",
            team = "nais",
            date = LocalDate.of(2021, Month.JANUARY,1),
            service = "kafka",
            costInEuros = BigDecimal("1.00")
        )
        val rowContent: MutableMap<String, Any> = HashMap()
        rowContent["invoiceId"] = "abc"
        rowContent["environment"] = "dev"
        rowContent["team"] = "nais"
        rowContent["date"] = "2021-01-01"
        rowContent["service"] = "kafka"
        rowContent["costInEuros"] = BigDecimal("1.00")

        assertThat(rowContent).containsAllEntriesOf(toRow(costItem))

    }

}