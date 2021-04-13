package io.nais.cost

import io.nais.cost.aiven.InvoiceLine
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import kotlin.test.assertEquals

class CostItemTest {

    @Test
    fun `convert invoiceLine to costItem for elastic`() {
        val invoiceLine = InvoiceLine(
            "id",
            mapOf(
                "project_name" to "nav-dev",
                "line_total_usd" to "3.00",
                "line_type" to "service_charge",
                "service_name" to "elastic-teamnavn-appnavn",
                "service_type" to "elastic",
                "timestamp_begin" to "2021-03-01T00:00:00Z",
                "timestamp_end" to "2021-03-04T00:00:00Z",
            )
        )
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)

        assertEquals("teamnavn", costItem.first().team)
        assertEquals("dev", costItem.first().environment)
        assertEquals("0.85", costItem.first().costInEuros.toPlainString())
        assertEquals(LocalDate.of(2021, Month.MARCH,1), costItem.first().date)
        assertEquals("elastic", costItem.first().service)

    }

    @Test
    fun `get team name`() {
        assertEquals("audun", "audun".getTeamName("", ""))
        assertEquals("teamaudun", "audun-teamaudun".getTeamName("", ""))
        assertEquals("teamaudun", "audun-teamaudun-audun".getTeamName("", ""))
        assertEquals("", "".getTeamName("", ""))
        assertEquals("nais", "whatever".getTeamName("kafka", ""))
        assertEquals("nais", "whatever".getTeamName("", "extra_charge"))
    }

    @Test
    fun `convert invoiceLine to costItem for kafka`() {
        val invoiceLine = InvoiceLine(
            "id",
            mapOf(
                "project_name" to "nav-dev",
                "line_total_usd" to "2.00",
                "line_type" to "service_charge",
                "service_name" to "nav-dev-kafka",
                "service_type" to "kafka",
                "timestamp_begin" to "2021-03-01T00:00:00Z",
                "timestamp_end" to "2021-03-02T00:00:00Z",
            )
        )
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)
        assertEquals("nais", costItem.first().team)
        assertEquals("kafka", costItem.first().service)
    }

    @Test
    fun `convert invoiceLine to costItem for support`() {
        val invoiceLine = InvoiceLine(
            "id",
            mapOf(
                "project_name" to "nav-prod",
                "line_total_usd" to "2.00",
                "line_type" to "extra_charge",
                "service_name" to "",
                "service_type" to "",
                "timestamp_begin" to "2021-03-01T00:00:00Z",
                "timestamp_end" to "2021-03-02T00:00:00Z",
            )
        )
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)
        assertEquals("nais", costItem.first().team)
        assertEquals("support", costItem.first().service)
    }

    @Test
    fun `convert invoiceLine to costItem for test`() {
        val invoiceLine = InvoiceLine(
            "id",
            mapOf(
                "project_name" to "nav-23d2",
                "line_total_usd" to "2.00",
                "line_type" to "service_charge",
                "service_name" to "mortenlj-test-kafka",
                "service_type" to "kafka",
                "timestamp_begin" to "2021-03-01T00:00:00Z",
                "timestamp_end" to "2021-03-02T00:00:00Z",
            )
        )
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)
        assertEquals("nais", costItem.first().team)
        assertEquals("dev", costItem.first().environment)
    }


    @Test
    fun `get list of dates from begin and end timestamp`() {
        assertEquals(7, getDateRangeFromInvoiceLine("2021-03-01T00:00:00Z", "2021-03-08T00:00:00Z").size)
        assertEquals(8, getDateRangeFromInvoiceLine("2021-03-01T00:00:00Z", "2021-03-08T08:00:00Z").size)
        assertEquals(1, getDateRangeFromInvoiceLine("2021-03-01T00:00:00Z", "2021-03-01T08:00:00Z").size)

    }
}

