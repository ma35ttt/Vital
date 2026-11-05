package com.vital.ui

import com.vital.data.entity.CardioType
import com.vital.data.entity.Intensity
import com.vital.data.entity.StrokeType

/** One enterable value on the Cardio screen; the visible set depends on the activity. */
enum class CardioField {
    DURATION, DISTANCE_KM, DISTANCE_M, SPEED, PACE, INCLINE, LEVEL, CADENCE, FLOORS, LAPS, STROKE, INTENSITY
}

/** How a field is typed and filtered. */
enum class CardioInput { TEXT, DECIMAL, INTEGER, PICKER }

data class CardioFieldSpec(
    val field: CardioField,
    val label: String,
    val input: CardioInput = field.input
)

/** How a value is typed and filtered, independent of the activity. */
val CardioField.input: CardioInput
    get() = when (this) {
        CardioField.DURATION, CardioField.PACE -> CardioInput.TEXT
        CardioField.DISTANCE_KM, CardioField.DISTANCE_M, CardioField.SPEED, CardioField.INCLINE -> CardioInput.DECIMAL
        CardioField.LEVEL, CardioField.CADENCE, CardioField.FLOORS, CardioField.LAPS -> CardioInput.INTEGER
        CardioField.STROKE, CardioField.INTENSITY -> CardioInput.PICKER
    }

/**
 * The two-by-two grid the Cardio screen shows for each activity. Walking and running keep
 * the original four fields; the rest map to what their machine or sport actually reports.
 */
fun CardioType.fieldSpecs(): List<CardioFieldSpec> = when (this) {
    CardioType.RUNNING, CardioType.WALKING -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_KM, "Distance · km", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.SPEED, "Speed · km/h", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.INCLINE, "Incline · %", CardioInput.DECIMAL)
    )
    CardioType.CYCLING -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_KM, "Distance · km", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.SPEED, "Speed · km/h", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.LEVEL, "Resistance · 1–20", CardioInput.INTEGER)
    )
    CardioType.ROWING -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_M, "Distance · m", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.PACE, "Pace · /500 m", CardioInput.TEXT),
        CardioFieldSpec(CardioField.CADENCE, "Stroke rate · spm", CardioInput.INTEGER)
    )
    CardioType.SWIMMING -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_M, "Distance · m", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.LAPS, "Laps", CardioInput.INTEGER),
        CardioFieldSpec(CardioField.STROKE, "Stroke", CardioInput.PICKER)
    )
    CardioType.ELLIPTICAL -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_KM, "Distance · km", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.LEVEL, "Resistance · 1–20", CardioInput.INTEGER),
        CardioFieldSpec(CardioField.INCLINE, "Incline · %", CardioInput.DECIMAL)
    )
    CardioType.STAIR_CLIMBER -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.FLOORS, "Floors", CardioInput.INTEGER),
        CardioFieldSpec(CardioField.CADENCE, "Steps/min · spm", CardioInput.INTEGER),
        CardioFieldSpec(CardioField.LEVEL, "Level · 1–20", CardioInput.INTEGER)
    )
    CardioType.OTHER -> listOf(
        CardioFieldSpec(CardioField.DURATION, "Duration", CardioInput.TEXT),
        CardioFieldSpec(CardioField.DISTANCE_KM, "Distance · km", CardioInput.DECIMAL),
        CardioFieldSpec(CardioField.INTENSITY, "Intensity", CardioInput.PICKER)
    )
}

/** Walking and running use the ACSM equations; everything else uses a MET estimate. */
val CardioType.usesAcsm: Boolean
    get() = this == CardioType.RUNNING || this == CardioType.WALKING

/** Default picker selection for a field, used until the draft has a value. */
fun CardioType.defaultPicker(field: CardioField): String? = when (field) {
    CardioField.STROKE -> StrokeType.FREESTYLE.name
    CardioField.INTENSITY -> Intensity.MODERATE.name
    else -> null
}

fun StrokeType.display(): String = when (this) {
    StrokeType.FREESTYLE -> "Freestyle"
    StrokeType.BREASTSTROKE -> "Breaststroke"
    StrokeType.BACKSTROKE -> "Backstroke"
    StrokeType.BUTTERFLY -> "Butterfly"
    StrokeType.MIXED -> "Mixed"
}

fun Intensity.display(): String = when (this) {
    Intensity.LIGHT -> "Light"
    Intensity.MODERATE -> "Moderate"
    Intensity.VIGOROUS -> "Vigorous"
}

/** Advances a picker to its next option, wrapping around; other fields are unchanged. */
fun cyclePicker(field: CardioField, current: String?): String? {
    fun next(name: String?, values: List<String>): String {
        val index = values.indexOf(name).takeIf { it >= 0 } ?: 0
        return values[(index + 1) % values.size]
    }
    return when (field) {
        CardioField.STROKE -> next(current, StrokeType.entries.map { it.name })
        CardioField.INTENSITY -> next(current, Intensity.entries.map { it.name })
        else -> current
    }
}

/** Label shown inside a picker cell, falling back to the type's default. */
fun pickerDisplay(field: CardioField, current: String?): String = when (field) {
    CardioField.STROKE ->
        current?.let { runCatching { StrokeType.valueOf(it) }.getOrNull() }?.display()
            ?: StrokeType.FREESTYLE.display()
    CardioField.INTENSITY ->
        current?.let { runCatching { Intensity.valueOf(it) }.getOrNull() }?.display()
            ?: Intensity.MODERATE.display()
    else -> current.orEmpty()
}

/** Swim's stroke value, defaulting to freestyle; null for other activities. */
fun resolveStroke(type: CardioType, raw: String?): StrokeType? {
    if (type != CardioType.SWIMMING) return null
    return raw?.let { runCatching { StrokeType.valueOf(it) }.getOrNull() } ?: StrokeType.FREESTYLE
}

/** Other's effort value, defaulting to moderate; null for other activities. */
fun resolveIntensity(type: CardioType, raw: String?): Intensity? {
    if (type != CardioType.OTHER) return null
    return raw?.let { runCatching { Intensity.valueOf(it) }.getOrNull() } ?: Intensity.MODERATE
}
