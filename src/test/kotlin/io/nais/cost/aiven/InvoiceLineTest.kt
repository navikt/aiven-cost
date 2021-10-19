package io.nais.cost.aiven

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import io.nais.cost.fromInvoiceLine
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class InvoiceLineTest {


    @Test
    internal fun testInvoiceLine() {

        @Language("JSON")
        val json = """{
  "lines": [
    {
      "cloud_name": "google-europe-north1",
      "description": "roy-test: PostgreSQL Startup-4 google-europe-north1",
      "line_total_local": "144.95",
      "line_total_usd": "144.95",
      "line_type": "service_charge",
      "local_currency": "USD",
      "project_name": "nav-23d2",
      "service_name": "roy-test",
      "service_plan": "startup-4",
      "service_type": "pg",
      "tags": {},
      "timestamp_begin": "2020-07-07T08:06:10Z",
      "timestamp_end": "2020-07-07T08:06:28Z"
    },
    {
      "cloud_name": "google-europe-north1",
      "description": "roy-test-kafka: Kafka Startup-2 google-europe-north1",
      "line_total_local": "144.95",
      "line_total_usd": "144.95",
      "line_type": "service_charge",
      "project_name": "nav-23d2",
      "service_name": "roy-test-kafka",
      "service_plan": "startup-2",
      "service_type": "kafka",
      "timestamp_begin": "2020-07-07T08:06:52Z",
      "timestamp_end": "2020-07-29T08:32:53Z"
    }
  ]
}
          """

        val map = JsonPath.parse(json)?.read<List<Map<String, Any>>>("$.lines[*]").orEmpty()

        val invoiceLine = InvoiceLine("", map.first())
        val item = fromInvoiceLine(invoiceLine)

        kotlin.test.assertEquals("dev", item.first().environment)
    }
}