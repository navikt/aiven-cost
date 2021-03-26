package io.nais.cost.bigquery

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.InsertAllResponse
import com.google.cloud.bigquery.TableId
import java.util.HashMap
import com.google.cloud.bigquery.BigQueryOptions
import io.nais.cost.CostItem


class CostItemDataset {
    fun writeCostItemsToBigQuery(): InsertAllResponse {
        val bigquery = BigQueryOptions.getDefaultInstance().service
        // [START bigquery_table_insert_rows]
        val tableId: TableId = TableId.of("aivencost", "costitems")
        // Values of the row to insert

        val rowContent: MutableMap<String, Any> = HashMap()
        rowContent["booleanField"] = true
        // Bytes are passed in base64

        rowContent["bytesField"] = "Cg0NDg0=" // 0xA, 0xD, 0xD, 0xE, 0xD in base64

        // Records are passed as a map

        val recordsContent: MutableMap<String, Any> = HashMap()
        recordsContent["stringField"] = "Hello, World!"
        rowContent["recordField"] = recordsContent
        val response: InsertAllResponse = bigquery.insertAll(
            InsertAllRequest.newBuilder(tableId)
                .addRow(
                    "rowId",
                    rowContent
                ) // More rows can be added in the same RPC by invoking .addRow() on the builder
                .build()
        )
        if (response.hasErrors()) {
            // If any of the insertions failed, this lets you inspect the errors
            for (entry in response.getInsertErrors().entries) {
                // inspect row error
            }
        }
        // [END bigquery_table_insert_rows]
        return response
    }


}

fun convertCostItemToRowContent(costItem: CostItem): MutableMap<String, Any> {
    TODO("Not yet implemented")
}