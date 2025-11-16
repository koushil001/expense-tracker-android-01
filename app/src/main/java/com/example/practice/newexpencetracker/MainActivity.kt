package com.example.practice.newexpencetracker

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.practice.newexpencetracker.ui.theme.NewExpencetrackerTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewExpencetrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // FAB and navigation state now live inside this composable
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

/* ---------- Navigation screens (top-level, not inside a composable) ---------- */
sealed class Screen {
    object SheetsList : Screen()
    data class SheetDetail(val sheet: ExpenseSheet) : Screen()
    object CreateSheet : Screen()
}

/* ---------- App root ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseTrackerApp(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SheetsList) }

    val context = LocalContext.current
    val sheets = remember { mutableStateListOf<ExpenseSheet>() }

    // Load from SQLite once when app starts
    LaunchedEffect(Unit) {
        val db = database(context)
        val loaded = db.getAllSheetsWithExpenses()

        if (loaded.isEmpty()) {
            // Optional: seed initial data if DB is empty
            val initial = listOf(
                ExpenseSheet(1, "August", 2025, 1200.0),
                ExpenseSheet(2, "September", 2025, 1500.0),
                ExpenseSheet(3, "October", 2025, 1100.0)
            )
            initial.forEach { db.insertSheet(it) }
            sheets.clear()
            sheets.addAll(initial)
        } else {
            sheets.clear()
            sheets.addAll(loaded)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentScreen is Screen.SheetsList) {
                FloatingActionButton(onClick = { currentScreen = Screen.CreateSheet }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Sheet")
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = modifier.padding(innerPadding)) {
            when (val s = currentScreen) {
                is Screen.SheetsList -> SheetsList(
                    sheets = sheets,
                    onSheetClick = { sheet -> currentScreen = Screen.SheetDetail(sheet) }
                )

                is Screen.SheetDetail -> SheetDetailScreen(
                    sheet = s.sheet,
                    onBack = { currentScreen = Screen.SheetsList }
                )

                is Screen.CreateSheet -> CreateSheetScreen(
                    existing = sheets,
                    onCancel = { currentScreen = Screen.SheetsList },
                    onCreate = { month, year ->
                        val nextId = (sheets.maxOfOrNull { it.id } ?: 0) + 1
                        val newSheet = ExpenseSheet(nextId, month, year, income = 0.0)

                        // Update in-memory list
                        sheets.add(newSheet)

                        // Save to DB
                        val db = database(context)
                        db.insertSheet(newSheet)

                        currentScreen = Screen.SheetsList
                    }
                )
            }
        }
    }
}


