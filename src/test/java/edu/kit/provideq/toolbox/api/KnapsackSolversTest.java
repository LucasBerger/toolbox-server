package edu.kit.provideq.toolbox.api;

import static edu.kit.provideq.toolbox.knapsack.KnapsackConfiguration.KNAPSACK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import edu.kit.provideq.toolbox.SolutionStatus;
import edu.kit.provideq.toolbox.meta.ProblemManagerProvider;
import edu.kit.provideq.toolbox.meta.ProblemSolver;
import edu.kit.provideq.toolbox.meta.ProblemState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
public class KnapsackSolversTest {
  @Autowired
  private WebTestClient client;

  @Autowired
  private ProblemManagerProvider problemManagerProvider;

  @BeforeEach
  void beforeEach() {
    this.client = this.client.mutate()
        .responseTimeout(Duration.ofSeconds(20))
        .build();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  Stream<Arguments> provideArguments() {
    var problemManager = problemManagerProvider.findProblemManagerForType(KNAPSACK).get();

    return ApiTestHelper.getAllArgumentCombinations(problemManager)
            .map(list -> Arguments.of(list.get(0), list.get(1)));
  }

  @ParameterizedTest
  @MethodSource("provideArguments")
  void testKnapsackSolver(ProblemSolver<String, String> solver, String input) {
    var problem = ApiTestHelper.createProblem(client, solver, input, KNAPSACK);
    assertEquals(ProblemState.SOLVED, problem.getState());
    assertNotNull(problem.getSolution());
    assertEquals(SolutionStatus.SOLVED, problem.getSolution().getStatus());
  }
}