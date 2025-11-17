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
import android.content.Intent
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateOf





import androidx.compose.ui.platform.LocalContext


@Composable
fun OpenChartButton(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Button(
        onClick = { ctx.startActivity(Intent(ctx, ChartActivity::class.java)) },
        modifier = modifier
    ) { Text("View Income/Expenses Chart") }
}


@Composable
fun ChartDemoSection() {
    val sample = listOf(
        MonthStat("Aug", 1200f, 900f),
        MonthStat("Sep", 1350f, 1100f),
        MonthStat("Oct", 980f, 1060f),
        MonthStat("Nov", 1500f, 950f),
    )
    IncomeExpensesChart(stats = sample)
}

// ---------- Models ----------

data class ExpenseSheet(
    val id: Int,
    var month: String,
    var year: Int,
    var income: Double = 0.0,
    val expenses: SnapshotStateList<Expense> = mutableStateListOf()
) {
    var incomeState by mutableStateOf(income)
}

data class Expense(
    var id: Int,
    var name: String,
    var amount: Double,
    val date: String = ""   // store the date as text, e.g. "2025-11-16"
)


// ===== Expenses UI (LazyList + Add dialog) =====
@Composable
fun ExpenseRow(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
            Column {
                Text(expense.name, style = MaterialTheme.typography.titleSmall)
                Text("€${"%.2f".format(expense.amount)}")
                if (expense.date.isNotBlank()) {
                    Text(
                        "Date: ${expense.date}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit expense"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete expense"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialogSimple(
    initialName: String = "",
    initialAmount: String = "",
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var amountText by remember(initialAmount) { mutableStateOf(initialAmount) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                error = null
                val amt = amountText.toDoubleOrNull()
                if (name.isBlank()) {
                    error = "Please enter a name."
                    return@TextButton
                }
                if (amt == null || amt < 0.0) {
                    error = "Enter a valid amount (≥ 0)."
                    return@TextButton
                }
                onSave(name.trim(), amt)
            }) {
                Text(if (isEditing) "Update" else "Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (isEditing) "Edit Expense" else "Add Expense") },
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
                    onValueChange = {
                        amountText = it.filter { c -> c.isDigit() || c == '.' }.take(12)
                    },
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
    val context = LocalContext.current
    val db = remember { database(context) }

    var showDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    Text(text = "Expenses:", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    // Row with "Add Expense" and "Delete All" for the month
    Row {
        Button(
            onClick = {
                editingExpense = null   // adding new expense
                showDialog = true
            }
        ) {
            Text("Add Expense")
        }

        Spacer(Modifier.width(12.dp))

        OutlinedButton(
            onClick = {
                // Delete all expenses for this month (sheet)
                if (sheet.expenses.isNotEmpty()) {
                    // First delete from DB
                    sheet.expenses.toList().forEach { expense ->
                        db.deleteExpense(expense.id)
                    }
                    // Then clear in-memory list
                    sheet.expenses.clear()
                }
            }
        ) {
            Text("Delete All Expenses")
        }
    }

    Spacer(Modifier.height(12.dp))

    if (sheet.expenses.isEmpty()) {
        Text("No expenses added yet.", style = MaterialTheme.typography.bodyMedium)
    } else {
        LazyColumn {
            items(items = sheet.expenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    onEdit = {
                        editingExpense = expense
                        showDialog = true
                    },
                    onDelete = {
                        // Remove from memory
                        sheet.expenses.remove(expense)
                        // Remove from DB
                        db.deleteExpense(expense.id)
                    }
                )
            }
        }
    }

    if (showDialog) {
        val exp = editingExpense
        AddExpenseDialogSimple(
            initialName = exp?.name ?: "",
            initialAmount = exp?.amount?.toString() ?: "",
            isEditing = exp != null,
            onDismiss = { showDialog = false },
            onSave = { name, amount ->
                if (exp == null) {
                    // 🔹 New expense
                    val today = java.text.SimpleDateFormat(
                        "yyyy-MM-dd",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    // id = 0 for now, DB will assign real id
                    val newExpense = Expense(
                        id = 0,
                        name = name,
                        amount = amount,
                        date = today
                    )

                    // Insert into DB first so it gets a real id
                    db.insertExpense(sheet.id, newExpense)

                    // Now add to in-memory list (id is updated inside insertExpense)
                    sheet.expenses.add(newExpense)
                } else {
                    // 🔹 Edit existing
                    exp.name = name
                    exp.amount = amount

                    // Update DB
                    db.updateExpense(exp)
                }
                showDialog = false
            }
        )
    }
}

// ---------- Income Display ----------
@Composable
fun IncomeDisplay(
    sheetId: Int,
    income: Double,
    onIncomeChange: (Double) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var incomeText by remember(income) {
        mutableStateOf(if (income == 0.0) "" else income.toString())
    }

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

            // Update in-memory state
            onIncomeChange(newIncome)

            // Save to SQLite
            val db = database(context)
            db.updateSheetIncome(sheetId, newIncome)

            onDone()
        }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Confirm Income"
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSheetDialog(
    sheet: ExpenseSheet,
    onDismiss: () -> Unit,
    onSave: (newMonth: String, newYear: Int) -> Unit
) {
    val months = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )

    var month by remember { mutableStateOf(sheet.month) }
    var expanded by remember { mutableStateOf(false) }
    var yearText by remember { mutableStateOf(sheet.year.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Sheet") },
        confirmButton = {
            TextButton(onClick = {
                val year = yearText.toIntOrNull()
                if (year == null || year < 1900 || year > 2100) {
                    error = "Enter a valid year (1900–2100)."
                    return@TextButton
                }

                onSave(month, year)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Month dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
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

                // Year input
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}




// ---------- Sheet Detail Screen ----------
@Composable
fun SheetDetailScreen(sheet: ExpenseSheet, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { database(context) }

    var showEditDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        // 🔹 Top row: month/year + edit icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "${sheet.month} ${sheet.year}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit month/year"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IncomeDisplay(
            sheetId = sheet.id,
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

        ExpensesSection(sheet)
    }

    // 🔥 Show edit dialog when user taps edit icon
    if (showEditDialog) {
        EditSheetDialog(
            sheet = sheet,
            onDismiss = { showEditDialog = false },
            onSave = { newMonth, newYear ->
                // Update in DB
                db.updateSheetDetails(sheet.id, newMonth, newYear)
                // Update in memory so UI refreshes
                sheet.month = newMonth
                sheet.year = newYear
                showEditDialog = false
            }
        )
    }
}




// ---------- Sheets List ----------
@Composable
fun SheetsList(
    sheets: List<ExpenseSheet>,
    onSheetClick: (ExpenseSheet) -> Unit,
    onDeleteSheet: (ExpenseSheet) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Top chart button
        item {
            OpenChartButton()
            Spacer(Modifier.height(16.dp))
        }

        items(sheets) { sheet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    // ✅ tap anywhere on the card (except bin) to open the sheet
                    .clickable { onSheetClick(sheet) },
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Top row: month/year + bin icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sheet.month} ${sheet.year}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onDeleteSheet(sheet) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete month sheet"
                            )
                        }
                    }

                    // Income / surplus
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

    // ✅ Default to current month & year
    val now = java.time.LocalDate.now()
    var month by remember { mutableStateOf(months[now.monthValue - 1]) }
    var expanded by remember { mutableStateOf(false) }
    var yearText by remember { mutableStateOf(now.year.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create New Sheet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Month dropdown (pre-filled with current month)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = month,
                onValueChange = {},
                readOnly = true,
                label = { Text("Month") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
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

        // Year input (pre-filled with current year)
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
