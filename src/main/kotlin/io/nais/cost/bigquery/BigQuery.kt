package io.nais.cost.bigquery

import com.google.cloud.bigquery.*
import io.nais.cost.CostItem
import org.slf4j.LoggerFactory

class BigQuery {
    val table = "costitems"
    val dataset = "aivencost"
    val project = "nais-analyse-prod-2dcc"


    private companion object {
        private val log = LoggerFactory.getLogger(BigQuery::class.java)
    }

    private val bigquery =
        BigQueryOptions.newBuilder()
            .setLocation("europe-north1")
            .setProjectId(project)
            .build().service

    fun write(costItems: List<CostItem>): InsertAllResponse {
        if (costItems.isNotEmpty()) {
            deleteContentFromTable()
        }
        val builder = InsertAllRequest.newBuilder(TableId.of(dataset, table))
        costItems.forEach { builder.addRow(toRow(it)) }

        val response = bigquery.insertAll(builder.build())
        if (response.hasErrors()) response.insertErrors.entries.forEach { log.info("insertError: ${it.value}") }

        return response
    }

    private fun deleteContentFromTable() {
        val query = "TRUNCATE TABLE $project.$dataset.$table"
        val results = bigquery.query(QueryJobConfiguration.newBuilder(query).build())
        results.iterateAll().forEach { log.info("insertError: $it") }
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

