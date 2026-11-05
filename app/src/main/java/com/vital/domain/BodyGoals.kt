package com.vital.domain

import com.vital.data.entity.ActivityLevel
import com.vital.data.entity.GoalType
import com.vital.data.entity.Settings
import com.vital.data.entity.Sex
import kotlin.math.roundToInt

/**
 * The energy math behind the body-goal screen.
 *
 * Maintenance comes from the Mifflin-St Jeor resting rate scaled by an activity multiplier, so
 * it is an estimate — accurate to roughly ±10% for most people. It sharpens as real weigh-ins
 * come in, which is why the UI always labels it as an estimate.
 */

/** Energy in 1 kg of body fat; a ~7,700 kcal surplus/deficit moves the scale by a kilo. */
private const val KCAL_PER_KG_FAT = 7700.0

/** Grams of protein per kg of body weight. */
private const val PROTEIN_G_PER_KG = 2.0

/** Grams of fat per kg of body weight. */
private const val FAT_G_PER_KG = 0.8

/** The inputs the energy estimate needs, independent of where they are stored. */
data class BodyProfile(
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val sex: Sex,
    val activityLevel: ActivityLevel
)

/** The BMI 18.5–25 weight band for a given height. */
data class HealthyRange(
    val minKg: Double,
    val maxKg: Double
)

/** Reads the body-goal inputs out of the stored settings. */
fun bodyProfileOf(settings: Settings): BodyProfile = BodyProfile(
    weightKg = settings.bodyWeightKg,
    heightCm = settings.heightCm,
    age = settings.age,
    sex = settings.sex,
    activityLevel = settings.activityLevel
)

/** The plan implied by the stored settings. */
fun bodyGoalPlan(settings: Settings): BodyGoalPlan = bodyGoalPlan(
    profile = bodyProfileOf(settings),
    goalType = settings.goalType,
    goalWeightKg = settings.goalWeightKg,
    rateKgPerWeek = settings.goalRateKgPerWeek
)

/** Everything the goal screen shows, derived once from the stored settings. */
data class BodyGoalPlan(
    val bmrKcal: Double,
    val maintenanceKcal: Int,
    val targetKcal: Int,
    /** Maintenance − target: negative for a deficit (loss), positive for a surplus (gain). */
    val deficitKcal: Int,
    val macros: Macros,
    val healthyRange: HealthyRange,
    val bmiNow: Double,
    /** Null until a goal weight is set. */
    val bmiGoal: Double?,
    val kgToGo: Double?,
    val weeks: Int?
)

/** Activity multipliers applied to BMR; the buckets match the onboarding cardio question. */
val ActivityLevel.multiplier: Double
    get() = when (this) {
        ActivityLevel.NONE -> 1.20
        ActivityLevel.LIGHT -> 1.375
        ActivityLevel.MODERATE -> 1.55
        ActivityLevel.HIGH -> 1.725
    }

/** Mifflin-St Jeor resting energy expenditure in kcal/day. */
fun bmrKcal(profile: BodyProfile): Double {
    val sexConstant = when (profile.sex) {
        Sex.MALE -> 5.0
        Sex.FEMALE -> -161.0
    }
    return 10.0 * profile.weightKg +
        6.25 * profile.heightCm -
        5.0 * profile.age +
        sexConstant
}

/** Resting rate scaled by the activity bucket: the daily maintenance estimate. */
fun maintenanceKcal(profile: BodyProfile): Double =
    bmrKcal(profile) * profile.activityLevel.multiplier

/** Body-mass index for a weight and height. */
fun bmi(weightKg: Double, heightCm: Double): Double {
    val metres = heightCm / 100.0
    return weightKg / (metres * metres)
}

/** The BMI 18.5–25 weight band for a height. */
fun healthyRange(heightCm: Double): HealthyRange {
    val metres = heightCm / 100.0
    val square = metres * metres
    return HealthyRange(minKg = 18.5 * square, maxKg = 25.0 * square)
}

/**
 * Builds the full plan from a profile and the chosen goal.
 *
 * The target is maintenance shifted by [rateKgPerWeek] worth of energy, rounded to the nearest
 * 10 kcal so the number reads cleanly; the reported deficit is then recomputed against the
 * rounded target, so the two always agree. When [goalWeightKg] is set we also report how far is
 * left and how many weeks the pace implies.
 */
fun bodyGoalPlan(
    profile: BodyProfile,
    goalType: GoalType,
    goalWeightKg: Double?,
    rateKgPerWeek: Double = 0.5
): BodyGoalPlan {
    val maintenance = maintenanceKcal(profile)
    val maintenanceInt = maintenance.roundToInt()

    // Loss and gain shift maintenance by the weekly rate; maintain keeps the raw estimate so
    // the target and maintenance agree exactly.
    val target = when (goalType) {
        GoalType.MAINTAIN -> maintenanceInt
        GoalType.LOSE, GoalType.GAIN -> {
            val sign = if (goalType == GoalType.LOSE) -1.0 else 1.0
            val shifted = maintenance + sign * rateKgPerWeek * KCAL_PER_KG_FAT / 7.0
            (shifted / 10.0).roundToInt() * 10
        }
    }

    val range = healthyRange(profile.heightCm)
    val kgToGo = goalWeightKg?.let { it - profile.weightKg }

    return BodyGoalPlan(
        bmrKcal = bmrKcal(profile),
        maintenanceKcal = maintenanceInt,
        targetKcal = target,
        deficitKcal = maintenanceInt - target,
        macros = macrosFor(target, profile.weightKg),
        healthyRange = range,
        bmiNow = bmi(profile.weightKg, profile.heightCm),
        bmiGoal = goalWeightKg?.let { bmi(it, profile.heightCm) },
        kgToGo = kgToGo,
        weeks = weeksToGoal(kgToGo, rateKgPerWeek)
    )
}

/**
 * 2 g/kg protein and 0.8 g/kg fat, with carbs filling the remaining calories. Protein and fat
 * scale with body weight, so they track the user as they change; carbs absorb the difference.
 */
private fun macrosFor(targetKcal: Int, weightKg: Double): Macros {
    val protein = (PROTEIN_G_PER_KG * weightKg).roundToInt()
    val fat = (FAT_G_PER_KG * weightKg).roundToInt()
    val carbKcal = targetKcal - protein * 4 - fat * 9
    val carbs = (carbKcal / 4.0).roundToInt().coerceAtLeast(0)
    return Macros(proteinG = protein.toDouble(), carbsG = carbs.toDouble(), fatG = fat.toDouble())
}

private fun weeksToGoal(kgToGo: Double?, rateKgPerWeek: Double): Int? {
    if (kgToGo == null || rateKgPerWeek <= 0.0) return null
    val weeks = kotlin.math.abs(kgToGo) / rateKgPerWeek
    return weeks.roundToInt().coerceAtLeast(1)
}
