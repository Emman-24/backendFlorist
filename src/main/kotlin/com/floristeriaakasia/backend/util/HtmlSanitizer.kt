package com.floristeriaakasia.backend.util

import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils

@Component
object HtmlSanitizer {

    fun sanitize(input: String?): String? {
        if (input.isNullOrBlank()) return input
        
        var sanitized = HtmlUtils.htmlEscape(input)
        
        sanitized = sanitized.replace("\u0000", "")
        
        sanitized = sanitized.replace("<script", "&lt;script", ignoreCase = true)
        sanitized = sanitized.replace("javascript:", "", ignoreCase = true)
        sanitized = sanitized.replace("onerror=", "", ignoreCase = true)
        sanitized = sanitized.replace("onload=", "", ignoreCase = true)
        
        return sanitized.trim()
    }


    fun sanitizeUsername(username: String?): String? {
        if (username.isNullOrBlank()) return username
        return username.replace(Regex("[^a-zA-Z0-9._-]"), "").trim()
    }


    fun sanitizeEmail(email: String?): String? {
        if (email.isNullOrBlank()) return email
        return email.replace(Regex("[<>\"'`]"), "").trim().lowercase()
    }

    fun sanitizeText(text: String?): String? {
        if (text.isNullOrBlank()) return text
        var sanitized = HtmlUtils.htmlEscape(text)
        sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F\\x7F]"), "")
        return sanitized.trim()
    }

    fun containsHtml(input: String?): Boolean {
        if (input.isNullOrBlank()) return false

        val htmlPattern = Regex("<[^>]+>|javascript:|on\\w+\\s*=", RegexOption.IGNORE_CASE)
        return htmlPattern.containsMatchIn(input)
    }


    fun sanitizeProductTitle(title: String?): String? {
        if (title.isNullOrBlank()) return title

        var sanitized = HtmlUtils.htmlEscape(title)

        sanitized = sanitized.replace(Regex("[\\x00-\\x1F\\x7F]"), "")

        sanitized = sanitized.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE), "")

        return sanitized.trim()
    }

    fun sanitizeSlug(slug: String?): String? {
        if (slug.isNullOrBlank()) return slug

        return slug.lowercase()
            .replace(Regex("[^a-z0-9-]"), "")
            .replace(Regex("-+"), "-")
            .trim('-')
    }

    fun sanitizeUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url

        var sanitized = url.trim()

        if (sanitized.startsWith("javascript:", ignoreCase = true)) {
            return null
        }

        if (sanitized.startsWith("data:", ignoreCase = true)) {
            return null
        }

        if (!sanitized.startsWith("http://", ignoreCase = true) &&
            !sanitized.startsWith("https://", ignoreCase = true)) {
            sanitized = "https://$sanitized"
        }

        return sanitized
    }


    fun sanitizeRichText(text: String?): String? {
        if (text.isNullOrBlank()) return text

        var sanitized = HtmlUtils.htmlEscape(text)


        sanitized = sanitized.replace(Regex("<script[^>]*>.*?</script>", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("<iframe[^>]*>.*?</iframe>", RegexOption.IGNORE_CASE), "")


        sanitized = sanitized.replace(Regex("on\\w+\\s*=\\s*[\"'][^\"']*[\"']", RegexOption.IGNORE_CASE), "")


        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")

        sanitized = sanitized.replace(Regex("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F\\x7F]"), "")

        return sanitized.trim()
    }
}
