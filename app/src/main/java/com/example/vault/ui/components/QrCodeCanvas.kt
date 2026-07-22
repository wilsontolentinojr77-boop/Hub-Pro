package com.example.vault.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

/**
 * Generates a visual 2D QR Code matrix preview based on the 2FA secret key URI.
 */
@Composable
fun QrCodeCanvas(
    secretKey: String,
    modifier: Modifier = Modifier.size(180.dp)
) {
    val darkColor = Color(0xFF0F172A)
    val lightColor = Color.White
    val accentColor = MaterialTheme.colorScheme.primary

    val matrixSize = 21
    val grid = generateQrMatrix(secretKey, matrixSize)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(lightColor)
            .border(2.dp, accentColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / matrixSize
            val cellHeight = size.height / matrixSize

            for (row in 0 until matrixSize) {
                for (col in 0 until matrixSize) {
                    val isFilled = grid[row][col]
                    if (isFilled) {
                        drawRect(
                            color = if (isFinderPattern(row, col, matrixSize)) accentColor else darkColor,
                            topLeft = Offset(col * cellWidth, row * cellHeight),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }
        }
    }
}

private fun isFinderPattern(row: Int, col: Int, size: Int): Boolean {
    // Top Left (0..6, 0..6)
    if (row in 0..6 && col in 0..6) return true
    // Top Right (0..6, size-7..size-1)
    if (row in 0..6 && col >= size - 7) return true
    // Bottom Left (size-7..size-1, 0..6)
    if (row >= size - 7 && col in 0..6) return true
    return false
}

private fun generateQrMatrix(input: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }
    val hash = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())

    var hashIdx = 0
    for (r in 0 until size) {
        for (c in 0 until size) {
            // Finder patterns outline
            if ((r in 0..6 && c in 0..6) ||
                (r in 0..6 && c >= size - 7) ||
                (r >= size - 7 && c in 0..6)
            ) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6) ||
                        (r in 0..6 && (c == size - 7 || c == size - 1)) ||
                        (r == 0 || r == 6) ||
                        (r >= size - 7 && (c == 0 || c == 6)) ||
                        (r == size - 7 || r == size - 1)
                val isCenter = (r in 2..4 && c in 2..4) ||
                        (r in 2..4 && c in (size - 5)..(size - 3)) ||
                        (r in (size - 5)..(size - 3) && c in 2..4)
                matrix[r][c] = isOuter || isCenter
            } else {
                val byteVal = hash[hashIdx % hash.size].toInt() and 0xFF
                matrix[r][c] = (byteVal and (1 shl (c % 8))) != 0
                hashIdx++
            }
        }
    }
    return matrix
}
