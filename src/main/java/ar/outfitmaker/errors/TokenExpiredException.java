package ar.outfitmaker.errors;

/** Token expirado. (401) */
public class TokenExpiredException extends AppException {

    public TokenExpiredException() {
        this("Token vencido");
    }

    public TokenExpiredException(String msg) {
        super("TOKEN_EXPIRED", msg);
    }
}
