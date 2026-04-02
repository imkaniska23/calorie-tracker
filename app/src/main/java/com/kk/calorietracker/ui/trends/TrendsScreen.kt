package com.kk.calorietracker.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kk.calorietracker.R
import com.kk.calorietracker.ui.components.MacroRow
import com.kk.calorietracker.ui.components.ProgressMacroRow
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun TrendsScreen(
    onNavigateToTargets: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val tabs = TrendsTab.entries

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabs.indexOf(state.selectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    text = {
                        Text(
                            when (tab) {
                                TrendsTab.DAY -> stringResource(R.string.trends_tab_day)
                                TrendsTab.WEEK -> stringResource(R.string.trends_tab_week)
                                TrendsTab.MONTH -> stringResource(R.string.trends_tab_month)
                            }
                        )
                    }
                )
            }
        }

        when (state.selectedTab) {
            TrendsTab.DAY -> DayView(state, onNavigateToTargets)
            TrendsTab.WEEK -> RangeView(
                title = stringResource(R.string.trends_week_title),
                entries = state.weeklyCalories,
            )
            TrendsTab.MONTH -> RangeView(
                title = stringResource(R.string.trends_month_title),
                entries = state.monthlyCalories,
            )
        }
    }
}

@Composable
private fun DayView(
    state: TrendsUiState,
    onNavigateToTargets: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Bar chart: calories per meal type
        if (state.mealTypeCalories.isNotEmpty()) {
            Text(
                text = stringResource(R.string.calories_by_meal_type),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(8.dp))

            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(state.mealTypeCalories) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(state.mealTypeCalories.map { it.calories.toFloat() })
                    }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )
            Spacer(Modifier.height(16.dp))
        }

        // Macro totals
        Text(
            text = stringResource(R.string.macro_totals),
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(4.dp))
        MacroRow(
            fatG = state.macroTotals.fatG,
            carbsG = state.macroTotals.carbsG,
            proteinG = state.macroTotals.proteinG,
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // vs target
        Text(
            text = stringResource(R.string.vs_target),
            style = MaterialTheme.typography.titleSmall,
        )
        Spacer(Modifier.height(8.dp))

        val target = state.dailyTarget
        if (target == null) {
            Text(
                text = stringResource(R.string.no_target_set),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onNavigateToTargets) {
                Text(stringResource(R.string.set_targets))
            }
        } else {
            ProgressMacroRow(
                label = stringResource(R.string.calories),
                current = state.macroTotals.calories,
                target = target.calories,
                unit = stringResource(R.string.unit_kcal),
            )
            ProgressMacroRow(
                label = stringResource(R.string.fat),
                current = state.macroTotals.fatG,
                target = target.fatG,
                unit = stringResource(R.string.unit_g),
            )
            ProgressMacroRow(
                label = stringResource(R.string.carbs),
                current = state.macroTotals.carbsG,
                target = target.carbsG,
                unit = stringResource(R.string.unit_g),
            )
            ProgressMacroRow(
                label = stringResource(R.string.protein),
                current = state.macroTotals.proteinG,
                target = target.proteinG,
                unit = stringResource(R.string.unit_g),
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun RangeView(
    title: String,
    entries: List<DailyCaloriesEntry>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.no_data_for_period),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val modelProducer = remember(title) { CartesianChartModelProducer() }
            LaunchedEffect(entries) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(entries.map { it.calories.toFloat() })
                    }
                }
            }
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}
