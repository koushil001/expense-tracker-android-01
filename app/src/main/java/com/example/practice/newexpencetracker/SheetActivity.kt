package com.example.practice.newexpencetracker
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practice.newexpencetracker.ui.theme.NewExpencetrackerTheme

class SheetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Get which month sheet we need to show
        val sheetId = intent.getIntExtra("sheet_id", -1)
        if (sheetId == -1) {
            finish()
            return
        }

        val db = database(this)
        val sheet = db.getSheetWithExpenses(sheetId)

        setContent {
            NewExpencetrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (sheet == null) {
                        SheetNotFoundScreen(onBack = { finish() })
                    } else {
                        // ✅ Re-use your existing UI for a single month
                        SheetDetailScreen(
                            sheet = sheet,
                            onBack = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetNotFoundScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Sheet not found.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
