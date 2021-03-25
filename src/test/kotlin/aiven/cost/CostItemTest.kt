package aiven.cost

import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import kotlin.math.cos
import kotlin.test.assertEquals

class CostItemTest {

    @Test
    fun `convert invoiceLine to costItem for elastic`(){
        val invoiceLine = InvoiceLine(mapOf(
            "project_name" to "nav-dev",
            "line_total_usd" to "2.00",
            "line_type" to "service_charge",
            "service_name" to "elastic-teamnavn-appnavn",
            "service_type" to "elastic",
            "timestamp_begin" to "2021-03-01T00:00:00Z",
            "timestamp_end" to "2021-03-02T00:00:00Z",
        ))
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)

        assertEquals("teamnavn", costItem.team)
        assertEquals("dev", costItem.environment)
        assertEquals("1.7", costItem.costInEuros)
        assertEquals(YearMonth.of(2021, Month.MARCH), costItem.month)
        assertEquals("elastic", costItem.service)

    }

    @Test
    fun `convert invoiceLine to costItem for kafka`(){
        val invoiceLine = InvoiceLine(mapOf(
            "project_name" to "nav-dev",
            "line_total_usd" to "2.00",
            "line_type" to "service_charge",
            "service_name" to "nav-dev-kafka",
            "service_type" to "kafka",
            "timestamp_begin" to "2021-03-01T00:00:00Z",
            "timestamp_end" to "2021-03-02T00:00:00Z",
        ))
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)
        assertEquals("nais", costItem.team)
        assertEquals("kafka", costItem.service)
    }

    @Test
    fun `convert invoiceLine to costItem for support`(){
        val invoiceLine = InvoiceLine(mapOf(
            "project_name" to "nav-prod",
            "line_total_usd" to "2.00",
            "line_type" to "extra_charge",
            "service_name" to "",
            "service_type" to "",
            "timestamp_begin" to "2021-03-01T00:00:00Z",
            "timestamp_end" to "2021-03-02T00:00:00Z",
        ))
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)
        assertEquals("nais", costItem.team)
        assertEquals("support", costItem.service)
    }
}

