package ar.outfitmaker.errors;

/**
 * La petición no pudo procesarse porque contiene datos inválidos o no cumple
 * con las reglas de negocio definidas.
 * (e.g., intentar transferir un monto negativo, un campo obligatorio vacío,
 * o un valor fuera de rango permitido).
 */
public class BusinessException extends AppException {
    public BusinessException(String code, String msg) {
        super(code, msg);
    }
}
