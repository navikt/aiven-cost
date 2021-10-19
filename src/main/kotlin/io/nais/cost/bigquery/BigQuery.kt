package io.nais.cost.bigquery

import com.google.cloud.bigquery.*
import io.nais.cost.BiqQueryErrorCounter
import io.nais.cost.CostItem
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.format.DateTimeFormatter

class BigQuery {
    val table = "costitems"
    val dataset = "aivencost"
    val project = "nais-analyse-prod-2dcc"


    private companion object {
        private val log = LoggerFactory.getLogger(BigQuery::class.java)
    }

    private val errorCounter = BiqQueryErrorCounter()

    private val bigquery =
        BigQueryOptions.newBuilder()
            .setLocation("europe-north1")
            .setProjectId(project)
            .build()

    fun write(costItems: List<CostItem>) {
        if (costItems.isNotEmpty()) {
            deleteContentFromTable()
            //log.info("ZZZZZ")
            //Thread.sleep(Duration.ofSeconds(120L).toMillis())
            log.info("woke up after truncating")
        }
        val builder = InsertAllRequest.newBuilder(TableId.of(dataset, table))
        costItems.forEach { builder.addRow(toRow(it)) }
        val request = builder.build()
        log.info("inserting ${costItems.size} items")
        try {
            val response = bigquery.service.insertAll(request)
            log.info("inserted lineitems to bigquery")
            if (response.hasErrors()) response.insertErrors.entries.forEach { log.error("insertError: ${it.value}") }
            else log.info("no errors when inserting logitems")

        } catch (e: BigQueryException) {
            errorCounter.countError()
            log.error(e.message)
            throw e
        }
    }

    fun deleteContentFromTable() {
        log.info("truncating table $table")
        val query = "DELETE FROM `$project.$dataset.$table` WHERE true;"
        //val query = "TRUNCATE TABLE `$project.$dataset.$table`"
        val results = bigquery.service.query(QueryJobConfiguration.newBuilder(query).build())
        log.info("truncated table $table")
        results.iterateAll().forEach { log.info("insertError: $it") }
    }
}

fun toRow(costItem: CostItem): Map<String, Any> {
    return mapOf(
        "invoiceId" to costItem.invoiceId,
        "environment" to costItem.environment,
        "team" to costItem.team,
        "date" to DateTimeFormatter.ISO_DATE.format(costItem.date),
        "service" to costItem.service,
        "costInEuros" to costItem.costInEuros
    )
}

