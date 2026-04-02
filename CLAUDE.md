# CalorieTracker — Claude Code Instructions

## Project overview
A local-first Android calorie tracker with food item management, meal logging,
macro auto-population, daily target tracking, and trend visualisation.

## Tech stack
- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Architecture: MVVM + Repository pattern
- DI: Hilt
- Local DB: Room (SQLite) — no remote/cloud sync
- Navigation: Navigation Compose with typed routes
- Charts: Vico (for trends screen)
- Async: Kotlin Coroutines + StateFlow

## Module structure
app/
├── data/
│   ├── db/              # Room database, entities, DAOs
│   ├── model/           # Domain models (FoodItem, MealType, MealLog, DailyTarget)
│   └── repository/      # FoodRepository, MealLogRepository, TargetsRepository
├── ui/
│   ├── food/            # FoodItemScreen + FoodViewModel
│   ├── mealtype/        # MealTypeScreen + MealTypeViewModel
│   ├── meallog/         # MealLogScreen + MealLogViewModel
│   ├── trends/          # TrendsScreen + TrendsViewModel
│   ├── targets/         # TargetsScreen + TargetsViewModel
│   ├── components/      # Shared composables (MacroRow, ChipSelector, ProgressMacroRow, etc.)
│   └── theme/           # MaterialTheme, Typography, Color
└── di/                  # Hilt modules

## Data models

### FoodItem
- id: Long (PK, autoGenerate)
- name: String
- measurementUnit: Enum → PER_GRAM | PER_ML
- measurementQuantity: Double  (e.g. 100 for "per 100g")
- fatG: Double
- carbsG: Double
- proteinG: Double

### MealType
- id: Long (PK, autoGenerate)
- name: String (e.g. "Breakfast", "Pre-workout")

### MealLog
- id: Long (PK, autoGenerate)
- date: LocalDate (stored as epoch day Long in Room)
- mealTypeId: Long (FK → MealType)
- foodItemId: Long (FK → FoodItem)
- quantity: Double  (how many grams/ml the user consumed)
- fatG, carbsG, proteinG: Double
  ← auto-calculated from FoodItem macros × (quantity / measurementQuantity)
  ← store denormalised so historical logs don't change if FoodItem is edited later

### DailyTarget
- id: Long (PK, autoGenerate) — only one row ever used (id = 1)
- calories: Double
- fatG: Double
- carbsG: Double
- proteinG: Double

## Macro auto-population rule
When saving a MealLog, calculate macros as:
  macro = foodItem.macroG * (quantity / foodItem.measurementQuantity)
Store the computed values on MealLog directly. Do not query FoodItem at read time.
This ensures historical logs are immutable if a FoodItem is later edited.

## Calorie calculation
  calories = (fatG × 9) + (carbsG × 4) + (proteinG × 4)
Always compute this in the ViewModel or Repository, never in a Composable.

## Screen-by-screen requirements

### Food Item screen
- Form: name (text), measurementUnit (segmented button: g / ml),
  measurementQuantity (numeric), fat, carbs, protein (all required)
- List of all saved food items below the form
- Tap to edit, swipe to delete with confirmation dialog

### Meal Type screen
- Simple form: name field + Save button
- List of all meal types below the form
- Swipe to delete with confirmation dialog

### Log Meal screen
- Date picker (defaults to today)
- Meal type selector (dropdown from MealType table)
- Food item selector (searchable dropdown from FoodItem table)
- Quantity field (numeric, with unit shown from the selected FoodItem)
- Read-only macro preview that updates live as quantity changes
- Save button — persists MealLog with pre-calculated macros

### Targets screen
- Accessible from a settings/profile icon in the top app bar (present on all screens)
- Single form: calories, fat, carbs, protein (all required)
- Upserts a single row (id = 1) — there is only ever one active target
- Pre-populates all fields with the currently saved values on open
- Save button with Snackbar confirmation

### Trends screen
- Tab row: Day | Week | Month

#### Day view
- Bar chart of calories per meal type for the selected day
- Macro breakdown (fat, carbs, protein totals)
- "vs. target" section below the breakdown:
    - Load DailyTarget (id = 1); if none saved, show an inline prompt with a
      button that navigates to Targets screen
    - For each macro and for total calories, show:
        - A labelled LinearProgressIndicator (0–100% of target, capped at 100% visually)
        - Numeric label: e.g. "74g / 120g  (62%)"
        - Colour: M3 `primary` up to 100% of target; switch to `error` if exceeded
    - Progress indicators update reactively from the same TrendsViewModel StateFlow

#### Week view
- Line chart of daily total calories for the last 7 days (Vico)

#### Month view
- Line chart of daily total calories for the last 30 days (Vico)

## Code conventions
- All ViewModels: extend ViewModel(), injected via @HiltViewModel
- UI state: sealed class or data class exposed as StateFlow<UiState>
- Side effects (navigation, snackbars): use Channel<Event> + collectAsEffect()
- No business logic in Composables — all logic in ViewModel or Repository
- Room type converters required for: LocalDate (store as epoch day Long), Enum fields
- Use @Transaction for any multi-table writes
- DailyTarget fetched as Flow<DailyTarget?> — nullable so the UI can handle
  the "no target set yet" empty state without crashing
- TargetsViewModel uses upsert (INSERT OR REPLACE) — never plain INSERT
- Unit test all Repository methods and ViewModel state transitions
- Instrumented tests for all Room DAOs
- No hardcoded strings — use strings.xml

## UI conventions
- Material 3 throughout — use M3 components only, no M2 fallbacks
- Dynamic colour: enabled
- Typography: follow M3 type scale, do not define custom sizes
- No hardcoded colours — use MaterialTheme.colorScheme tokens only
- All screens support light and dark mode
- Responsive: designed for phone portrait; must not break in landscape
- OutlinedTextField for all text inputs
- FilterChip or SegmentedButton for discrete selections
- FAB for primary create actions on list screens
- Snackbar for all success/error feedback — no Toast
- Empty states: show a helpful message (and link where relevant) when lists are empty

## What to avoid
- Do not use LiveData — StateFlow/SharedFlow only
- Do not use XML layouts or Fragments
- Do not add cloud sync, login, or user accounts — local only
- Do not add analytics, tracking, or crash reporting libraries
- Do not use RxJava
- Do not use MPAndroidChart or any chart library other than Vico
- Do not allow multiple DailyTarget rows — always upsert id = 1
- Do not compute calorie totals in Composables — always in ViewModel or Repository
- Do not add any dependency without checking first
