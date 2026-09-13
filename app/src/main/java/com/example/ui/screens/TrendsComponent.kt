package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.SavedReport
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun BloodTestTrendsSection(reports: List<SavedReport>) {
    val trends = remember(reports) { extractTrends(reports) }
    
    if (trends.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Metric Trends", 
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(trends) { trend ->
                    TrendCard(trend)
                }
            }
        }
    }
}

@Composable
fun TrendCard(trend: TrendData) {
    Card(modifier = Modifier.width(300.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = trend.testName, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(16.dp))
            val chartEntryModel = entryModelOf(trend.entries)
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1.2f, round = true)
                ),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _ -> 
                        val index = value.toInt()
                        if (index >= 0 && index < trend.dates.size) trend.dates[index] else ""
                    }
                ),
                modifier = Modifier.height(150.dp).fillMaxWidth()
            )
        }
    }
}
