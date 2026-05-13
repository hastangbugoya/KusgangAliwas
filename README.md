# Kusgang Aliwas

Kusgang Aliwas is a simple gym planning and workout logging app for Android.

The app is built for people who want a practical way to plan gym sessions, track what they actually did, and see basic progress over time without turning the workout into paperwork. It is not meant to force a rigid plan. It suggests a roadmap, lets the user adjust in the gym, and keeps a record of the work that was actually performed.

## What the app does

Kusgang Aliwas helps you:

- Create exercises such as Bench Press, Lat Pull, Treadmill, Bike, or other gym activities.
- Group exercises into split templates.
- Schedule simple gym plans across weeks.
- Start a quick session or start from a saved split.
- Log strength exercises with sets, weight, and reps.
- Log cardio with distance, duration, and optional notes.
- Reuse previous workout values when helpful.
- Estimate cardio distance from duration and intensity when distance is unknown.
- See weekly progress for strength and cardio.
- See calendar markers for completed, missed, partial, and current workout days.

The app is designed around real gym behavior. If a machine is busy, you can deviate. If you did something unplanned, you can log it. If you only want a quick session, you can start one without building a full program first.

## Basic usage

### 1. Add exercises

Go to the Exercises tab and add the exercises you want to track.

Each exercise can be marked as:

- Strength
- Cardio
- Mobility

Examples:

- Bench Press — Strength
- Lat Pull — Strength
- Treadmill — Cardio
- Stationary Bike — Cardio
- Stretching — Mobility

### 2. Create a split

A split is a reusable workout roadmap.

Example:

```text
Push Day
1. Bench Press
2. Shoulder Press
3. Triceps Pushdown
```

Another example:

```text
Cardio Day
1. Treadmill
2. Bike
```

The split does not need to store exact weight or cardio targets. The app can pull suggestions from your recent logs when you start a session.

### 3. Start a session

You can start:

- a quick session
- a saved split
- a planned session from the calendar

When a saved split is started, the app creates the workout structure but does not automatically create completed work. This prevents fake progress. You still need to add sets or enter cardio data to count actual work.

### 4. Log strength work

For strength exercises, expand the exercise and tap **Add set**.

The first set can be pre-filled from your previous best or most recent useful value. You can change it before saving.

Example:

```text
Bench Press
Set 1: 135 lb × 10 reps
Set 2: 135 lb × 8 reps
```

The app uses logged sets for weekly strength volume.

### 5. Log cardio work

For cardio exercises, enter what you know.

Examples:

```text
Treadmill
Distance: 2 mi
Duration: 20 min
```

If you know the duration but not the distance, choose an intensity level from 1 to 5:

```text
1 = slow walk
2 = walk
3 = brisk walk
4 = jog
5 = run
```

The app estimates distance from duration and intensity. Estimated distances are marked so they are not confused with measured distances.

### 6. View progress

The Calendar tab shows:

- day markers for planned/logged workout days
- a weekly progress graph
- strength volume
- cardio distance

Strength progress uses:

```text
weight × reps
```

Cardio progress uses:

```text
distance
```

If distance was estimated, it can still help fill the weekly graph while remaining labeled as an approximation.

## Feature summary

### Exercise library

- Add strength, cardio, and mobility exercises.
- Keep exercises active/inactive instead of deleting useful history.
- Support future exercise metadata such as muscle groups, substitutions, notes, and reference URLs.

### Split templates

- Build reusable session roadmaps.
- Keep order suggestions without enforcing them.
- Allow split names to be renamed without breaking relationships.
- Avoid storing required weight or cardio targets in the split itself.

### Session logging

- Start quick sessions.
- Start saved splits.
- Add strength exercises.
- Add cardio items.
- Reorder session items.
- Remove empty exercises.
- Delete sessions when needed.
- Rate sessions.

### Strength logging

- Add sets manually.
- Log weight and reps.
- Duplicate sets.
- Delete sets.
- Show previous max support text.
- Prefill first set from previous data only when the user taps Add set.

### Cardio logging

- Log distance.
- Log duration.
- Log notes.
- Reuse previous cardio data with a Use button.
- Estimate distance from duration and intensity.
- Mark estimated distances separately from directly entered distances.

### Calendar and weekly progress

- Locale-aware week start.
- Current week awareness.
- Today highlighting.
- Calendar status markers.
- Weekly strength and cardio graph.
- Strength volume calculation.
- Cardio distance aggregation.

## Technical overview

Kusgang Aliwas is a native Android app built with Kotlin and Jetpack Compose. It uses a local-first architecture with Room as the source of truth, Hilt for dependency injection, Flow/StateFlow for reactive UI state, and domain/use-case classes for business rules.

The app follows a domain-first pattern where persistent database entities are kept separate from UI state models and higher-level app behavior. The goal is to keep the logic testable, incremental, and easy to refactor as the app grows.

## Tech stack

### Language and platform

- Kotlin
- Android
- Jetpack Compose
- Material 3

### Persistence

- Room
- SQLite
- Room migrations
- Exported schema enabled

### Dependency injection

- Hilt
- Constructor injection for repositories and use cases
- Hilt ViewModels

### Reactive state

- Kotlin Flow
- StateFlow
- `combine`
- `flatMapLatest`
- `stateIn`
- `SharingStarted.WhileSubscribed`

### Architecture style

- Local-first persistence
- Repository interfaces in the domain layer
- Room-backed repository implementations in the data layer
- Use cases for app behavior
- Compose screens driven by immutable UI state
- Simple, incremental feature development

## Architecture

The app is organized around a layered structure:

```text
data/
  local/
    dao/
    db/
    entity/
    model/
  repository/

domain/
  model/
  repository/
  usecase/

ui/
  calendar/
  common/
  exercise/
  session/
  split/
```

### Data layer

The data layer owns Room entities, DAOs, database configuration, and repository implementations.

Key responsibilities:

- Store exercises.
- Store split templates.
- Store planned sessions.
- Store actual sessions.
- Store strength exercise logs.
- Store strength set logs.
- Store cardio logs.
- Provide query methods to the repository implementation.
- Handle migrations.

Important Room entities include:

- `ExerciseEntity`
- `SplitTemplateEntity`
- `SplitTemplateExerciseEntity`
- `PlannedSessionEntity`
- `PlannedSessionExerciseEntity`
- `ActualSessionEntity`
- `ActualExerciseLogEntity`
- `ActualExerciseSetLogEntity`
- `ActualCardioLogEntity`
- `ProgramEntity`
- `SplitScheduleEntity`

### Domain layer

The domain layer defines repository boundaries, domain models, and use cases.

The domain layer is where app rules belong.

Examples:

- Creating a quick session.
- Creating a session from a saved split.
- Building weekly training progress.
- Adding an exercise log to a session.
- Calculating suggested values from previous logs.

The domain layer should not depend on Compose.

### UI layer

The UI layer uses Compose screens and ViewModels.

ViewModels collect repository flows and use cases, then expose UI state objects.

Examples:

- `CalendarViewModel`
- `SessionDetailViewModel`
- `SplitRoadmapViewModel`
- `ExerciseListRoute`
- `SessionDayViewModel`

Screens are intentionally plain and functional. The current priority is correctness, data flow, and useful behavior over visual polish.

## Core data model concepts

### Exercise

An exercise is a reusable activity that can appear in sessions or splits.

It can be classified by type:

```text
STRENGTH
CARDIO
MOBILITY
```

This allows the UI and logging behavior to differ by exercise type.

Strength exercises use sets.

Cardio exercises use distance, duration, incline, resistance, or notes.

### Split template

A split template is a reusable workout plan.

It stores a list of exercises and their order.

The app intentionally avoids relying on planned weights, planned sets, or planned cardio metrics in the split. Instead, the split acts as a roadmap, while actual logging values come from user input or previous logs.

This keeps planning light and avoids creating fake completed workout data.

### Actual session

An actual session represents a workout session that exists on a specific day.

It may come from:

- a quick session
- a saved split
- a planned calendar session

The session owns both strength logs and cardio logs.

### Strength exercise log

An actual strength exercise log represents one exercise performed or queued inside a session.

It can have zero or more set logs.

A strength exercise with no sets means the exercise is present in the session but no actual set work has been logged yet.

### Strength set log

A set log stores:

- set order
- weight
- reps
- notes

Weekly strength volume is calculated from completed set logs:

```text
strength volume = weight × reps
```

Across a day or week:

```text
total strength volume = sum(weight × reps)
```

### Cardio log

A cardio log stores:

- exercise reference
- display name
- distance
- distance unit
- duration
- incline
- resistance
- notes
- estimated distance flag
- intensity level

A cardio log can exist with no actual values yet. This allows a split to create the cardio row without pretending the user already completed cardio work.

### Estimated cardio distance

If the user enters duration but not distance, the app can estimate distance from an intensity level.

Suggested intensity scale:

```text
1 = slow walk
2 = walk
3 = brisk walk
4 = jog
5 = run
```

Approximate speeds:

```text
1 -> 2.0 mph
2 -> 3.0 mph
3 -> 4.0 mph
4 -> 5.5 mph
5 -> 7.0 mph
```

Formula:

```text
estimated distance = speed × time
```

The app marks these distances with:

```kotlin
isEstimatedDistance = true
```

This lets analytics use the value while still preserving that it was approximated.

## Session behavior rules

### Starting a saved split

When a saved split is started:

- Create an actual session.
- Create strength/cardio rows based on the split.
- Do not automatically create strength sets.
- Do not automatically fill cardio distance or duration.
- Show previous values only as support text.
- Let the user explicitly apply previous cardio values with a Use button.
- Let the user create the first strength set by tapping Add set.

This avoids false completion.

### Strength suggestions

For strength exercises:

- Previous max or useful recent values are shown as support text.
- First set creation can use previous values.
- The user must tap Add set.
- A set counts as work only when a set row exists with logged values.

### Cardio suggestions

For cardio exercises:

- Previous cardio values are shown as support text.
- The Use button copies previous values into the current cardio log.
- Entering duration plus intensity can estimate distance.
- Estimated distance is visibly labeled.

### Completion philosophy

The app is meant to encourage consistency, not enforce rigid adherence.

A day can be considered successful when actual logged sessions satisfy or exceed planned sessions. The user does not have to perform the exact originally planned split for the day to count as successful.

## Calendar logic

The calendar uses planned sessions and actual sessions to determine day status.

Status meanings:

```text
GREEN:
logged >= planned

YELLOW:
planned > logged
logged > 0

RED:
planned > 0
logged == 0

NEUTRAL:
planned == 0
logged == 0

TODAY:
current day with no logged session yet
```

Future days stay neutral so the app does not punish the user for days that have not happened yet.

The current day can also be visually highlighted independently of completion state.

## Locale-aware weeks

The app uses locale settings to determine the first day of the week.

Relevant API:

```kotlin
WeekFields.of(Locale.getDefault()).firstDayOfWeek
```

This affects:

- calendar weekday headers
- visible month cell alignment
- weekly progress aggregation
- weekly graph labels

## Weekly progress

The weekly progress card summarizes current week activity.

Strength metric:

```text
sum(weight × reps)
```

Cardio metric:

```text
sum(distance)
```

The graph is intentionally simple:

- 7 columns
- one column per weekday
- strength row
- cardio row
- values normalized against the largest value in the current week

This avoids external chart dependencies and keeps the UI easy to replace later.

## Database

The app uses Room with explicit migrations and exported schema enabled.

Current database includes tables for:

- exercise library
- muscle groups
- exercise-muscle mappings
- substitutions
- split templates
- split template exercises
- training cycles
- planned sessions
- actual sessions
- actual exercise logs
- actual set logs
- cardio logs
- programs
- split schedules

### Migration style

Migrations are intentionally small and incremental.

Recent migration example:

```sql
ALTER TABLE actual_cardio_log
ADD COLUMN isEstimatedDistance INTEGER NOT NULL DEFAULT 0;

ALTER TABLE actual_cardio_log
ADD COLUMN intensityLevel INTEGER;
```

This approach keeps schema evolution safer and easier to test.

## Dependency injection

Hilt provides:

- Room database
- DAOs
- repository implementations
- use cases
- ViewModels

The repository interface lives in the domain layer, while the Room implementation lives in the data layer.

Example pattern:

```text
SessionRepository
SessionRepositoryImpl
```

## Repository pattern

The app uses repository interfaces as boundaries between domain/UI logic and Room.

Example responsibilities of `SessionRepository`:

- Observe planned sessions.
- Observe actual sessions.
- Insert/update/delete sessions.
- Read exercise logs.
- Read cardio logs.
- Read set logs.
- Fetch previous strength suggestions.
- Fetch previous cardio suggestions.

This keeps database details out of ViewModels and use cases.

## ViewModel pattern

ViewModels expose immutable UI state.

Example:

```text
Room Flow + use case data
        ↓
ViewModel mapping
        ↓
UiState
        ↓
Compose screen
```

The UI does not directly query DAOs.

## Compose UI pattern

Screens are built with Compose and simple reusable components.

Common components include:

- `KusgangTopBar`
- `SharpCard`
- section headers
- calendar day cells
- session cards
- input rows

The UI is intentionally simple and modular so a future design system can replace the current styling without rewriting business logic.

## Why this app exists

Many gym apps assume the user follows a perfect plan. Real gym sessions are messier.

Equipment is busy. Energy changes. Time runs out. Sometimes the user wants to log just enough to remember what happened.

Kusgang Aliwas is built around that reality:

```text
Plan lightly.
Log honestly.
Track progress simply.
Adjust as needed.
```

## Current status

This app is under active development.

Implemented foundations include:

- exercise library
- split planning
- quick sessions
- saved split sessions
- strength logging
- cardio logging
- previous-value suggestions
- estimated cardio distance
- calendar markers
- weekly progress graph
- Room migrations

Planned future improvements may include:

- per-exercise trend screens
- better graph styling
- muscle group summaries
- exercise substitutions
- richer program planning
- workout history drill-down
- wearable/GPS imports
- better accessibility and gym-mode controls
