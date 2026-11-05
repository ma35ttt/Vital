package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class RemindersTest {

    @Test
    fun `times parse the stored json and skip junk`() {
        val parsed = Reminders.times("""["08:00","13:30","25:99","nope"]""")

        assertEquals(listOf(LocalTime.of(8, 0), LocalTime.of(13, 30)), parsed)
    }

    @Test
    fun `days parse one to seven only`() {
        assertEquals(listOf(1, 2, 3, 7), Reminders.days("""[1,2,3,7,9,0]"""))
    }

    @Test
    fun `clock times parse and reject junk`() {
        assertEquals(LocalTime.of(18, 5), Reminders.time("18:05"))
        assertEquals(LocalTime.of(7, 0), Reminders.time(" 07:00 "))
        assertEquals(null, Reminders.time("25:00"))
        assertEquals(null, Reminders.time("noon"))
    }

    @Test
    fun `iso day matches the stored monday first numbering`() {
        assertEquals(1, Reminders.isoDay(DayOfWeek.MONDAY))
        assertEquals(7, Reminders.isoDay(DayOfWeek.SUNDAY))
    }

    @Test
    fun `meals are not due before the first reminder`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(13, 0))

        assertFalse(Reminders.mealsDue(true, times, false, LocalTime.of(7, 30)))
    }

    @Test
    fun `meals are due after the first reminder with nothing logged`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(13, 0))

        assertTrue(Reminders.mealsDue(true, times, false, LocalTime.of(9, 0)))
    }

    @Test
    fun `any logged meal clears the meal dot for the day`() {
        val times = listOf(LocalTime.of(8, 0), LocalTime.of(13, 0))

        assertFalse(Reminders.mealsDue(true, times, hasMealsToday = true, now = LocalTime.of(20, 0)))
    }

    @Test
    fun `disabled meal reminders never raise the dot`() {
        assertFalse(
            Reminders.mealsDue(
                enabled = false,
                times = listOf(LocalTime.of(8, 0)),
                hasMealsToday = false,
                now = LocalTime.of(20, 0)
            )
        )
    }

    @Test
    fun `workout is due only on its day after its time`() {
        val monday = 1
        val tuesday = 2
        val time = LocalTime.of(18, 0)

        assertTrue(Reminders.workoutDue(true, listOf(monday), time, monday, false, LocalTime.of(19, 0)))
        assertFalse(Reminders.workoutDue(true, listOf(monday), time, monday, false, LocalTime.of(17, 0)))
        assertFalse(Reminders.workoutDue(true, listOf(monday), time, tuesday, false, LocalTime.of(19, 0)))
    }

    @Test
    fun `a logged workout clears the dot even after its time`() {
        assertFalse(
            Reminders.workoutDue(
                enabled = true,
                days = listOf(1, 2, 3, 4, 5, 6, 7),
                time = LocalTime.of(18, 0),
                dayOfWeek = 1,
                hasWorkoutToday = true,
                now = LocalTime.of(21, 0)
            )
        )
    }
}
