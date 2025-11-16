package com.example.practice.newexpencetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practice.newexpencetracker.ui.theme.NewExpencetrackerTheme

class ChartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Use your SQLiteOpenHelper
        val db = database(this)
        val sheets = db.getAllSheetsWithExpenses()

        // Take the last 4 "months" (sheets)
        val lastSheets = if (sheets.size > 4) {
            sheets.takeLast(4)
        } else {
            sheets
        }

        // Convert to MonthStat for the chart
        val stats = lastSheets.map { sheet ->
            val totalExpenses = sheet.expenses.sumOf { it.amount }
            MonthStat(
                monthLabel = sheet.month.take(3),          // e.g. "August" -> "Aug"
                income = sheet.incomeState.toFloat(),
                expenses = totalExpenses.toFloat()
            )
        }

        setContent {
            NewExpencetrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IncomeExpensesChartScreen(
                        stats = stats,
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpensesChartScreen(
    stats: List<MonthStat>,
    onClose: () -> Unit
) {
    Column {
        if (stats.isEmpty()) {
            Text(
                text = "No data available for the chart yet.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            IncomeExpensesChart(stats = stats)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Back")
        }
    }
}
