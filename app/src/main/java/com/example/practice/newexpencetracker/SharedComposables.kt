package com.example.practice.newexpencetracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
//import com.example.practice.newexpencetracker.IncomeExpensesChart
//import com.example.practice.newexpencetracker.MonthStat
//import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlin.jvm.java

//@Composable
//fun OpenChartButton(modifier: Modifier = Modifier) {
//    val ctx = LocalContext.current
//    Button(
//        onClick = { ctx.startActivity(Intent(ctx, ChartActivity::class.java)) },
//        modifier = modifier
//    ) { Text("View Income/Expenses Chart") }
//}
//
//
//@Composable
//fun ChartDemoSection() {
//    val sample = listOf(
//        MonthStat("Aug", 1200f, 900f),
//        MonthStat("Sep", 1350f, 1100f),
//        MonthStat("Oct", 980f, 1060f),
//        MonthStat("Nov", 1500f, 950f),
//    )
//    IncomeExpensesChart(stats = sample)
//}

// ---------- Models ----------
data class ExpenseSheet(
    val id: Int,
    val month: String,
    val year: Int,
    var income: Double = 0.0,
    val expenses: MutableList<Expense> = mutableListOf()
){
    var incomeState by mutableStateOf(income)
}

data class Expense(
    val id: Int,
    val name: String,
    val amount: Double
)

// ===== Expenses UI (LazyList + Add dialog) =====
@Composable
fun ExpenseRow(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(expense.name, style = MaterialTheme.typography.titleSmall)
            Text("€${"%.2f".format(expense.amount)}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialogSimple(
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                error = null
                val amt = amountText.toDoubleOrNull()
                if (name.isBlank()) { error = "Please enter a name."; return@TextButton }
                if (amt == null || amt < 0.0) { error = "Enter a valid amount (≥ 0)."; return@TextButton }
                onSave(name.trim(), amt)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Expense name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12) },
                    label = { Text("Amount (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun ExpensesSection(sheet: ExpenseSheet) {
    var showDialog by remember { mutableStateOf(false) }

    Text(text = "Expenses:", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    Button(onClick = { showDialog = true }) { Text("Add Expense") }
    Spacer(Modifier.height(12.dp))

    if (sheet.expenses.isEmpty()) {
        Text("No expenses added yet.", style = MaterialTheme.typography.bodyMedium)
    } else {
        LazyColumn {
            items(items = sheet.expenses, key = { it.id }) { expense ->
                ExpenseRow(expense)
            }
        }
    }

    if (showDialog) {
        AddExpenseDialogSimple(
            onDismiss = { showDialog = false },
            onSave = { name, amount ->
                val nextId = (sheet.expenses.maxOfOrNull { it.id } ?: 0) + 1
                sheet.expenses.add(Expense(id = nextId, name = name, amount = amount))
                showDialog = false
            }
        )
    }
}


// ---------- Income Display ----------
@Composable
fun IncomeDisplay(
    income: Double,
    onIncomeChange: (Double) -> Unit,
    onDone: () -> Unit
) {
    var incomeText by remember(income) { mutableStateOf(income.toString()) }

    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = incomeText,
            onValueChange = { incomeText = it },
            label = { Text("Income (€)") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            val newIncome = incomeText.toDoubleOrNull() ?: 0.0
            onIncomeChange(newIncome)
            onDone() // ✅ go back after updating
        }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirm Income"
            )
        }
    }
}



// ---------- Sheet Detail Screen ----------
@Composable
fun SheetDetailScreen(sheet: ExpenseSheet, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${sheet.month} ${sheet.year}", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        IncomeDisplay(
            income = sheet.incomeState,
            onIncomeChange = { newIncome -> sheet.incomeState = newIncome },
            onDone = { onBack() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val totalExpenses = sheet.expenses.sumOf { it.amount }
        Text(
            text = "Surplus/Deficit: €${"%.2f".format(sheet.incomeState - totalExpenses)}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ New Expense LazyList section
        ExpensesSection(sheet)
    }
}



// ---------- Sheets List ----------
@Composable
fun SheetsList(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(sheets) { sheet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onSheetClick(sheet) },
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${sheet.month} ${sheet.year}", style = MaterialTheme.typography.titleMedium)
                    Text("Income: €${sheet.incomeState}")
                    val totalExpenses = sheet.expenses.sumOf { it.amount }
                    Text("Surplus/Deficit: €${sheet.incomeState - totalExpenses}")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSheetScreen(
    existing: List<ExpenseSheet>,
    onCancel: () -> Unit,
    onCreate: (month: String, year: Int) -> Unit
) {
    val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )

    val now = java.time.LocalDate.now()
    var month by remember { mutableStateOf(months[now.monthValue - 1]) }
    var expanded by remember { mutableStateOf(false) }
    var yearText by remember { mutableStateOf(now.year.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create New Sheet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Month dropdown
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = month,
                onValueChange = {},
                readOnly = true,
                label = { Text("Month") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                months.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            month = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Year input
        OutlinedTextField(
            value = yearText,
            onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
            label = { Text("Year") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))

        Row {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = {
                error = null
                val year = yearText.toIntOrNull()
                if (year == null || year < 1900 || year > 2100) {
                    error = "Enter a valid year (1900–2100)."
                    return@Button
                }
                val duplicate = existing.any { it.month == month && it.year == year }
                if (duplicate) {
                    error = "A sheet for $month $year already exists."
                    return@Button
                }
                onCreate(month, year)
            }) {
                Text("Create")
            }
        }
    }
}


