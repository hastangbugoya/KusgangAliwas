package com.example.kusgangaliwas.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * Standard section header text used inside screens/cards.
 */
@Composable
fun SectionHeader(
    text: String,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
    )
}