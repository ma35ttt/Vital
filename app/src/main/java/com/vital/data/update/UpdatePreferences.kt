package com.vital.data.update

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Small, unencrypted bookkeeping for the updater: throttle window and "Later" choice. */
@Singleton
class UpdatePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("vital_updates", Context.MODE_PRIVATE)

    /** The version the user asked to skip; launch checks stay quiet until a newer one ships. */
    var dismissedVersionCode: Int
        get() = prefs.getInt(KEY_DISMISSED_VERSION, 0)
        set(value) {
            prefs.edit().putInt(KEY_DISMISSED_VERSION, value).apply()
        }

    /** Last successful check, so foreground launches do not hit the network every time. */
    var lastCheckAt: Long
        get() = prefs.getLong(KEY_LAST_CHECK, 0L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_CHECK, value).apply()
        }

    private companion object {
        const val KEY_DISMISSED_VERSION = "dismissed_version_code"
        const val KEY_LAST_CHECK = "last_check_at"
    }
}
