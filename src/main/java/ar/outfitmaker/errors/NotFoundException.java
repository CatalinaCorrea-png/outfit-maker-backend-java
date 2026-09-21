package ar.outfitmaker.errors;

/**
 * El recurso solicitado no fue encontrado en el sistema.
 * (e.g., buscar un usuario por ID que no existe en la base de datos).
 */
public class NotFoundException extends AppException {

    public NotFoundException(String code, String msg) {
        super(code, msg);
    }
}
