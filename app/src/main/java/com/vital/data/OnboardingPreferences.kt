package com.vital.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-run flags kept in SharedPreferences rather than Room so they need no migration.
 * [tutorialSeen] is written the first time the walk-through is finished or skipped.
 */
@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("vital_onboarding", Context.MODE_PRIVATE)

    fun tutorialSeen(): Boolean = prefs.getBoolean(KEY_TUTORIAL_SEEN, false)

    fun markTutorialSeen() {
        prefs.edit().putBoolean(KEY_TUTORIAL_SEEN, true).apply()
    }

    private companion object {
        const val KEY_TUTORIAL_SEEN = "tutorial_seen"
    }
}
