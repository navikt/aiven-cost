package io.nais.cost

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


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
}