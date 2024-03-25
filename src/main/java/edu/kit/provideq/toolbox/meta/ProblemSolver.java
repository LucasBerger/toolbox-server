package edu.kit.provideq.toolbox.meta;

import edu.kit.provideq.toolbox.Solution;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * A problem solver provides information about its own suitability to solve a given problem.
 * It can solve problems and write the resulting data in a provided {@link Solution} object.
 *
 * @param <InputT> the input type of the problems this solver can solve.
 */
public interface ProblemSolver<InputT, ResultT> {
  /**
   * Returns an id which is unique to the solver.
   *
   * @return id of the solver
   */
  default String getId() {
    return getClass().getName();
  }

  /**
   * Returns the name of the solver.
   *
   * @return name of the solver
   */
  String getName();

  /**
   * Returns the sub problems used to solver this problem.
   *
   * @return list of sub problems
   */
  default List<SubRoutineDefinition<?, ?>> getSubRoutines() {
    return Collections.emptyList();
  }

  /**
   * Solves a given problem instance, current status and final results as well as debug information
   * is stored in the provided {@link Solution} object.
   *
   * @param input the problem instance to solve.
   * @param subRoutineResolver interface to execute sub-routines with.
   * @return the {@link Solution} in which all resulting information is stored.
   */
  Mono<Solution<ResultT>> solve(
      InputT input,
      SubRoutineResolver subRoutineResolver
  );

  /**
   * Returns the problem type that can be solved by this problem solver.
   */
  ProblemType<InputT, ResultT> getProblemType();
}
