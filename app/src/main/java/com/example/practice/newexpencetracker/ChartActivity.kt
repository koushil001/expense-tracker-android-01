package com.example.practice.newexpencetracker


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practice.newexpencetracker.ui.theme.NewExpencetrackerTheme

class ChartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewExpencetrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IncomeExpensesChartScreen(
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpensesChartScreen(onClose: () -> Unit) {
    // For now: simple sample of 4 months.
    // Later, you can replace with real data from DB/Intents if needed.
    val sampleStats = listOf(
        MonthStat("Aug", 1200f, 900f),
        MonthStat("Sep", 1350f, 1100f),
        MonthStat("Oct", 980f, 1060f),
        MonthStat("Nov", 1500f, 950f),
    )

    Column {
        // The custom chart composable
        IncomeExpensesChart(stats = sampleStats)

        Spacer(Modifier.height(16.dp))

        Button(onClick = onClose, modifier = Modifier) {
            Text("Back")
        }
    }
}
