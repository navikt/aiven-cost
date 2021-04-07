package io.nais.cost.bigquery

import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.InsertAllResponse
import com.google.cloud.bigquery.TableId
import io.nais.cost.CostItem
import org.slf4j.LoggerFactory

class BigQuery {

    private companion object {
        private val log = LoggerFactory.getLogger(BigQuery::class.java)
    }

    private val bigquery = BigQueryOptions.getDefaultInstance().service

    fun write(costItems: List<CostItem>): InsertAllResponse {

        val tableId: TableId = TableId.of("aivencost", "costitems")
        val builder = InsertAllRequest.newBuilder(tableId)

        costItems.forEach {builder.addRow(toRow(it))}

        val response: InsertAllResponse = bigquery.insertAll(builder.build())

        if (response.hasErrors()) {
            response.insertErrors.entries.forEach { entry ->
                log.info("insertError ${entry.value}")
            }
        }
        return response
    }

}

 fun toRow(costItem: CostItem): Map<String, Any> {
    return mapOf(
        "invoiceId" to costItem.invoiceId,
        "environment" to costItem.environment,
        "team" to costItem.team,
        "month" to costItem.month,
        "service" to costItem.service,
        "costInEuros" to costItem.costInEuros
    )
}

