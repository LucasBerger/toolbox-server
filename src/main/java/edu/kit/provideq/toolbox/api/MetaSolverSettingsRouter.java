package edu.kit.provideq.toolbox.api;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import edu.kit.provideq.toolbox.MetaSolverProvider;
import edu.kit.provideq.toolbox.meta.MetaSolver;
import edu.kit.provideq.toolbox.meta.ProblemType;
import edu.kit.provideq.toolbox.meta.setting.MetaSolverSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * This router handles requests to the GET {@code /meta-solver/{problemType}/settings} endpoints.
 * Responses are generated from the settings reported by the available meta-solvers
 * (see {@link MetaSolver#getSettings()}).
 */
@Configuration
@EnableWebFlux
public class MetaSolverSettingsRouter {
  private final MetaSolverProvider metaSolverProvider;

  @Autowired
  public MetaSolverSettingsRouter(MetaSolverProvider metaSolverProvider) {
    this.metaSolverProvider = metaSolverProvider;
  }

  @Bean
  RouterFunction<ServerResponse> getMetaSolverSettingsRoutes() {
    return metaSolverProvider.getMetaSolvers().stream()
        .map(this::defineMetaSolverSettingsRouteForMetaSolver)
        .reduce(RouterFunction::and)
        .orElseThrow();
  }

  private RouterFunction<ServerResponse> defineMetaSolverSettingsRouteForMetaSolver(
      MetaSolver<?, ?, ?> metaSolver) {
    var problemType = metaSolver.getProblemType();
    return route().GET(
        getRouteForProblemType(problemType),
        req -> handleMetaSolverSettingsRouteForMetaSolver(metaSolver),
        ops -> ops
            .operationId(getRouteForProblemType(problemType))
            .tag(problemType.getId())
            .response(responseBuilder()
                .responseCode(String.valueOf(HttpStatus.OK.value()))
                .content(contentBuilder()
                    .mediaType(APPLICATION_JSON_VALUE)
                    .array(arraySchemaBuilder().schema(
                        schemaBuilder().implementation(MetaSolverSetting.class)))
                )
            )
    ).build();
  }

  private Mono<ServerResponse> handleMetaSolverSettingsRouteForMetaSolver(
      MetaSolver<?, ?, ?> metaSolver) {
    return ok().body(Mono.just(metaSolver.getSettings()), new ParameterizedTypeReference<>() {
    });
  }

  private String getRouteForProblemType(ProblemType type) {
    return "/meta-solver/settings/" + type.getId();
  }
}