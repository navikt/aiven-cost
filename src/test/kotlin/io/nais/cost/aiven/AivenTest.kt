package io.nais.cost.aiven

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import io.nais.cost.aiven.Aiven
import io.nais.cost.fromInvoiceLine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class AivenTest {

    @Test
    internal fun ` get a billiggroupId `() {

        val host = "http://localhost"
        val wireMockServer =
            WireMockServer(wireMockConfig().dynamicPort())
        wireMockServer.start()

        val port = wireMockServer.port()

        configureFor(port);


        val secret = "secret"
        val billingGroupId = "123"
        val invoiceId = "da23c-1"
        val billlingGroupJsonFromAiven = """
                                            {
                   "billing_groups":[
                      {
                         "account_id":"1233131",
                         "account_name":"NAV_Aiven",
                         "address_lines":[
                            
                         ],
                         "billing_address":"Atea nNorway",
                         "billing_currency":"USD",
                         "billing_emails":[
                            {
                               "email":"nn.ff.@nav.no"
                            },
                            {
                               "email":"xx.vv.qq@nav.no"
                            }
                         ],
                         "billing_extra_text":"text",
                         "billing_group_id":"$billingGroupId",
                         "billing_group_name":"Default billing group",
                         "card_info":null,
                         "city":"",
                         "company":"",
                         "country":"Norway",
                         "country_code":"NO",
                         "estimated_balance_local":"11.55",
                         "estimated_balance_usd":"11.55",
                         "payment_method":"email",
                         "state":"",
                         "vat_id":"",
                         "zip_code":""
                      }
                   ]
                }""".trimMargin()
        val invoicesJsonFromAiven = """
                                {
                       "invoices":[
                          {
                             "billing_group_name":"Default ",
                             "currency":"USD",
                             "download_cookie":"321",
                             "invoice_number":"da23c-1",
                             "period_begin":"2020-08-01T00:00:00Z",
                             "period_end":"2020-08-31T23:59:59Z",
                             "state":"paid",
                             "total_inc_vat":"111.65",
                             "total_vat_zero":"111.65"
                          }
                       ]
                    }""".trimMargin()

        val invoiceLineJsonFromAiven = """
            {
               "lines":[
                  {
                     "cloud_name":"skyen",
                     "description":"beskrivelse av skyen",
                     "line_total_local":"2.00",
                     "line_total_usd":"2.00",
                     "line_type":"service_charge",
                     "local_currency":"USD",
                     "project_name":"test-project",
                     "service_name":"test-service",
                     "service_plan":"test-plan",
                     "service_type":"test-type",
                     "timestamp_begin":"2020-08-19T11:21:31Z",
                     "timestamp_end":"2020-08-19T14:20:05Z"
                  }
                ]
            }
        """.trimIndent()


        stubFor(
            get(urlEqualTo("/v1/billing-group/$billingGroupId/invoice"))
                .withHeader("authorization", equalTo("aivenv1 $secret"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
                            invoicesJsonFromAiven.trimIndent()
                        )
                )
        )

        stubFor(
            get(urlEqualTo("/v1/billing-group"))
                .withHeader("authorization", equalTo("aivenv1 $secret"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
                            billlingGroupJsonFromAiven.trimIndent()
                        )
                )
        )

        stubFor(
            get(urlEqualTo("/v1/billing-group/$billingGroupId/invoice/$invoiceId/lines"))
                .withHeader("authorization", equalTo("aivenv1 $secret"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
                            invoiceLineJsonFromAiven.trimIndent()
                        )
                )
        )


        val aiven = Aiven(secret, "$host:$port")
        val invoiceData = aiven.getInvoiceData()
        assertEquals(1, invoiceData.size)
        assertEquals("test-project", invoiceData.first().getProjectName())

        wireMockServer.stop()
    }

    @Test
    fun testInvoiceLine(){

        val json = """
          {"lines":[{"cloud_name":"google-europe-north1","description":"roy-test: PostgreSQL Startup-4 google-europe-north1","line_total_local":"0.10","line_total_usd":"0.10","line_type":"service_charge","local_currency":"USD","project_name":"nav-23d2","service_name":"roy-test","service_plan":"startup-4","service_type":"pg","timestamp_begin":"2020-07-07T08:06:10Z","timestamp_end":"2020-07-07T08:06:28Z"},{"cloud_name":"google-europe-north1","description":"roy-test-kafka: Kafka Startup-2 google-europe-north1","line_total_local":"144.95","line_total_usd":"144.95","line_type":"service_charge","local_currency":"USD","project_name":"nav-23d2","service_name":"roy-test-kafka","service_plan":"startup-2","service_type":"kafka","timestamp_begin":"2020-07-07T08:06:52Z","timestamp_end":"2020-07-29T08:32:53Z"}]}
          """

        val map = JsonPath.parse(json)?.read<List<Map<String, String>>>("$.lines[*]").orEmpty()

        val invoiceLine = InvoiceLine("", map.first())
        val item = fromInvoiceLine(invoiceLine)

        assertEquals("dev", item.first().environment)


    }
}


