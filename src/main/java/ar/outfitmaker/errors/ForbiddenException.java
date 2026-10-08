package ar.outfitmaker.errors;

/**
 * El usuario está autenticado pero no tiene permiso sobre el recurso pedido
 * (e.g., editar o borrar una prenda que pertenece a otra cuenta).
 */
public class ForbiddenException extends AppException {
    public ForbiddenException(String code, String msg) {
        super(code, msg);
    }
}
