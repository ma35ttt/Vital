package com.vital.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PACKAGE = "com.vital"

/**
 * Generates the Baseline Profile shipped with release builds.
 *
 * The startup journey is marked for the Startup Profile; the navigation journey is not,
 * so app launch stays lean while the common in-app paths are still pre-compiled.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = PACKAGE,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()
        dismissOnboardingIfPresent()
    }

    @Test
    fun navigation() = rule.collect(
        packageName = PACKAGE,
        includeInStartupProfile = false
    ) {
        pressHome()
        startActivityAndWait()
        dismissOnboardingIfPresent()

        // The four content pages live in one pager behind the bottom nav.
        clickTab("Meals")
        clickTab("Lift")
        clickTab("Cardio")
        clickTab("Today")
        clickTab("More")
    }
}

/** First run shows the intro; later runs land straight on Today. */
private fun MacrobenchmarkScope.dismissOnboardingIfPresent() {
    val skip = runCatching { device.findObject(By.text("Skip")) }.getOrNull()
    if (skip != null) {
        runCatching { skip.click() }
        device.waitForIdle()
    }
}

/**
 * Taps a bottom-nav tab by label. The same word can appear as a page heading, so prefer
 * the clickable instance nearest the bottom of the screen. The UI animates while we look,
 * so a node can go stale between query and click; retry a couple of times.
 */
private fun MacrobenchmarkScope.clickTab(label: String) {
    repeat(3) {
        try {
            val matches = device.findObjects(By.text(label))
            val target = matches
                .filter { runCatching { it.isClickable }.getOrDefault(false) }
                .maxByOrNull { runCatching { it.visibleBounds.centerY() }.getOrDefault(Int.MIN_VALUE) }
                ?: matches.maxByOrNull {
                    runCatching { it.visibleBounds.centerY() }.getOrDefault(Int.MIN_VALUE)
                }
            if (target != null) {
                target.click()
                device.waitForIdle()
                return
            }
            device.waitForIdle()
        } catch (e: StaleObjectException) {
            device.waitForIdle()
        }
    }
}
