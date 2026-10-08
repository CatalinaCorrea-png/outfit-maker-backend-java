package ar.outfitmaker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Sirve la carpeta de fotos en /uploads/**.
 * Registra StorageProperties acá y no en AppConfiguration: los @WebMvcTest cargan los
 * WebMvcConfigurer pero no las @Configuration comunes, y sin esto no levantarían.
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfiguration implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    public StorageConfiguration(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file:///C:/.../outfit-maker-uploads/ — sin la "/" final Spring no resuelve los archivos
        String location = Path.of(storageProperties.dir()).toAbsolutePath().toUri().toString();
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
