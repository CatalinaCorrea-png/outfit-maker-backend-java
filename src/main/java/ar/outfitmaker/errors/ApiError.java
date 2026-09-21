package ar.outfitmaker.errors;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record ApiError(
        int status,
        String code,
        String error,
        String detail,
        String timestamp
) {
    public static ApiError of(int status, String code, String error, String detail) {
        return new ApiError(
                status,
                code,
                error,
                detail,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }
}
