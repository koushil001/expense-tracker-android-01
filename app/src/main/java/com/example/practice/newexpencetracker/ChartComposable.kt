//package com.example.practice.newexpencetracker
//
//import androidx.compose.runtime.Composable
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.unit.dp
//import kotlin.math.ceil
//import kotlin.math.max
//import kotlin.math.roundToInt
//import androidx.compose.ui.graphics.nativeCanvas
//
//data class MonthStat(
//    val label: String,    // e.g., "Aug"
//    val income: Float,
//    val expenses: Float
//)
//
//@Composable
//fun IncomeExpensesChart(
//    stats: List<MonthStat>,
//    modifier: Modifier = Modifier
//) {
//    Column(modifier.padding(8.dp)) {
//        Text("Income/Expenses", style = MaterialTheme.typography.titleMedium)
//
//        Canvas(
//            Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)      // REQUIRED: square
//                .padding(top = 8.dp)
//        ) {
//            // We'll implement Steps 3–6 here
//            val leftPad = 56.dp.toPx()
//            val rightPad = 16.dp.toPx()
//            val topPad = 8.dp.toPx()
//            val bottomPad = 36.dp.toPx()
//
//            val plotLeft = leftPad
//            val plotRight = size.width - rightPad
//            val plotTop = topPad
//            val plotBottom = size.height - bottomPad
//            val plotWidth = plotRight - plotLeft
//            val plotHeight = plotBottom - plotTop
//
//// Use the last up-to-4 items (assignment wants ≤ 4 months on X)
//            val points = if (stats.size <= 4) stats else stats.takeLast(4)
//
//// yMax that “fits” both income and expenses (Task 12-ready)
//            val maxVal = points.maxOfOrNull { max(it.income, it.expenses) } ?: 0f
//            val step = when {
//                maxVal <= 0f -> 100f
//                maxVal < 500 -> 100f
//                maxVal < 2000 -> 500f
//                else -> 1000f
//            }
//            val yMax = max(1f, ceil(maxVal / step) * step)
//
//
//            // Axes
//            drawLine(Color.Gray, Offset(plotLeft, plotTop), Offset(plotLeft, plotBottom), 2f)
//            drawLine(Color.Gray, Offset(plotLeft, plotBottom), Offset(plotRight, plotBottom), 2f)
//
//// Y ticks (5)
//            val yTicks = 5
//            val labelPaint = android.graphics.Paint().apply {
//                color = android.graphics.Color.DKGRAY
//                textSize = 28f
//                isAntiAlias = true
//            }
//            for (i in 0..yTicks) {
//                val frac = i / yTicks.toFloat()
//                val y = plotBottom - frac * plotHeight
//                val value = (frac * yMax).roundToInt()
//                // grid line
//                drawLine(Color(0xFFE0E0E0), Offset(plotLeft, y), Offset(plotRight, y), 1f)
//                // label
//                drawContext.canvas.nativeCanvas.drawText(value.toString(), 8f, y + 8f, labelPaint)
//            }
//
//// X labels (≤ 4)
//            val xCount = points.size.coerceAtLeast(1)
//            val xLabelPaint = android.graphics.Paint().apply {
//                color = android.graphics.Color.DKGRAY
//                textSize = 28f
//                isAntiAlias = true
//                textAlign = android.graphics.Paint.Align.CENTER
//            }
//            for (i in 0 until xCount) {
//                val x = plotLeft + i * (plotWidth / (xCount - 1).coerceAtLeast(1))
//                drawContext.canvas.nativeCanvas.drawText(points[i].label, x, size.height - 8f, xLabelPaint)
//            }
//
//            // === Task 11: Single series (e.g., income) ===
//            val singlePath = Path()
//            points.forEachIndexed { i, item ->
//                val x = plotLeft + i * (plotWidth / (xCount - 1).coerceAtLeast(1))
//                val y = plotBottom - (item.income / yMax) * plotHeight
//                if (i == 0) singlePath.moveTo(x, y) else singlePath.lineTo(x, y)
//            }
//            drawPath(singlePath, color = Color(0xFF1E88E5), style = Stroke(width = 4f))
//
//
//
//        }
//    }
//}
//
//
//
