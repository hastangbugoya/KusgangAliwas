# Kusgang Aliwas
# Pocket Gym Remote Mode Technical Design
# Revision 1 (Experimental Architecture)

## Purpose

This document defines the proposed architecture and interaction model for
Kusgang Aliwas (KA) Pocket Gym Remote Mode.

The goal is to allow users to:
- keep the phone in their pocket,
- navigate workout sessions using a Bluetooth media remote,
- receive spoken guidance through TTS,
- optionally coexist with external music playback,
- complete entire workouts without touching the phone.

This document is a technical architecture/design guide.
It is NOT a user-facing manual.

---

# Design Philosophy

Pocket Gym Mode is intentionally designed around:
- low visual interaction,
- low phone handling,
- gym practicality,
- interruption tolerance,
- remote-first navigation,
- spoken state feedback.

The system should:
- behave predictably,
- tolerate imperfect Bluetooth remotes,
- avoid requiring exact timing,
- minimize accidental actions,
- minimize screen dependency.

The architecture assumes:
- user attention is on exercise performance,
- not on the phone UI.

---

# Core Concept

The Bluetooth remote acts as a directional/session cursor controller.

The user navigates:
- exercises,
- sets,
- fields,
- prompts,
- transitions,

using only:
- Play/Pause,
- Next,
- Previous,
- Volume Up,
- Volume Down.

TTS acts as the primary feedback layer.

The screen becomes optional.

---

# High-Level State Model

Pocket Gym Mode is not based on Compose focus or UI focus.

Instead, the system maintains an independent remote-navigation cursor/state machine.

Conceptually:

```text
Session
    ↓
Exercise List
    ↓
Exercise
    ↓
Set List
    ↓
Set
    ↓
Set Field
```

---

# Core Remote Inputs

## Confirm Input

Usually:
- Play/Pause,
- Enter,
- Center button.

Mapped to:
```kotlin
GymRemoteInput.Confirm
```

Purpose:
- enter selection,
- confirm action,
- start set,
- create set,
- release control to music,
- resume session flow.

---

## Next Input

Usually:
- media next,
- D-pad right.

Mapped to:
```kotlin
GymRemoteInput.Next
```

Purpose:
- move forward through:
  - exercises,
  - sets,
  - fields,
  - prompts.

---

## Previous Input

Usually:
- media previous,
- D-pad left.

Mapped to:
```kotlin
GymRemoteInput.Previous
```

Purpose:
- move backward through:
  - exercises,
  - sets,
  - fields,
  - prompts.

---

## Increment Input

Usually:
- volume up.

Mapped to:
```kotlin
GymRemoteInput.Increment
```

Purpose:
- increase numeric field values.

Examples:
- weight,
- reps,
- duration,
- distance.

---

## Decrement Input

Usually:
- volume down.

Mapped to:
```kotlin
GymRemoteInput.Decrement
```

Purpose:
- decrease numeric field values.

---

# Navigation Levels

## Session-Level Navigation

Initial remote entry point.

Example prompt:

```text
"Press play to go to exercises."
```

At this level:
- next/previous may eventually support session sections,
- play enters exercise navigation.

---

## Exercise-Level Navigation

The user traverses exercises in the session.

Example prompts:

```text
"Bench press."
"Treadmill."
"Lat pulldown."
```

Behavior:
- next moves to next exercise,
- previous moves to previous exercise,
- confirm enters selected exercise.

---

## Empty Exercise Behavior

If an exercise has no sets:

```text
"Bench press. No sets. Press play to add set."
```

Confirm:
- creates set 1,
- enters set editing flow.

---

# Set-Level Navigation

The user traverses sets inside an exercise.

Example prompts:

```text
"Set 1."
"Set 2."
"Set 3."
```

Behavior:
- next/previous move between sets,
- confirm enters field editing.

---

# End-of-Set Traversal

To prevent accidental infinite set creation:

The user must intentionally traverse past the final set.

Example:

```text
"Press play to add next set."
```

This design:
- avoids accidental set spam,
- preserves predictable navigation,
- reinforces intentional session structure.

---

# Field-Level Navigation

The user edits fields inside a set.

Examples:

```text
"Set 1. 135 pounds."
"Set 1. 8 reps."
"Set 1. 2 minutes."
```

Behavior:
- next/previous move between fields,
- increment/decrement adjust values,
- confirm transitions to the next logical state.

---

# Suggested Field Order

For strength exercises:

```text
Weight
→ Reps
→ Start Set
```

For cardio:

```text
Duration
→ Distance
→ Start Segment
```

---

# Start Set Flow

The proposed flow intentionally integrates with external music playback.

## Example

```text
Set 1 weight
→ Set 1 reps
→ Confirm
→ KA releases media control
→ External music resumes
→ User performs set
→ User presses pause on remote
→ KA regains media control
→ KA advances workflow
```

This converts media-session ownership conflicts into a natural workout rhythm.

---

# Music Interop Model

## Observed Android Behavior

Testing showed:

- when another media app is actively playing,
  that app owns Bluetooth media controls.

- when playback pauses,
  KA can reclaim media-session control.

This behavior is considered acceptable.

---

# Proposed Music Flow

## During Set

External music app owns:
- play/pause,
- media controls.

KA temporarily yields control.

---

## Between Sets

KA owns:
- navigation,
- TTS,
- workout interaction.

User pauses music to return control to KA.

---

# Conceptual Workout Rhythm

```text
KA control
→ setup/edit/navigation

Music control
→ during actual effort/performance
```

This is considered a desirable interaction model.

---

# Cursor Architecture

Remote navigation should NOT rely on:
- Compose focus,
- focused text fields,
- visible UI state.

Instead:
- a dedicated remote cursor state machine should exist.

Example conceptual model:

```kotlin
sealed interface GymRemoteCursor {

    data object SessionStart : GymRemoteCursor

    data class ExerciseList(
        val selectedExerciseLogId: Long,
    ) : GymRemoteCursor

    data class SetList(
        val exerciseLogId: Long,
        val selectedSetLogId: Long?,
    ) : GymRemoteCursor

    data class SetField(
        val exerciseLogId: Long,
        val setLogId: Long,
        val field: GymRemoteField,
    ) : GymRemoteCursor

    data class AddSetPrompt(
        val exerciseLogId: Long,
    ) : GymRemoteCursor
}
```

This is conceptual guidance only.
Final implementation may differ.

---

# Suggested Field Enum

```kotlin
enum class GymRemoteField {
    WEIGHT,
    REPS,
    DURATION,
    DISTANCE,
    REST_TIMER,
}
```

---

# TTS Requirements

TTS should:
- reflect authoritative state,
- never invent values,
- avoid ambiguity,
- avoid verbose phrasing.

Examples:

Good:
```text
"Set 2. 135 pounds."
```

Bad:
```text
"You are now editing the weight field for set 2."
```

The spoken interaction must remain concise.

---

# Architectural Requirements

Pocket Gym Mode should eventually:
- operate independently from visible UI state,
- survive screen-off operation,
- survive app backgrounding,
- tolerate notification interruptions,
- tolerate temporary Bluetooth disconnects.

The system should be centered around:
- a foreground service,
- media session ownership,
- remote input buses,
- state-driven TTS.

---

# Current Experimental Architecture

Current experiment includes:

```text
Bluetooth Remote
    ↓
Android Media Session
    ↓
GymRemoteService
    ↓
GymRemoteInputBus
    ↓
SessionDetailViewModel
    ↓
Session State
    ↓
GymVoiceBus
    ↓
TTS
```

---

# Known Limitations

## Android Media Session Ownership

Android routes Bluetooth media controls to the currently active media app.

Therefore:
- external music playback may intercept remote controls,
- KA may only regain control after playback pauses.

This behavior is currently accepted.

---

# Future Exploration Areas

Potential future features:

- native music playback inside KA,
- rest timers,
- interval timers,
- set auto-complete heuristics,
- voice recognition,
- workout audio cues,
- wearable integration,
- Bluetooth earbud gesture support,
- remote remapping,
- configurable TTS verbosity,
- haptic feedback,
- gym-mode-only simplified UI.

---

# Important Non-Goals

Pocket Gym Mode is NOT intended to:
- fully replace the touchscreen UI,
- become a generic media player,
- require perfect voice interaction,
- enforce workout structure rigidly.

The philosophy remains:
- assistive,
- flexible,
- lightweight,
- practical.

---

# Design Direction Summary

Pocket Gym Mode should feel like:

```text
A spoken workout cursor system
with optional music integration
and minimal phone handling.
```
