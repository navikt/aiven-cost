package io.nais.cost

import io.ktor.http.*
import io.ktor.server.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class AivenCostKtTest {

    @Disabled
    @Test
    internal fun `isready is ready`() {
        withTestApplication(
            moduleFunction = { aivenApi() }
        ) {
            val testCall: TestApplicationCall = handleRequest(method = HttpMethod.Get, uri = "/internal/isready")
            testCall.response.status() == HttpStatusCode.OK
        }
    }

    @Test
    internal fun `next extension function returns the current date adjusted with timeofday when timeofday has not yet passed`() {
        val oslo = ZoneId.of("Europe/Oslo")
        val morningMarch31 = ZonedDateTime.of(LocalDateTime.of(2020, 1, 31, 8, 0),oslo)
        val midday = LocalTime.of(12, 0, 0)

        val noonMarch31 =  ZonedDateTime.of(LocalDateTime.of(2020, 1, 31, 12, 0),oslo)
        assertThat(morningMarch31.next(midday)).isEqualTo(Date.from(noonMarch31.toInstant()))
    }


    @Test
    internal fun `next extension function returns the current date with the current timeofday when timeofday happens now`() {
        val oslo = ZoneId.of("Europe/Oslo")
        val morningMarch31 = ZonedDateTime.of(LocalDateTime.of(2020, 1, 31, 12, 0),oslo)
        val midday = LocalTime.of(12, 0, 0)

        val noonMarch31 =  ZonedDateTime.of(LocalDateTime.of(2020, 1, 31, 12, 0),oslo)
        assertThat(morningMarch31.next(midday)).isEqualTo(Date.from(noonMarch31.toInstant()))
    }
    @Test
    internal fun `next extension function returns the next day adjusted with timeofday when timeofday already happened on this day`() {
        val oslo = ZoneId.of("Europe/Oslo")
        val eveningMarch31 = ZonedDateTime.of(LocalDateTime.of(2020, 1, 31, 18, 0),oslo)
        val midday = LocalTime.of(12, 0, 0)

        val noonFebruray1 =  ZonedDateTime.of(LocalDateTime.of(2020, 2, 1, 12, 0),oslo)
        assertThat(eveningMarch31.next(midday)).isEqualTo(Date.from(noonFebruray1.toInstant()))
    }


}
