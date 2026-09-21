package ar.outfitmaker.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /** Logger para errores de sistema */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // NotFoundException (HTTP 404 Not Found)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getCode(), "Recurso no encontrado",
                ex.getMessage(), "El elemento solicitado no existe.");
    }

    // ConflictException (HTTP 409 Conflict)
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflictException(ConflictException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getCode(), "Conflicto en la solicitud",
                ex.getMessage(), "La operación no se puede completar debido a un conflicto en los datos.");
    }

    // BusinessException (HTTP 400 Bad Request)
    // Se usa 400 para errores de negocio (validación, reglas de negocio fallidas)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getCode(), "Regla de negocio no cumplida",
                ex.getMessage(), "La solicitud es incorrecta o no cumple una regla de negocio.");
    }

    // InternalException (HTTP 500 Internal Server Error)
    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ApiError> handleInternalException(InternalException ex) {
        log.error("Error interno [{}]", ex.getCode(), ex);   // stack trace completo al log
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCode(), "Error interno del servidor",
                ex.getMessage(), "Ocurrió un error inesperado en el servidor.");
    }

    // TokenExpiredException (HTTP 401 Unauthorized)
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiError> handleTokenExpired(TokenExpiredException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getCode(), "Token vencido",
                ex.getMessage(), "La sesión expiró, volvé a iniciar sesión.");
    }

    // handler para errores en serializacion/deserializacion
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = switch (ex.getCause()) {
            case InvalidFormatException cause ->
                    "El campo '" + fieldPath(cause) + "' tiene un valor inválido.";
            case null, default -> {
                // El mensaje de Jackson expone clases internas y fragmentos del JSON: va al log, no al cliente
                log.warn("Cuerpo del request ilegible", ex);
                yield "El cuerpo de la solicitud no es válido.";
            }
        };

        return errorResponse(HttpStatus.BAD_REQUEST, "REQUEST_MALFORMED", "Error en el formato del JSON",
                detail, "El cuerpo de la solicitud no es válido.");
    }

    // handler para autenticacion de login con token
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Credenciales inválidas",
                null, "El email o la contraseña son incorrectos.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        log.warn("Fallo de autenticación: {}", ex.getMessage());
        return errorResponse(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "No autorizado",
                null, "No se pudo autenticar la solicitud.");
    }

    /**
     * Arma el camino hasta el campo que Jackson no pudo leer: "pattern", "garment.category",
     * o "items[2].layerOrder" cuando el error viene dentro de una lista.
     */
    private String fieldPath(InvalidFormatException ex) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference ref : ex.getPath()) {
            if (ref.getPropertyName() == null) {
                path.append('[').append(ref.getIndex()).append(']');
            } else {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(ref.getPropertyName());
            }
        }
        return path.toString();
    }

    private ResponseEntity<ApiError> errorResponse(HttpStatus status, String code, String error, String detail, String detailFallback) {
        String message = (detail == null || detail.isBlank()) ? detailFallback : detail;
        return ResponseEntity.status(status).body(ApiError.of(status.value(), code, error, message));
    }
}
