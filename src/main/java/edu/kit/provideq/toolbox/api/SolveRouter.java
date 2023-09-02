package edu.kit.provideq.toolbox.api;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import edu.kit.provideq.toolbox.MetaSolverProvider;
import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.SolutionHandle;
import edu.kit.provideq.toolbox.SolveRequest;
import edu.kit.provideq.toolbox.meta.MetaSolver;
import edu.kit.provideq.toolbox.meta.ProblemType;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * This router handles problem-solving requests to the GET and POST {@code /solve/{problemType}}
 * endpoints.
 * Requests are validated and relayed to the corresponding {@link MetaSolver}.
 */
@Configuration
@EnableWebFlux
public class SolveRouter {
  private final MetaSolverProvider metaSolverProvider;
  private final Validator validator;

  public SolveRouter(MetaSolverProvider metaSolverProvider, Validator validator) {
    this.metaSolverProvider = metaSolverProvider;
    this.validator = validator;
  }

  @Bean
  RouterFunction<ServerResponse> getSolveRoutes() {
    return metaSolverProvider.getMetaSolvers().stream()
        .map(this::defineRouteForMetaSolver)
        .reduce(RouterFunction::and)
        .orElseThrow(); // we should always have at least one route or the toolbox is useless
  }

  private RouterFunction<ServerResponse> defineRouteForMetaSolver(MetaSolver<?, ?, ?> metaSolver) {
    var problemType = metaSolver.getProblemType();
    return route().POST(
        getSolveRouteForProblemType(problemType),
        accept(APPLICATION_JSON),
        req -> handleRouteForMetaSolver(metaSolver, req),
        ops -> ops
            .operationId(getSolveRouteForProblemType(problemType))
            .tag(problemType.getId())
            .requestBody(requestBodyBuilder()
                .content(contentBuilder()
                    .schema(schemaBuilder().implementation(
                        metaSolver.getProblemType().getRequestType()))
                    .mediaType(APPLICATION_JSON_VALUE)
                )
                .required(true)
            )
            .response(responseBuilder()
                .responseCode(String.valueOf(HttpStatus.OK.value()))
                .implementation(SolutionHandle.class)
            )
    ).build();
  }

  private <ProblemT, SolutionT> Mono<ServerResponse> handleRouteForMetaSolver(
      MetaSolver<ProblemT, SolutionT, ?> metaSolver, ServerRequest req) {
    var solutionMono = req
        .bodyToMono(new ParameterizedTypeReference<SolveRequest<ProblemT>>() {
        })
        .doOnNext(this::validate)
        .map(metaSolver::solve)
        .map(Solution::toStringSolution);
    return ok().body(solutionMono, new ParameterizedTypeReference<>() {
    });
  }

  private <ProblemT> void validate(SolveRequest<ProblemT> request) {
    Errors errors = new BeanPropertyBindingResult(request, "request");
    validator.validate(request, errors);
    if (errors.hasErrors()) {
      throw new ServerWebInputException(errors.toString());
    }
  }

  @Bean
  RouterFunction<ServerResponse> getSolutionRoutes() {
    return metaSolverProvider.getMetaSolvers().stream()
        .map(this::defineSolutionRouteForMetaSolver)
        .reduce(RouterFunction::and)
        .orElseThrow(); // we should always have at least one route or the toolbox is useless
  }

  private RouterFunction<ServerResponse> defineSolutionRouteForMetaSolver(
      MetaSolver<?, ?, ?> metaSolver) {
    var problemType = metaSolver.getProblemType();
    return route().GET(
        // FIXME this is intentionally SOLVE instead of SOLUTION to avoid breaking things
        //  but maybe we should switch the name at some point
        getSolveRouteForProblemType(problemType),
        accept(APPLICATION_JSON),
        req -> handleSolutionRouteForMetaSolver(metaSolver, req),
        ops -> ops
            .operationId(getSolutionRouteForProblemType(problemType))
            .tag(problemType.getId())
            .parameter(parameterBuilder().in(ParameterIn.QUERY).name("id"))
            .response(responseBuilder()
                .responseCode(String.valueOf(HttpStatus.OK.value()))
                .implementation(SolutionHandle.class)
            )
    ).build();
  }

  private Mono<ServerResponse> handleSolutionRouteForMetaSolver(MetaSolver<?, ?, ?> metaSolver,
                                                                ServerRequest req) {
    var solution = req.queryParam("id")
        .map(Long::parseLong)
        .map(solutionId -> metaSolver.getSolutionManager().getSolution(solutionId))
        .map(Solution::toStringSolution)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Could not find a solution for this problem with this solution id!"));

    // yes, solution is of type `Solution<String>`.
    // No idea why `toStringSolution` returns `SolutionHandle`
    return ok().body(Mono.just(solution), new ParameterizedTypeReference<Solution<String>>() {
    });
  }

  private String getSolveRouteForProblemType(ProblemType type) {
    return "/solve/" + type.getId();
  }

  private String getSolutionRouteForProblemType(ProblemType type) {
    return "/solution/" + type.getId();
  }
}
