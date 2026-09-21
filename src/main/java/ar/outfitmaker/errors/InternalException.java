package ar.outfitmaker.errors;

/**
 * Ocurrió un error inesperado e interno en el servidor que no está relacionado
 * con la entrada del usuario.
 * (e.g., fallo de conexión a la base de datos, un servicio externo no disponible).
 */
public class InternalException extends AppException {

    public InternalException(String code, String msg) {
        super(code, msg);
    }
}