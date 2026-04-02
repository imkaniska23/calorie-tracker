package com.kk.calorietracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kk.calorietracker.R

@Composable
fun MacroRow(
    fatG: Double,
    carbsG: Double,
    proteinG: Double,
    modifier: Modifier = Modifier,
) {
    val calories = (fatG * 9) + (carbsG * 4) + (proteinG * 4)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.macro_calories_value, calories),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.macro_fat_value, fatG),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.macro_carbs_value, carbsG),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.macro_protein_value, proteinG),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
