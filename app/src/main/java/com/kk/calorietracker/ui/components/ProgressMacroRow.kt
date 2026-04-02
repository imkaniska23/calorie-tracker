package com.kk.calorietracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressMacroRow(
    label: String,
    current: Double,
    target: Double,
    unit: String,
    modifier: Modifier = Modifier,
) {
    val progress = if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
    val exceeded = target > 0 && current > target
    val percent = if (target > 0) ((current / target) * 100).toInt() else 0
    val color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(80.dp),
            )
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${current.toInt()}$unit / ${target.toInt()}$unit  ($percent%)",
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
