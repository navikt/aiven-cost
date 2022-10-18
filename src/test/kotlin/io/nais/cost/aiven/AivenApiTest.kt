package io.nais.cost.aiven

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class AivenApiTest {

    @Test
    internal fun testInvoiceData() {
        val billingGroupId = "7d14362d-1e2a-4864-b408-1cc631bc4fab"
        val invoiceId = "da23c-1"
        val server = createWMServer()
        val aiven = Aiven("secret", "http://localhost:${server.port()}")
        val invoiceData = aiven.getInvoiceData()
        assertEquals(7, invoiceData.size)
        assertEquals("test-project", invoiceData.first().projectName)

        server.stop()
    }

    private fun createWMServer(): WireMockServer {
        val server = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
        server.start()
        val port = server.port()
        configureFor(port)

        stubFor(
            get(urlEqualTo("/v1/project"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(resource("projects.json")))
        )


        stubFor(
            get(urlMatching("/v1/project/(.*)/service"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(resource("services.json")))
        )


        stubFor(
            get(urlMatching("/v1/billing-group/(.*)/invoice"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(resource("invoices.json")))
        )

        stubFor(
            get(urlMatching("/v1/billing-group/(.*)/invoice/(.*)/lines"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(resource("invoicelines.json")))
        )
        return server
    }


    private fun resource(fileName: String): String? =
        javaClass.classLoader.getResource(fileName)?.readText()?.trimIndent()


}