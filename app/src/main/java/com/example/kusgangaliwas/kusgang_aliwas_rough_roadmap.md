# Kusgang Aliwas – Rough Planning Roadmap

## Working Job Statement

**Kusgang Aliwas helps the user plan a gym session, follow it flexibly, log what actually happened, and remember which training split or body-area focus was last covered.**

The app is not a strict workout enforcer. It is a practical in-gym roadmap and memory aid.

It should answer:

- What was I planning to train today?
- What exercises were suggested?
- What can I do instead if equipment is unavailable?
- What did I actually perform?
- What split/body area did I last cover?
- Is rest recommended before repeating this split?

---

## App Boundaries

### Relationship to Existing Apps

- **AdobongKangkong (AK):** Nutrition, meals, food logging, nutrients.
- **HastangHubaga (HH):** Broader health timeline, reminders, actions, daily planning.
- **Kusgang Aliwas:** Training plans, gym sessions, exercise roadmaps, flexible exercise logs, split/cycle memory.

### Integration Posture

V1 should stand alone, but avoid blocking future HH integration.

Future HH integration could expose:

- planned gym session summary
- completed gym session log
- rest-day recommendation
- last-covered split/body focus

Do not design v1 as if HH integration is required immediately.

---

## V1 Scope

### In Scope

1. Exercise library
   - name
   - category/body area
   - optional notes
   - active/inactive flag

2. Exercise substitutions/equivalences
   - user-defined alternatives
   - example: Bench Press → Dumbbell Press, Chest Press Machine
   - substitutions are suggestions, not automatic replacements

3. Split/program templates
   - example: Push / Pull / Legs
   - example: Chest + Triceps
   - suggested days between repeats
   - notes

4. One-session roadmap
   - planned session based on a split/template
   - ordered list of suggested exercises
   - order is advisory only

5. Flexible session execution
   - start a session from a roadmap
   - mark planned exercises as performed, skipped, substituted, or not done yet
   - add impromptu exercises during session
   - reorder actual execution naturally through log order

6. Planned vs actual logging
   - planned exercise remains as the roadmap intent
   - actual exercise records what happened
   - actual may reference the planned item it came from
   - actual may be a substitution or impromptu exercise

7. Split/cycle memory
   - track when each split was last covered
   - show recent session history
   - show what has already been trained in the current cycle/week

8. Gentle rest-day suggestions
   - based on suggested days between split repeats
   - wording should be advisory, not punitive
   - example: “Rest recommended before repeating Push”
   - example: “Push last covered 1 day ago”

9. Basic notes
   - split notes
   - exercise notes
   - session notes
   - actual log notes

---

## Explicitly Out of Scope for V1

- Nutrition features
- Calories, macros, nutrients
- Social sharing or leaderboards
- AI coaching
- Automatic progression engine
- Strength analytics dashboards
- Personal records system
- Wearable integration
- Complex fatigue scoring
- Complex periodization
- Required workout completion scoring
- Punitive missed-workout language
- Video demos
- Cloud sync
- HH integration implementation

---

## Core Design Principle

### Planned Roadmap ≠ Actual Session

The app should preserve both:

1. **Planned intent**
   - what the user expected to do
   - suggested order
   - target split/body focus
   - suggested exercises

2. **Actual execution**
   - what the user really did
   - actual order
   - substitutions
   - impromptu additions
   - skipped or deferred items

This keeps the app honest without being strict.

---

## Core Entities

### Exercise

Represents a known movement.

Suggested fields:

- id
- name
- primaryBodyArea
- secondaryBodyAreas
- category/type
- notes
- isActive
- createdAt
- updatedAt

Examples:

- Bench Press
- Dumbbell Press
- Chest Press Machine
- Lat Pulldown
- Squat
- Leg Press

---

### ExerciseEquivalence / ExerciseSubstitution

Represents user-defined alternatives.

Suggested fields:

- id
- sourceExerciseId
- substituteExerciseId
- relationshipType
- notes
- isActive

Possible relationship types:

- substitute
- similar
- machineAlternative
- dumbbellAlternative
- bodyweightAlternative

V1 can start with a simple substitute relationship.

Important behavior:

- Substitutions are suggestions.
- The app should not automatically assume they are identical.
- The user chooses the substitute during session execution.

---

### SplitTemplate

Represents a training focus or split.

Suggested fields:

- id
- name
- notes
- suggestedDaysBeforeRepeat
- isActive

Examples:

- Push
- Pull
- Legs
- Upper
- Lower
- Chest + Triceps
- Back + Biceps

---

### SplitTemplateExercise

Represents the planned exercise list for a split.

Suggested fields:

- id
- splitTemplateId
- exerciseId
- suggestedOrder
- notes
- isOptional

This is the reusable roadmap definition.

---

### WorkoutSession

Represents one planned or performed gym session.

Suggested fields:

- id
- sessionDate
- splitTemplateId nullable
- title
- status
- notes
- startedAt nullable
- completedAt nullable

Possible statuses:

- planned
- inProgress
- completed
- abandoned

Use gentle labels in UI. Avoid harsh wording.

---

### PlannedSessionExercise

Represents the session-specific copy of the roadmap.

Suggested fields:

- id
- workoutSessionId
- sourceSplitTemplateExerciseId nullable
- plannedExerciseId
- suggestedOrder
- notes
- plannedStatus

Possible planned statuses:

- suggested
- performed
- substituted
- skipped
- notDone

This lets the user change the session without mutating the reusable split template.

---

### ActualExerciseLog

Represents what actually happened.

Suggested fields:

- id
- workoutSessionId
- plannedSessionExerciseId nullable
- exerciseId
- logOrder
- logType
- notes
- performedAt nullable

Possible log types:

- plannedExercise
- substitution
- impromptu

This is the most important entity for flexible execution.

---

### ActualExerciseSetLog

Optional for v1, but likely useful.

Suggested fields:

- id
- actualExerciseLogId
- setOrder
- weight
- reps
- durationSeconds nullable
- distance nullable
- notes

Recommendation:

For v1, include simple set logging if desired, but do not build analytics around it yet.

---

## Flexible Execution Model

### Starting a Session

User can:

1. Choose a split/template.
2. Generate a session roadmap.
3. Start the session.
4. Follow the suggested order loosely.

### During Session

For each planned exercise, user can:

- log it as performed
- substitute it
- skip/defer it
- add notes

At any time, user can:

- add impromptu exercise
- log available machine/equipment
- perform items out of order

### Actual Order

Actual order should come from `ActualExerciseLog.logOrder`.

The planned order remains only as a suggestion.

---

## Planned vs Actual Examples

### Example 1: Normal Planned Exercise

Planned:

- Bench Press

Actual:

- Bench Press

Model:

- PlannedSessionExercise: Bench Press
- ActualExerciseLog: Bench Press, linked to planned item, type = plannedExercise

---

### Example 2: Substitution

Planned:

- Bench Press

Actual:

- Dumbbell Press

Model:

- PlannedSessionExercise: Bench Press, status = substituted
- ActualExerciseLog: Dumbbell Press, linked to planned item, type = substitution

The original plan remains visible.

---

### Example 3: Impromptu Exercise

Planned:

- Squat
- Leg Press

Actual added:

- Calf Raise Machine

Model:

- ActualExerciseLog: Calf Raise Machine, no plannedSessionExerciseId, type = impromptu

---

## Split / Cycle Tracking

V1 should keep this simple.

### Recommended Approach

Track split coverage from completed sessions.

A split is considered “covered” when:

- a session linked to that split is completed
- or the user manually marks the split as covered

Useful display:

- Push: last covered May 5
- Pull: last covered May 3
- Legs: last covered Apr 30

### Current Cycle

For v1, use a simple weekly view first:

- This week covered: Push, Pull
- Not yet covered: Legs

Later, allow custom cycle rules:

- 3-day cycle
- 4-day split
- rolling cycle
- user-defined repeat pattern

Do not overbuild this in v1.

---

## Rest-Day Suggestions

Rest suggestions should be advisory.

### Rule

If a split has `suggestedDaysBeforeRepeat`, compare:

- today/session date
- last completed session date for that split

### Example

Split:

- Push
- suggestedDaysBeforeRepeat = 2

If Push was last covered yesterday:

- “Push was last covered 1 day ago. Rest may be useful before repeating.”

If enough time has passed:

- “Push is available based on your suggested rest spacing.”

### Avoid

- “You failed to rest”
- “Workout missed”
- “Bad recovery”
- “Noncompliant”

### Preferred Wording

- “Rest recommended”
- “Recently covered”
- “Consider another split”
- “Available based on your spacing”
- “Last covered”

---

## V1 UI Screens

### 1. Home / Today Screen

Purpose:

- show suggested next split
- show recent split coverage
- start planned session
- continue in-progress session

Possible cards:

- Current/next session
- Last covered splits
- Rest recommendation
- Quick start

---

### 2. Split Templates Screen

Purpose:

- list splits
- create/edit split
- configure suggested days between repeats
- open roadmap editor

---

### 3. Split Roadmap Editor

Purpose:

- add exercises to split
- reorder suggested exercises
- mark optional items
- add notes

---

### 4. Exercise Library Screen

Purpose:

- create/edit exercises
- assign body area/category
- open substitutions

---

### 5. Exercise Substitutions Screen

Purpose:

- define alternatives for an exercise
- view suggested substitutions during session

---

### 6. Session Roadmap Screen

Purpose:

- view planned session
- start session
- see suggested order
- see available substitutions

---

### 7. Active Session Screen

Purpose:

- log planned exercise
- substitute exercise
- add impromptu exercise
- add set notes/details
- complete session

This is the core v1 screen.

---

### 8. Session History Screen

Purpose:

- view completed sessions
- see what split was covered
- review actual exercises performed

---

## Conceptual Room Schema

This is not Kotlin code. This is only a planning-level schema.

### exercise

- id PK
- name
- primary_body_area
- notes
- is_active
- created_at
- updated_at

### exercise_substitution

- id PK
- source_exercise_id FK exercise.id
- substitute_exercise_id FK exercise.id
- relationship_type
- notes
- is_active

### split_template

- id PK
- name
- notes
- suggested_days_before_repeat
- is_active
- created_at
- updated_at

### split_template_exercise

- id PK
- split_template_id FK split_template.id
- exercise_id FK exercise.id
- suggested_order
- notes
- is_optional

### workout_session

- id PK
- session_date
- split_template_id nullable FK split_template.id
- title
- status
- notes
- started_at nullable
- completed_at nullable
- created_at
- updated_at

### planned_session_exercise

- id PK
- workout_session_id FK workout_session.id
- source_split_template_exercise_id nullable FK split_template_exercise.id
- planned_exercise_id FK exercise.id
- suggested_order
- planned_status
- notes

### actual_exercise_log

- id PK
- workout_session_id FK workout_session.id
- planned_session_exercise_id nullable FK planned_session_exercise.id
- exercise_id FK exercise.id
- log_order
- log_type
- notes
- performed_at nullable

### actual_exercise_set_log

- id PK
- actual_exercise_log_id FK actual_exercise_log.id
- set_order
- weight nullable
- reps nullable
- duration_seconds nullable
- distance nullable
- notes

---

## Recommended First Implementation Milestone

### Milestone 1: Skeleton + Exercise Library + Split Templates

Goal:

Build the app foundation without session complexity yet.

Deliver:

1. Create Android Studio project.
2. Add basic packages.
3. Add Room.
4. Add Exercise entity/DAO/repository.
5. Add SplitTemplate entity/DAO/repository.
6. Add SplitTemplateExercise.
7. Add simple screens:
   - Exercise Library
   - Split Templates
   - Split Roadmap Editor

Why first:

- Exercises and splits are the foundation.
- No flexible session logic is needed yet.
- Easy to test.
- Avoids premature logging complexity.

---

## Milestone Roadmap

### Milestone 1: Foundation

- Exercise library
- Split templates
- Split roadmap editor
- Basic Room schema
- Minimal navigation

### Milestone 2: Session Generation

- Create session from split template
- Copy roadmap into PlannedSessionExercise
- View session roadmap
- Start session

### Milestone 3: Active Session Logging

- Log planned exercise
- Add impromptu exercise
- Complete session
- Store actual logs
- Preserve planned vs actual distinction

### Milestone 4: Substitutions

- Define substitutions
- Show substitutions during session
- Log substitute while preserving original planned item

### Milestone 5: Split Memory + Rest Suggestions

- Last covered split
- Weekly/current cycle summary
- Gentle rest recommendation
- Home screen summary

### Milestone 6: Polish / Hardening

- Edit completed session notes
- Better empty states
- Safer delete handling
- Basic tests
- Export/import planning if needed later

---

## Naming Notes

App name: **Kusgang Aliwas**

Possible plain-English internal description:

> A flexible gym session roadmap and exercise log for remembering what was planned, what was actually performed, and which split was last covered.

Possible short tagline:

> Plan the session. Adapt in the gym. Remember what you covered.

---

## Future Ideas, Not V1

- HH read-only integration
- calendar-style training history
- personal records
- progression suggestions
- deload reminders
- workout duration analytics
- volume tracking
- equipment availability profiles
- muscle-group heat map
- exercise media/demo links
- cloud backup/sync
- AK/HH shared dashboard
- custom cycle engine beyond weekly view

---

## Design Reminders for Future Coding Chats

- Planning first, coding second.
- One file at a time.
- Full-file replacements when editing.
- Keep UI simple and functional.
- Avoid broad refactors.
- Preserve future HH integration.
- No nutrition.
- No punitive language.
- Suggested order is not enforced.
- Actual logs should always be allowed to differ from planned roadmap.
