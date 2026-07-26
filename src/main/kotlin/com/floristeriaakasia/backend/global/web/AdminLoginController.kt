package com.floristeriaakasia.backend.global.web

import com.floristeriaakasia.backend.util.HtmlSanitizer
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/admin")
class AdminLoginController(
    private val authenticationManager: AuthenticationManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)


    @GetMapping("/login")
    fun loginPage(model: Model): String {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal != "anonymousUser") {
            return "redirect:/admin/dashboard"
        }
        return "admin/login"
    }

    @PostMapping("/login")
    fun processLogin(
        @ModelAttribute("username") username: String,
        @ModelAttribute("password") password: String,
        request: HttpServletRequest,
        session: HttpSession,
        redirectAttributes: RedirectAttributes
    ): String {
        val safeUsername = HtmlSanitizer.sanitizeUsername(username.trim()) ?: ""

        return try {

            val authToken = UsernamePasswordAuthenticationToken(safeUsername, password)
            val authenticated = authenticationManager.authenticate(authToken)

            val securityContext = SecurityContextImpl(authenticated)
            SecurityContextHolder.setContext(securityContext)
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
            )
            log.info("ADMIN_WEB_LOGIN_OK username={} ip={}", safeUsername, clientIp(request))

            "redirect:/admin/dashboard"

        } catch (_: BadCredentialsException) {
            log.warn("ADMIN_WEB_LOGIN_FAILED username={} ip={}", safeUsername, clientIp(request))
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password")
            "redirect:/admin/login"
        } catch (_: DisabledException) {
            log.warn("ADMIN_WEB_LOGIN_FAIL username={} ip={} reason=ACCOUNT_DISABLED", safeUsername, clientIp(request))
            redirectAttributes.addFlashAttribute("errorMessage", "Your account has been disabled. Contact support.")
            "redirect:/admin/login"
        } catch (_: LockedException) {
            log.warn("ADMIN_WEB_LOGIN_FAIL username={} ip={} reason=ACCOUNT_LOCKED", safeUsername, clientIp(request))
            redirectAttributes.addFlashAttribute("errorMessage", "Your account is locked. Contact support.")
            "redirect:/admin/login"
        } catch (ex: Exception) {
            log.error("ADMIN_WEB_LOGIN_ERROR username={} ip={}", safeUsername, clientIp(request), ex)
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Authentication service unavailable. Please try again."
            )
            "redirect:/admin/login"
        }


    }


    @GetMapping("/logout")
    fun logout(
        session: HttpSession,
        request: HttpServletRequest
    ): String {
        val username = SecurityContextHolder.getContext().authentication?.name ?: "unknown"
        SecurityContextHolder.clearContext()
        session.invalidate()
        log.info("ADMIN_WEB_LOGOUT username={} ip={}", username, clientIp(request))
        return "redirect:/admin/login?logout"
    }

    private fun clientIp(request: HttpServletRequest): String {
        val cfIp = request.getHeader("CF-Connecting-IP")
        val xff = request.getHeader("X-Forwarded-For")
        val realIp = request.getHeader("X-Real-IP")
        return when {
            !cfIp.isNullOrBlank() -> cfIp.trim()
            !xff.isNullOrBlank() -> xff.split(",").first().trim()
            !realIp.isNullOrBlank() -> realIp.trim()
            else -> request.remoteAddr ?: "unknown"
        }
    }


}