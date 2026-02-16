package com.floristeriaakasia.backend.util

import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils

@Component
object HtmlSanitizer {

    /**
     * Sanitizes HTML and removes potential XSS threats
     */
    fun sanitize(input: String?): String? {
        if (input.isNullOrBlank()) return input
        
        // HTML escape to prevent XSS
        var sanitized = HtmlUtils.htmlEscape(input)
        
        // Remove null bytes
        sanitized = sanitized.replace("\u0000", "")
        
        // Remove potentially dangerous characters
        sanitized = sanitized.replace("<script", "&lt;script", ignoreCase = true)
        sanitized = sanitized.replace("javascript:", "", ignoreCase = true)
        sanitized = sanitized.replace("onerror=", "", ignoreCase = true)
        sanitized = sanitized.replace("onload=", "", ignoreCase = true)
        
        return sanitized.trim()
    }

    /**
     * Sanitizes username - allows only alphanumeric, underscore, hyphen, and dot
     */
    fun sanitizeUsername(username: String?): String? {
        if (username.isNullOrBlank()) return username
        
        // Remove any character that is not alphanumeric, underscore, hyphen, or dot
        return username.replace(Regex("[^a-zA-Z0-9._-]"), "").trim()
    }

    /**
     * Sanitizes email - basic validation pattern
     */
    fun sanitizeEmail(email: String?): String? {
        if (email.isNullOrBlank()) return email
        
        // Remove dangerous characters but keep valid email chars
        return email.replace(Regex("[<>\"'`]"), "").trim().lowercase()
    }

    /**
     * Sanitizes general text fields (like full name)
     */
    fun sanitizeText(text: String?): String? {
        if (text.isNullOrBlank()) return text
        
        var sanitized = HtmlUtils.htmlEscape(text)
        
        // Remove control characters except newline and tab
        sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F\\x7F]"), "")
        
        return sanitized.trim()
    }

    /**
     * Validates that a string contains no HTML or script tags
     */
    fun containsHtml(input: String?): Boolean {
        if (input.isNullOrBlank()) return false

        val htmlPattern = Regex("<[^>]+>|javascript:|on\\w+\\s*=", RegexOption.IGNORE_CASE)
        return htmlPattern.containsMatchIn(input)
    }

    /**
     * Sanitizes product title - allows letters, numbers, spaces, accented chars, and common punctuation
     */
    fun sanitizeProductTitle(title: String?): String? {
        if (title.isNullOrBlank()) return title

        var sanitized = HtmlUtils.htmlEscape(title)

        // Remove control characters
        sanitized = sanitized.replace(Regex("[\\x00-\\x1F\\x7F]"), "")

        // Remove script-related patterns
        sanitized = sanitized.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE), "")

        return sanitized.trim()
    }

    /**
     * Sanitizes product slug - allows only lowercase letters, numbers, and hyphens
     */
    fun sanitizeSlug(slug: String?): String? {
        if (slug.isNullOrBlank()) return slug

        // Remove any character that is not lowercase letter, number, or hyphen
        return slug.lowercase()
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-+"), "-") // Replace multiple hyphens with single
            .trim('-') // Remove leading/trailing hyphens
    }

    /**
     * Sanitizes URL fields
     */
    fun sanitizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url

        var sanitized = url.trim()

        // Remove javascript: protocol
        if (sanitized.startsWith("javascript:", ignoreCase = true)) {
            return null
        }

        // Remove data: protocol (potential XSS vector)
        if (sanitized.startsWith("data:", ignoreCase = true)) {
            return null
        }

        // Only allow http and https protocols
        if (!sanitized.startsWith("http://", ignoreCase = true) &&
            !sanitized.startsWith("https://", ignoreCase = true)) {
            sanitized = "https://$sanitized"
        }

        return sanitized
    }

    /**
     * Sanitizes rich text content (descriptions, paragraphs)
     * More permissive than other methods but still removes XSS vectors
     */
    fun sanitizeRichText(text: String?): String? {
        if (text.isNullOrBlank()) return text

        var sanitized = HtmlUtils.htmlEscape(text)

        // Remove script tags and their content
        sanitized = sanitized.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("<iframe[^>]*>.*?</iframe>", RegexOption.IGNORE_CASE), "")

        // Remove event handlers
        sanitized = sanitized.replace(Regex("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")

        // Remove javascript: protocol
        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")

        // Remove control characters except newlines and tabs
        sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F\\x7F]"), "")

        return sanitized.trim()
    }
}
