package com.example.noise

/**
 * Formats a name for display purposes only.
 * - Capitalizes the first letter of each word.
 * - Ignores single-letter words (e.g., initials).
 */
fun formatNameForDisplay(name: String): String {
    return name
        .split(' ')
        .filter { it.length > 1 } // Ignores single-letter words
        .joinToString(" ") { word ->
            // Ensures the word is lowercase before capitalizing the first letter
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
