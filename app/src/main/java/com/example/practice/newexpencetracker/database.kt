package com.example.practice.newexpencetracker


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class database(context: Context) :
    SQLiteOpenHelper(context, "expense_tracker.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE sheets(
                id INTEGER PRIMARY KEY,
                month TEXT NOT NULL,
                year INTEGER NOT NULL,
                income REAL NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE expenses(
                id INTEGER PRIMARY KEY,
                sheet_id INTEGER NOT NULL,
                name TEXT NOT NULL,
                amount REAL NOT NULL,
                FOREIGN KEY(sheet_id) REFERENCES sheets(id) ON DELETE CASCADE
            );
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS sheets")
        onCreate(db)
    }

    // ---------- READ ALL SHEETS + THEIR EXPENSES ----------

    fun getAllSheetsWithExpenses(): List<ExpenseSheet> {
        val db = readableDatabase
        val result = mutableListOf<ExpenseSheet>()

        val cursor = db.rawQuery(
            "SELECT id, month, year, income FROM sheets ORDER BY year, id",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val month = it.getString(1)
                val year = it.getInt(2)
                val income = it.getDouble(3)

                val sheet = ExpenseSheet(id, month, year, income)
                sheet.expenses.addAll(getExpensesForSheet(id))
                result.add(sheet)
            }
        }
        return result
    }

    private fun getExpensesForSheet(sheetId: Int): List<Expense> {
        val db = readableDatabase
        val list = mutableListOf<Expense>()

        val cursor = db.rawQuery(
            "SELECT id, name, amount FROM expenses WHERE sheet_id = ?",
            arrayOf(sheetId.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val name = it.getString(1)
                val amount = it.getDouble(2)
                list.add(Expense(id = id, name = name, amount = amount))
            }
        }
        return list
    }

    // ---------- SHEETS CRUD ----------

    fun insertSheet(sheet: ExpenseSheet) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", sheet.id)
            put("month", sheet.month)
            put("year", sheet.year)
            put("income", sheet.incomeState)
        }
        db.insert("sheets", null, values)
    }

    fun updateSheetIncome(sheetId: Int, income: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("income", income)
        }
        db.update("sheets", values, "id = ?", arrayOf(sheetId.toString()))
    }

    // ---------- EXPENSES CRUD ----------

    fun insertExpense(sheetId: Int, expense: Expense) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", expense.id)
            put("sheet_id", sheetId)
            put("name", expense.name)
            put("amount", expense.amount)
        }
        db.insert("expenses", null, values)
    }

    fun updateExpense(expense: Expense) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", expense.name)
            put("amount", expense.amount)
        }
        db.update("expenses", values, "id = ?", arrayOf(expense.id.toString()))
    }

    fun deleteExpense(expenseId: Int) {
        val db = writableDatabase
        db.delete("expenses", "id = ?", arrayOf(expenseId.toString()))
    }
}
