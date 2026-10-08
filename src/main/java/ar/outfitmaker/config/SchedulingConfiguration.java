package ar.outfitmaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita los @Scheduled del proyecto (hoy: el barrido de fotos huérfanas). */
@Configuration
@EnableScheduling
public class SchedulingConfiguration {
}
