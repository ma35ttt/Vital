package com.vital.domain

import com.vital.data.entity.ActivityLevel
import com.vital.data.entity.GoalType
import com.vital.data.entity.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyGoalsTest {

    private val male = BodyProfile(
        weightKg = 82.0,
        heightCm = 178.0,
        age = 29,
        sex = Sex.MALE,
        activityLevel = ActivityLevel.MODERATE
    )

    @Test
    fun `mifflin st jeor for a male`() {
        assertEquals(1792.5, bmrKcal(male), 1e-9)
    }

    @Test
    fun `female resting rate is 166 kcal lower`() {
        assertEquals(1626.5, bmrKcal(male.copy(sex = Sex.FEMALE)), 1e-9)
    }

    @Test
    fun `activity buckets increase maintenance`() {
        val none = maintenanceKcal(male.copy(activityLevel = ActivityLevel.NONE))
        val light = maintenanceKcal(male.copy(activityLevel = ActivityLevel.LIGHT))
        val moderate = maintenanceKcal(male.copy(activityLevel = ActivityLevel.MODERATE))
        val high = maintenanceKcal(male.copy(activityLevel = ActivityLevel.HIGH))
        assertEquals(2151.0, none, 1e-9)
        assertEquals(2464.6875, light, 1e-9)
        assertEquals(2778.375, moderate, 1e-9)
        assertEquals(3092.0625, high, 1e-9)
        assertEquals(none < light && light < moderate && moderate < high, true)
    }

    @Test
    fun `healthy range is the bmi 18_5 to 25 band`() {
        val range = healthyRange(178.0)
        assertEquals(58.6154, range.minKg, 1e-3)
        assertEquals(79.21, range.maxKg, 1e-3)
    }

    @Test
    fun `bmi uses metres`() {
        assertEquals(25.8806, bmi(82.0, 178.0), 1e-3)
    }

    @Test
    fun `loss plan matches the worked example`() {
        val plan = bodyGoalPlan(male, GoalType.LOSE, goalWeightKg = 75.0, rateKgPerWeek = 0.5)
        assertEquals(2778, plan.maintenanceKcal)
        assertEquals(2230, plan.targetKcal)
        assertEquals(548, plan.deficitKcal)
        assertEquals(164.0, plan.macros.proteinG, 1e-9)
        assertEquals(245.0, plan.macros.carbsG, 1e-9)
        assertEquals(66.0, plan.macros.fatG, 1e-9)
        assertEquals(2230.0, plan.macros.kcal, 1e-9)
        assertEquals(25.9, plan.bmiNow, 0.05)
        assertEquals(23.7, plan.bmiGoal!!, 0.05)
        assertEquals(-7.0, plan.kgToGo!!, 1e-9)
        assertEquals(14, plan.weeks)
    }

    @Test
    fun `maintain keeps target equal to maintenance`() {
        val plan = bodyGoalPlan(male, GoalType.MAINTAIN, goalWeightKg = null)
        assertEquals(2778, plan.maintenanceKcal)
        assertEquals(2778, plan.targetKcal)
        assertEquals(0, plan.deficitKcal)
        assertNull(plan.kgToGo)
        assertNull(plan.weeks)
        assertNull(plan.bmiGoal)
    }

    @Test
    fun `gain plan adds a surplus and reports positive deficit`() {
        val plan = bodyGoalPlan(male, GoalType.GAIN, goalWeightKg = 86.0, rateKgPerWeek = 0.25)
        assertEquals(2778, plan.maintenanceKcal)
        // 0.25 kg/week ≈ 275 kcal/day, so the target rounds up from ~3053 to 3050.
        assertEquals(3050, plan.targetKcal)
        assertEquals(-272, plan.deficitKcal)
        assertEquals(4.0, plan.kgToGo!!, 1e-9)
        assertEquals(16, plan.weeks)
    }
}
