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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.practice.newexpencetracker.ui.theme.NewExpencetrackerTheme

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
                    ExpenseTrackerApp()
                }
            }
        }
    }
}

// 🔹 We no longer need a Screen.SheetDetail here, because
//    each month’s detailed view is now in a separate Activity (SheetActivity).
sealed class Screen {
    object SheetsList : Screen()
    object CreateSheet : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExpenseTrackerApp(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.SheetsList) }

    val context = LocalContext.current
    val sheets = remember { mutableStateListOf<ExpenseSheet>() }

    LaunchedEffect(Unit) {
        val db = database(context)
        val loaded = db.getAllSheetsWithExpenses()
        if (loaded.isEmpty()) {
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
        topBar = {
            if (currentScreen is Screen.SheetsList) {
                TopAppBar(
                    title = { Text("ExpenseTracker") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentScreen is Screen.SheetsList) {
                FloatingActionButton(onClick = { currentScreen = Screen.CreateSheet }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Sheet")
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = modifier.padding(innerPadding)) {
            when (currentScreen) {
                is Screen.SheetsList -> SheetsList(
                    sheets = sheets,
                    // 🔹 onSheetClick removed – SheetsList now starts SheetActivity itself
                    onDeleteSheet = { sheet ->
                        val db = database(context)
                        db.deleteSheet(sheet.id)   // delete from DB
                        sheets.remove(sheet)        // remove from in-memory list
                    }
                )

                is Screen.CreateSheet -> CreateSheetScreen(
                    existing = sheets,
                    onCancel = { currentScreen = Screen.SheetsList },
                    onCreate = { month, year ->
                        val nextId = (sheets.maxOfOrNull { it.id } ?: 0) + 1
                        val newSheet = ExpenseSheet(nextId, month, year, income = 0.0)
                        sheets.add(newSheet)
                        val db = database(context)
                        db.insertSheet(newSheet)
                        currentScreen = Screen.SheetsList
                    }
                )
            }
        }
    }
}
