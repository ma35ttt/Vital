package com.vital.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerTest {

    @Test
    fun `start begins at the full duration`() {
        val state = RestTimerState(totalSeconds = 90).start()
        assertEquals(90, state.remainingSeconds)
        assertTrue(state.running)
    }

    @Test
    fun `tick counts down and stops at zero`() {
        var state = RestTimerState(totalSeconds = 3).start()
        state = state.tick()
        assertEquals(2, state.remainingSeconds)
        assertTrue(state.running)
        state = state.tick()
        state = state.tick()
        assertEquals(0, state.remainingSeconds)
        assertFalse(state.running)
        assertTrue(state.isFinished)
    }

    @Test
    fun `tick is ignored while paused or finished`() {
        val paused = RestTimerState(totalSeconds = 60).start().pause()
        assertEquals(paused, paused.tick())
        val finished = RestTimerState(totalSeconds = 1).start().tick()
        assertEquals(finished, finished.tick())
    }

    @Test
    fun `resume restarts a paused timer but not a finished one`() {
        val paused = RestTimerState(totalSeconds = 60).start().pause()
        assertTrue(paused.resume().running)
        val finished = RestTimerState(totalSeconds = 1).start().tick()
        assertEquals(finished, finished.resume())
    }

    @Test
    fun `reset restores the duration and pauses`() {
        val state = RestTimerState(totalSeconds = 90).start().tick(30).reset()
        assertEquals(90, state.remainingSeconds)
        assertFalse(state.running)
    }

    @Test
    fun `large tick does not go below zero`() {
        val state = RestTimerState(totalSeconds = 30).start().tick(100)
        assertEquals(0, state.remainingSeconds)
        assertFalse(state.running)
    }
}
