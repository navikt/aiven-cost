package aiven.cost

import org.junit.jupiter.api.Test
import java.time.Month
import java.time.YearMonth
import kotlin.test.assertEquals

class CostItemTest {


    @Test
    fun `extract team name from service name`() {
        val teamName = "elastic-dolly-testdata-gjeter".getTeamName()
        assertEquals("dolly", teamName)
    }

    @Test
    fun `extract environment`() {
        val env = "nav-dev".toEnvironment()
        assertEquals("dev", env)
    }

    @Test
    fun `convert to euros`() {
        val euro = 1.0.toEuros()
        assertEquals(0.85, euro)
    }

    @Test
    fun `convert timestamp to yearmonth`() {
        val march2021 = "2021-03-01T00:00:00Z".toYearMonth()
        assertEquals(YearMonth.of(2021, Month.MARCH), march2021)
    }


}