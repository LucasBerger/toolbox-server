package edu.kit.provideq.toolbox.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Spring configuration to enable CORS between the API and the web frontend.
 */
@Configuration
@EnableWebFlux
public class CorsConfiguration implements WebFluxConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*");
  }
}
