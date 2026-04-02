package com.kk.calorietracker.ui.meallog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kk.calorietracker.R
import com.kk.calorietracker.data.model.MeasurementUnit
import com.kk.calorietracker.ui.components.MacroRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class GroupedMealLog(
    val mealTypeName: String,
    val foodItemsSummary: String,
    val fatG: Double,
    val carbsG: Double,
    val proteinG: Double,
    val latestLogId: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: MealLogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val logs by viewModel.logsForDate.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val savedMsg = stringResource(R.string.meal_log_saved)
    val deletedMsg = stringResource(R.string.meal_log_deleted)
    val errorMsg = stringResource(R.string.error_fill_all_fields)
    val groupedLogs = remember(logs) {
        logs.groupBy { it.mealTypeId to it.mealTypeName }
            .values
            .map { mealLogs ->
                GroupedMealLog(
                    mealTypeName = mealLogs.first().mealTypeName,
                    foodItemsSummary = mealLogs.map { it.foodItemName }.distinct().joinToString(", "),
                    fatG = mealLogs.sumOf { it.fatG },
                    carbsG = mealLogs.sumOf { it.carbsG },
                    proteinG = mealLogs.sumOf { it.proteinG },
                    latestLogId = mealLogs.maxOf { it.id },
                )
            }
            .sortedByDescending { it.latestLogId }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MealLogEvent.ShowSnackbar -> {
                    if (event.message == savedMsg) {
                        focusManager.clearFocus(force = true)
                        keyboardController?.hide()
                    }
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toEpochDay() * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = viewModel::onDismissDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onDateChange(date)
                        } else {
                            viewModel.onDismissDatePicker()
                        }
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDatePicker) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))

            // Date picker
            OutlinedTextField(
                value = state.selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.date)) },
                trailingIcon = {
                    IconButton(onClick = viewModel::onShowDatePicker) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))

            // Meal type dropdown
            MealTypeDropdown(
                selected = state.selectedMealType?.name ?: "",
                options = state.mealTypes.map { it.name },
                onSelect = { name ->
                    state.mealTypes.find { it.name == name }?.let { viewModel.onMealTypeSelected(it) }
                },
            )
            Spacer(Modifier.height(8.dp))

            // Food item searchable dropdown
            FoodItemDropdown(
                query = state.foodSearchQuery,
                options = state.filteredFoodItems,
                onSelect = viewModel::onFoodItemSelected,
            )
            Spacer(Modifier.height(8.dp))

            // Quantity field
            val unitLabel = state.selectedFoodItem?.let {
                if (it.measurementUnit == MeasurementUnit.PER_GRAM) "g" else "ml"
            } ?: ""
            OutlinedTextField(
                value = state.quantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Text(stringResource(R.string.quantity_with_unit, unitLabel)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            Spacer(Modifier.height(8.dp))

            // Macro preview
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.macro_preview),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    MacroRow(
                        fatG = state.macroPreview.fatG,
                        carbsG = state.macroPreview.carbsG,
                        proteinG = state.macroPreview.proteinG,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onAddItemToMeal(errorMsg) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_food_to_meal))
            }

            if (state.mealDraftItems.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.meal_items),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        state.mealDraftItems.forEachIndexed { index, draftItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = draftItem.foodItemName,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = stringResource(R.string.logged_quantity_value, draftItem.quantity),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    MacroRow(
                                        fatG = draftItem.fatG,
                                        carbsG = draftItem.carbsG,
                                        proteinG = draftItem.proteinG,
                                    )
                                }
                                IconButton(onClick = { viewModel.onRemoveMealDraftItem(index) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete),
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                            if (index != state.mealDraftItems.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.meal_total_preview),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        MacroRow(
                            fatG = state.mealDraftTotalPreview.fatG,
                            carbsG = state.mealDraftTotalPreview.carbsG,
                            proteinG = state.mealDraftTotalPreview.proteinG,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.onSave(savedMsg, errorMsg) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_meal))
            }

            Spacer(Modifier.height(16.dp))

            if (logs.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.todays_logs),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
            } else {
                Text(
                    text = stringResource(R.string.meal_logs_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }

        items(groupedLogs, key = { it.latestLogId }) { groupedLog ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = groupedLog.mealTypeName,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = groupedLog.foodItemsSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    MacroRow(
                        fatG = groupedLog.fatG,
                        carbsG = groupedLog.carbsG,
                        proteinG = groupedLog.proteinG,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.meal_type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onSelect(name); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodItemDropdown(
    query: String,
    options: List<com.kk.calorietracker.data.model.FoodItem>,
    onSelect: (com.kk.calorietracker.data.model.FoodItem) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && options.isNotEmpty(),
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.food_item)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
        )
        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = { onSelect(item); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
