package ar.outfitmaker.errors;

/**
 * La petición no pudo completarse debido a un conflicto con el estado actual del recurso.
 * (e.g., intentar crear un registro que ya existe, violando una restricción de unicidad).
 */
public class ConflictException extends AppException {

    public ConflictException(String code, String msg) {
        super(code, msg);
    }
}
