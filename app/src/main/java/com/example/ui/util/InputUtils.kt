package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Clean text utilities for user input fields.
 */
class InputSanitizer {
    fun sanitize(oldText: String, newText: String): String {
        // Fast path
        if (oldText == newText) return newText

        // Prevent full duplicate word paste/injection bug (e.g., "Ramesh" -> "RameshRamesh")
        if (oldText.isNotEmpty() && oldText.length >= 2 && newText == oldText + oldText) {
            return oldText
        }

        return newText
    }
}

@Composable
fun rememberInputSanitizer(): InputSanitizer {
    return remember { InputSanitizer() }
}

/**
 * Static single-pass sanitizer for dialogs / stateless input fields.
 */
fun sanitizeInputText(oldText: String, newText: String): String {
    if (oldText == newText) return newText
    if (oldText.isNotEmpty() && oldText.length >= 2 && newText == oldText + oldText) {
        return oldText
    }
    return newText
}
