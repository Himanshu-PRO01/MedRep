package com.example.ui.screens

import com.example.data.SavedReport
import com.patrykandpatrick.vico.core.entry.FloatEntry

data class TrendData(
    val testName: String,
    val entries: List<FloatEntry>,
    val dates: List<String>
)

fun extractTrends(reports: List<SavedReport>): List<TrendData> {
    // Only consider blood test reports
    val bloodTests = reports.filter { it.category == "Blood Test" }
        // Sort by date (assuming ISO format YYYY-MM-DD or similar, string sort works mostly, 
        // but let's just reverse assuming they are newest first normally, wait, room might not sort them)
        .sortedBy { it.date }

    val metricsMap = mutableMapOf<String, MutableList<Pair<String, Float>>>()
    val valueRegex = Regex("[-+]?[0-9]*\\.?[0-9]+")

    for (report in bloodTests) {
        for (item in report.testItems) {
            val valueStr = item.resultValue
            val match = valueRegex.find(valueStr)
            val numValue = match?.value?.toFloatOrNull()
            if (numValue != null) {
                val list = metricsMap.getOrPut(item.simpleName.ifBlank { item.testName }) { mutableListOf() }
                list.add(Pair(report.date, numValue))
            }
        }
    }

    // Only return trends that have at least 2 data points
    return metricsMap.filter { it.value.size >= 2 }.map { (name, data) ->
        val entries = data.mapIndexed { index, pair -> FloatEntry(x = index.toFloat(), y = pair.second) }
        val dates = data.map { it.first }
        TrendData(name, entries, dates)
    }
}
