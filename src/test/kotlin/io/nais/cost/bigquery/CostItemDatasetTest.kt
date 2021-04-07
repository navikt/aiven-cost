package io.nais.cost.bigquery

import io.nais.cost.CostItem
import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import java.util.HashMap
import kotlin.test.assertEquals

class CostItemDatasetTest {
    @Test
    fun `cost item can be converted to rowcontent`(){
        val costItem = CostItem(
            "abc", YearMonth.of(2021, Month.JANUARY), "nais", "kafka", "dev", "1.00"
        )
        val rowContent: MutableMap<String, Any> = HashMap()
        rowContent["invoiceId"] = "abc"
        rowContent["environment"] = "dev"
        rowContent["team"] = "nais"
        rowContent["month"] = "2021-01"
        rowContent["service"] = "kafka"
        rowContent["costInEuros"] = 1.00

        //assertEquals(rowContent, convertCostItemToRowContent(costItem))

    }

}