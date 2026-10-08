package ar.outfitmaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dónde se guardan las fotos subidas y con qué URL se sirven.
 *
 * @param dir           carpeta en disco (fuera del repo)
 * @param publicBaseUrl prefijo de las URLs que se devuelven al front, sin "/" final
 */
@ConfigurationProperties("storage")
public record StorageProperties(
        String dir,
        String publicBaseUrl
) {
}
