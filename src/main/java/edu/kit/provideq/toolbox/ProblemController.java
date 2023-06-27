package edu.kit.provideq.toolbox;

import edu.kit.provideq.toolbox.meta.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract Controller, offers generic post and get methods
 *
 * @param <ProblemT>  the type in which problem input is expected to arrive
 * @param <SolutionT> the type in which a solution will be formatted
 * @param <SolverT>         the type of solver that is to be used to solve a problem
 */
@ComponentSolverType
@RestController
public abstract class ProblemController<ProblemT, SolutionT, SolverT extends ProblemSolver<ProblemT, SolutionT>> {
  private ApplicationContext context;

  public abstract ProblemType getProblemType();

  public abstract MetaSolver<SolverT> getMetaSolver();

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    this.context = context;
  }

  public Solution<SolutionT> solve(SolveRequest<ProblemT> request) {
    Solution<SolutionT> solution = SolutionManager.createSolution();
    Problem<ProblemT> problem = new Problem<>(request.requestContent, getProblemType());

    SolverT solver = getMetaSolver()
        .getSolver(request.requestedSolverId)
        .orElseGet(() -> getMetaSolver().findSolver(problem, request.requestedMetaSolverSettings));

    solution.setSolverName(solver.getName());

    SubRoutinePool subRoutinePool =
        request.requestedSubSolveRequests == null
            ? context.getBean(SubRoutinePool.class)
            : context.getBean(SubRoutinePool.class, request.requestedSubSolveRequests);

    long start = System.currentTimeMillis();
    solver.solve(problem, solution, subRoutinePool);
    long finish = System.currentTimeMillis();

    solution.setExecutionMilliseconds(finish - start);

    return solution;
  }

  public Solution<SolutionT> findSolution(long id) {
    var solution = SolutionManager.getSolution(id);
    if (solution == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format("Unable to find solution process with id %d", id));
    }

    return (Solution<SolutionT>) solution;
  }

  public SolverT getSolver(String id) {
    Optional<SolverT> solver = getMetaSolver()
        .getAllSolvers()
        .stream()
        .filter(s -> id.equals(s.getId()))
        .findFirst();

    if (solver.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format("Unable to find solver %s", id));
    }

    return solver.get();
  }

  public Set<ProblemSolverInfo> getSolvers() {
    return getMetaSolver()
        .getAllSolvers()
        .stream()
        .map(s -> new ProblemSolverInfo(s.getId(), s.getName()))
        .collect(Collectors.toSet());
  }

  public List<SubRoutineDefinition> getSubRoutines(String id) {
    SolverT solver = getSolver(id);
    return solver.getSubRoutines();
  }
}
