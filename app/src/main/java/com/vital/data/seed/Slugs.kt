package com.vital.data.seed

/**
 * Turns a display name into a stable id fragment ("Bengal gram, dal" -> "bengal-gram-dal").
 * Shared by the bundled catalogs so their seed ids never change between launches.
 */
internal fun String.toSeedSlug(): String {
    val slug = lowercase().map { if (it.isLetterOrDigit()) it else '-' }.joinToString("")
    return slug.trim('-').replace(Regex("-+"), "-")
}
