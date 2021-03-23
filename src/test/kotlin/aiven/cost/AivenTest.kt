package aiven.cost

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

import org.junit.jupiter.api.Test

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents

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
                             "download_cookie":"123",
                             "invoice_number":"$invoiceId",
                             "period_begin":"2020-07-01T00:00:00Z",
                             "period_end":"2020-07-31T23:59:59Z",
                             "state":"paid",
                             "total_inc_vat":"0.00",
                             "total_vat_zero":"0.00"
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
        val groupId = aiven.getInvoiceData()
        assertEquals("", groupId)


        wireMockServer.stop()
    }
}


