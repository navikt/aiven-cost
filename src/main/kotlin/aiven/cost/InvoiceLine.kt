package aiven.cost

import java.time.LocalDateTime

data class InvoiceLine(val input : Map<String,String>){
    fun getProjectName() = input.get("project_name")
}

