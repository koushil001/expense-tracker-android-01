package com.example.practice.newexpencetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

// Represents one month of stats
data class MonthStat(
    val monthLabel: String,   // e.g. "Aug"
    val income: Float,
    val expenses: Float
)

@Composable
fun IncomeExpensesChart(
    stats: List<MonthStat>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Income/Expenses",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text("No data to display", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    // We show at most 4 months at a time
    val visibleCount = min(4, stats.size)

    // Which "window" of the list is currently visible
    var startIndex by remember(stats) {
        mutableStateOf(stats.size - visibleCount) // start with last 4
    }

    val maxStart = (stats.size - visibleCount).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title: "Income/Expenses"
        Text(
            text = "Income/Expenses",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        // Square chart area with drag
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // square aspect ratio
                .pointerInput(stats, visibleCount) {
                    detectDragGestures { _, dragAmount ->
                        val threshold = 40f // how far to drag before we move one month

                        if (dragAmount.x < -threshold && startIndex < maxStart) {
                            // drag left -> move to later months
                            startIndex++
                        } else if (dragAmount.x > threshold && startIndex > 0) {
                            // drag right -> move to earlier months
                            startIndex--
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // visible slice of all stats
                val visibleStats = stats.subList(
                    startIndex,
                    startIndex + visibleCount
                )

                if (visibleStats.isEmpty()) return@Canvas

                // chart padding inside the square
                val paddingLeft = 80f
                val paddingRight = 24f
                val paddingTop = 24f
                val paddingBottom = 60f

                val width = size.width - paddingLeft - paddingRight
                val height = size.height - paddingTop - paddingBottom

                val origin = Offset(paddingLeft, size.height - paddingBottom)
                val topY = paddingTop
                val rightX = size.width - paddingRight

                // Max value for scaling
                val maxAmount = max(
                    1f,
                    visibleStats.flatMap { listOf(it.income, it.expenses) }.maxOrNull() ?: 1f
                )

                // --- Draw axes ---

                // Y-axis
                drawLine(
                    color = Color.Black,
                    start = origin,
                    end = Offset(paddingLeft, topY),
                    strokeWidth = 4f
                )

                // X-axis
                drawLine(
                    color = Color.Black,
                    start = origin,
                    end = Offset(rightX, origin.y),
                    strokeWidth = 4f
                )

                // --- Optional: simple Y-axis ticks (0, max/2, max) ---
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }

                val nativeCanvas = drawContext.canvas.nativeCanvas
                val y0 = origin.y
                val yMid = origin.y - height / 2f
                val yMax = topY

                nativeCanvas.drawText("0", paddingLeft - 10f, y0 + 10f, textPaint)
                nativeCanvas.drawText(
                    (maxAmount / 2f).toInt().toString(),
                    paddingLeft - 10f,
                    yMid + 10f,
                    textPaint
                )
                nativeCanvas.drawText(
                    maxAmount.toInt().toString(),
                    paddingLeft - 10f,
                    yMax + 10f,
                    textPaint
                )

                // --- Calculate points ---

                val count = visibleStats.size
                val stepX = if (count > 1) width / (count - 1) else 0f

                fun toPoint(index: Int, value: Float): Offset {
                    val x = paddingLeft + stepX * index
                    val ratio = value / maxAmount
                    val y = origin.y - (height * ratio)
                    return Offset(x, y)
                }

                val incomePoints = visibleStats.mapIndexed { index, item ->
                    toPoint(index, item.income)
                }
                val expensePoints = visibleStats.mapIndexed { index, item ->
                    toPoint(index, item.expenses)
                }

                // --- Draw lines (Income = green, Expenses = red) ---

                // Income line
                for (i in 0 until incomePoints.size - 1) {
                    drawLine(
                        color = Color(0xFF388E3C), // green
                        start = incomePoints[i],
                        end = incomePoints[i + 1],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
                // Expenses line
                for (i in 0 until expensePoints.size - 1) {
                    drawLine(
                        color = Color(0xFFD32F2F), // red
                        start = expensePoints[i],
                        end = expensePoints[i + 1],
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // --- Draw points ---

                incomePoints.forEach { p ->
                    drawCircle(
                        color = Color(0xFF388E3C),
                        radius = 10f,
                        center = p,
                        style = Stroke(width = 4f)
                    )
                }
                expensePoints.forEach { p ->
                    drawCircle(
                        color = Color(0xFFD32F2F),
                        radius = 10f,
                        center = p,
                        style = Stroke(width = 4f)
                    )
                }

                // --- X-axis labels (month names) ---

                val textPaintX = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                visibleStats.forEachIndexed { index, item ->
                    val x = paddingLeft + stepX * index
                    val y = origin.y + 40f
                    nativeCanvas.drawText(item.monthLabel, x, y, textPaintX)
                }
            }
        }

        // 🔹 New hint text so user/marker knows about drag
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Drag left/right on the chart to scroll between months.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
