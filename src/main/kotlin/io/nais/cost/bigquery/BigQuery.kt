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

    private val bigquery =
        BigQueryOptions.newBuilder()
            .setLocation("europe-north1")
            .setProjectId("nais-analyse-prod-2dcc")
            .build().service

    fun write(costItems: List<CostItem>): InsertAllResponse {
        val builder = InsertAllRequest.newBuilder(TableId.of("aivencost", "costitems"))
        costItems.forEach { builder.addRow(toRow(it)) }

        val response = bigquery.insertAll(builder.build())
        if (response.hasErrors()) response.insertErrors.entries.forEach { log.info("insertError: ${it.value}") }

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

