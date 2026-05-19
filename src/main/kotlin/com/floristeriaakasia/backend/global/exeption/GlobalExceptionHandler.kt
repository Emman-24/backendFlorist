package com.floristeriaakasia.backend.global.exeption

import com.floristeriaakasia.backend.util.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)


    @ExceptionHandler(FloralArrangementNotFoundException::class)
    fun handleFloralArrangementNotFound(
        ex: FloralArrangementNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Resource not found: uri={}, message={}", request.requestURI, ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Error(message = ex.message ?: "Resource not found", code = "ARRANGEMENT_NOT_FOUND"))
    }


    @ExceptionHandler(FloralArrangementSlugNotFoundException::class)
    fun handleSlugNotFound(
        ex: FloralArrangementSlugNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Slug not found: uri={}, message={}", request.requestURI, ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Error(message = ex.message ?: "Resource not found", code = "SLUG_NOT_FOUND"))
    }

    @ExceptionHandler(FloralArrangementSeoNameNotFoundException::class)
    fun handleSeoNameNotFound(
        ex: FloralArrangementSeoNameNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("SEO name not found: uri={}, message={}", request.requestURI, ex.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Error(message = ex.message ?: "Resource not found", code = "SEO_NAME_NOT_FOUND"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Business rule violation: uri={}, message={}", request.requestURI, ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.Error(message = ex.message ?: "Invalid request", code = "BUSINESS_RULE_VIOLATION"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(
        ex: IllegalStateException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.error("Illegal state: uri={}, message={}", request.requestURI, ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ApiResponse.Error(
                    message = ex.message ?: "Operation not allowed in current state",
                    code = "ILLEGAL_STATE"
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.ValidationError> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { fe ->
            fe.field to (fe.defaultMessage ?: "Invalid value")
        }
        logger.warn(
            "Validation failed: uri={}, fields={}",
            request.requestURI,
            fieldErrors.keys.joinToString()
        )
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                ApiResponse.ValidationError(
                    message = "Validation failed. Check the highlighted fields.",
                    fieldErrors = fieldErrors
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.ValidationError> {
        val fieldErrors = ex.constraintViolations.associate { cv ->
            cv.propertyPath.toString() to (cv.message ?: "Invalid value")
        }
        logger.warn("Constraint violation: uri={}, violations={}", request.requestURI, fieldErrors)
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(
                ApiResponse.ValidationError(
                    message = "Request parameters are invalid.",
                    fieldErrors = fieldErrors
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Malformed request body: uri={}", request.requestURI)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.Error(message = "Malformed or unreadable request body.", code = "MALFORMED_BODY"))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Missing param: uri={}, param={}", request.requestURI, ex.parameterName)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse.Error(
                    message = "Required parameter '${ex.parameterName}' is missing.",
                    code = "MISSING_PARAMETER"
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Type mismatch: uri={}, param={}, value={}", request.requestURI, ex.name, ex.value)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse.Error(
                    message = "Parameter '${ex.name}' has an invalid value: '${ex.value}'.",
                    code = "TYPE_MISMATCH"
                )
            )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Method not allowed: uri={}, method={}", request.requestURI, ex.method)
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ApiResponse.Error(
                    message = "HTTP method '${ex.method}' is not supported on this endpoint.",
                    code = "METHOD_NOT_ALLOWED"
                )
            )
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Unsupported media type: uri={}, type={}", request.requestURI, ex.contentType)
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(
                ApiResponse.Error(
                    message = "Media type '${ex.contentType}' is not supported.",
                    code = "UNSUPPORTED_MEDIA_TYPE"
                )
            )
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandler(
        ex: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("No handler: uri={}, method={}", ex.requestURL, ex.httpMethod)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Error(message = "Endpoint not found.", code = "ENDPOINT_NOT_FOUND"))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        val user = request.userPrincipal?.name ?: "anonymous"
        logger.warn("Access denied: user={}, uri={}", user, request.requestURI)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.Error(message = "You don't have permission to access this resource.", code = "FORBIDDEN"))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(
        ex: BadCredentialsException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Bad credentials: uri={}", request.requestURI)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.Error(message = "Invalid username or password.", code = "BAD_CREDENTIALS"))
    }

    @ExceptionHandler(DisabledException::class)
    fun handleDisabled(request: HttpServletRequest): ResponseEntity<ApiResponse.Error> {
        logger.warn("Disabled account login attempt: uri={}", request.requestURI)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.Error(message = "This account has been disabled.", code = "ACCOUNT_DISABLED"))
    }

    @ExceptionHandler(LockedException::class)
    fun handleLocked(request: HttpServletRequest): ResponseEntity<ApiResponse.Error> {
        logger.warn("Locked account login attempt: uri={}", request.requestURI)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.Error(message = "This account is locked.", code = "ACCOUNT_LOCKED"))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        val message = when {
            ex.message?.contains("slug", ignoreCase = true) == true ->
                "A resource with this slug already exists."

            ex.message?.contains("email", ignoreCase = true) == true ->
                "This email address is already registered."

            ex.message?.contains("username", ignoreCase = true) == true ->
                "This username is already taken."

            ex.message?.contains("public_id", ignoreCase = true) == true ->
                "This image has already been uploaded."

            else -> "A data conflict occurred. The resource may already exist."
        }
        logger.warn("Data integrity violation: uri={}, cause={}", request.requestURI, ex.cause?.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.Error(message = message, code = "DATA_CONFLICT"))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(
        ex: MaxUploadSizeExceededException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Upload too large: uri={}, maxSize={}", request.requestURI, ex.maxUploadSize)
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(
                ApiResponse.Error(
                    message = "Uploaded file exceeds the maximum allowed size.",
                    code = "FILE_TOO_LARGE"
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.error("Unhandled exception: uri={}, type={}", request.requestURI, ex.javaClass.simpleName, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiResponse.Error(
                    message = "An unexpected error occurred. Please try again later.",
                    code = "INTERNAL_ERROR"
                )
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFound(
        ex: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse.Error> {
        logger.warn("Static resource not found: uri={}", request.requestURI)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.Error(message = "Resource not found.", code = "NOT_FOUND"))
    }

}