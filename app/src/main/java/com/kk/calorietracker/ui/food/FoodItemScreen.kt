package com.kk.calorietracker.ui.food

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kk.calorietracker.R
import com.kk.calorietracker.data.model.MeasurementUnit
import com.kk.calorietracker.ui.components.MacroRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val items by viewModel.items.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val savedMsg = stringResource(R.string.food_item_saved)
    val deletedMsg = stringResource(R.string.food_item_deleted)
    val errorMsg = stringResource(R.string.error_fill_all_fields)
    val deleteBlockedMsg = stringResource(R.string.delete_food_item_blocked)
    val deleteFailedMsg = stringResource(R.string.delete_failed_try_again)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FoodEvent.ShowSnackbar -> {
                    if (event.message == savedMsg) {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    }
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    state.showDeleteConfirm?.let { item ->
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_food_item_confirm, item.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteConfirm(item, deletedMsg, deleteBlockedMsg, deleteFailedMsg)
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.food_item_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.measurement_unit),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                MeasurementUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = state.measurementUnit == unit,
                        onClick = { viewModel.onUnitChange(unit) },
                        shape = SegmentedButtonDefaults.itemShape(index, MeasurementUnit.entries.size),
                        label = {
                            Text(
                                if (unit == MeasurementUnit.PER_GRAM)
                                    stringResource(R.string.unit_per_gram)
                                else
                                    stringResource(R.string.unit_per_ml)
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.measurementQuantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Text(stringResource(R.string.measurement_quantity)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.fatG,
                    onValueChange = viewModel::onFatChange,
                    label = { Text(stringResource(R.string.fat_g)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = state.carbsG,
                    onValueChange = viewModel::onCarbsChange,
                    label = { Text(stringResource(R.string.carbs_g)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                OutlinedTextField(
                    value = state.proteinG,
                    onValueChange = viewModel::onProteinChange,
                    label = { Text(stringResource(R.string.protein_g)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                androidx.compose.material3.Button(
                    onClick = { viewModel.onSave(savedMsg, errorMsg) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        if (state.editingId != null)
                            stringResource(R.string.update)
                        else
                            stringResource(R.string.save)
                    )
                }
                if (state.editingId != null) {
                    TextButton(onClick = viewModel::onClearForm) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text(
                    text = stringResource(R.string.food_items_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }

        items(items, key = { it.id }) { item ->
            val isInUse = state.referencedFoodItemIds.contains(item.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onEditItem(item) },
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(
                                    R.string.per_quantity_unit,
                                    item.measurementQuantity.toInt(),
                                    if (item.measurementUnit == MeasurementUnit.PER_GRAM) "g" else "ml",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (isInUse) {
                                Text(
                                    text = stringResource(R.string.item_in_use_cannot_delete),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onDeleteRequest(item) }, enabled = !isInUse) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = if (isInUse) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    MacroRow(fatG = item.fatG, carbsG = item.carbsG, proteinG = item.proteinG)
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
