package io.nais.cost.aiven

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class AivenApiTest {

    @Test
    internal fun testInvoiceData() {
        val server = createWMServer()
        val aiven = Aiven("secret", "http://localhost:${server.port()}")
        val invoiceData = aiven.getInvoiceData()
        assertEquals(6, invoiceData.size) //only 6 of 7 invoice should have access
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
                .willReturn(aResponse().withStatus(200).withBody(fromResource("projects.json")))
        )


        stubFor(
            get(urlMatching("/v1/project/(.*)/service"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(fromResource("services.json")))
        )

        stubFor(
            get(urlMatching("/v1/billing-group/(.*)/invoice")).atPriority(2)
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(fromResource("invoices.json")))
        )

        stubFor(
            get(urlEqualTo("/v1/billing-group/fbfa93c8-f821-49b9-89a8-8b0aa628e670/invoice")).atPriority(1)
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(403).withStatusMessage("Insufficient permissions"))
        )

        stubFor(
            get(urlMatching("/v1/billing-group/(.*)/invoice/(.*)/lines"))
                .withHeader("authorization", equalTo("aivenv1 secret"))
                .willReturn(aResponse().withStatus(200).withBody(fromResource("invoicelines.json")))
        )
        return server
    }

    private fun fromResource(fileName: String): String? =
        javaClass.classLoader.getResource(fileName)?.readText()?.trimIndent()


}