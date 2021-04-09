package io.nais.cost

import io.nais.cost.aiven.InvoiceLine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CostItemTest {

    @Test
    fun `convert invoiceLine to costItem for elastic`() {
        val invoiceLine = InvoiceLine(
            "id",
            mapOf(
                "project_name" to "nav-dev",
                "line_total_usd" to "2.00",
                "line_type" to "service_charge",
                "service_name" to "elastic-teamnavn-appnavn",
                "service_type" to "elastic",
                "timestamp_begin" to "2021-03-01T00:00:00Z",
                "timestamp_end" to "2021-03-02T00:00:00Z",
            )
        )
        val costItem = fromInvoiceLine(invoiceLine = invoiceLine)

        assertEquals("teamnavn", costItem.team)
        assertEquals("dev", costItem.environment)
        assertEquals("1.7", costItem.costInEuros.toPlainString())
        assertEquals("2021-03", costItem.month)
        assertEquals("elastic", costItem.service)

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
        assertEquals("nais", costItem.team)
        assertEquals("kafka", costItem.service)
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
        assertEquals("nais", costItem.team)
        assertEquals("support", costItem.service)
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
        assertEquals("nais", costItem.team)
        assertEquals("dev", costItem.environment)
    }
}

