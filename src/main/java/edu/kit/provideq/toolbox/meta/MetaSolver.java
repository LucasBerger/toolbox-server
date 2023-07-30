package edu.kit.provideq.toolbox.meta;

import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.SolutionManager;
import edu.kit.provideq.toolbox.SolveRequest;
import edu.kit.provideq.toolbox.SubRoutinePool;
import edu.kit.provideq.toolbox.meta.setting.MetaSolverSetting;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Decides which known {@link ProblemSolver} is suited best for a given problem,
 * manages known solvers.
 *
 * @param <ProblemT> the type of the problem input of {@link ProblemSolver}
 * @param <SolutionT> the type of the solution output of {@link ProblemSolver}
 * @param <SolverT> the type of {@link ProblemSolver} this metasolver is to manage
 */
public abstract class MetaSolver<
        ProblemT,
        SolutionT,
        SolverT extends ProblemSolver<ProblemT, SolutionT>> {

  private final SolutionManager<SolutionT> solutionManager = new SolutionManager<>();
  private ApplicationContext context;

  protected Set<SolverT> solvers = new HashSet<>();
  private ProblemType problemType;

  public MetaSolver() {
  }

  public MetaSolver(ProblemType problemType, List<SolverT> problemSolvers) {
    solvers.addAll(problemSolvers);
    this.problemType = problemType;
  }

  @SafeVarargs
  public MetaSolver(ProblemType problemType, SolverT... problemSolvers) {
    solvers.addAll(List.of(problemSolvers));
    this.problemType = problemType;
  }

  @Autowired
  public void setContext(ApplicationContext context) {
    this.context = context;
  }

  /**
   * Adds a new solver to this meta solvers list of known solvers.
   *
   * @param problemSolver the new problem solver
   * @return true in case the addition was successful, false otherwise
   */
  public boolean registerSolver(SolverT problemSolver) {
    return solvers.add(problemSolver);
  }

  /**
   * Removes a solver from this meta solvers list of known solvers.
   *
   * @param problemSolver the solver
   * @return true in case the removal was successful, false otherwise
   */
  public boolean unregisterSolver(SolverT problemSolver) {
    return solvers.remove(problemSolver);
  }

  /**
   * Provides the best suited known solver this meta solver is aware of for a given problem.
   *
   * @param problem the problem the meta solver is to check its solvers by
   * @return the best suited solver, null in case no suitable solver was found
   */
  public abstract SolverT findSolver(
          Problem<ProblemT> problem,
          List<MetaSolverSetting> metaSolverSettings);

  public Optional<SolverT> getSolver(String solverId) {
    if (solverId == null) {
      return Optional.empty();
    }

    return solvers.stream()
        .filter(solver -> solver.getId().equals(solverId))
        .findFirst();
  }

  /**
   * Provides a list of all solvers registered on this meta solver.
   *
   * @return set of all solvers
   */
  public Set<SolverT> getAllSolvers() {
    return new HashSet<>(solvers);
  }

  public List<MetaSolverSetting> getSettings() {
    return List.of();
  }

  @Override
  public int hashCode() {
    return this.solvers.hashCode();
  }

  public ProblemType getProblemType() {
    return problemType;
  }

  public SolutionManager<SolutionT> getSolutionManager() {
    return solutionManager;
  }

  public Solution<SolutionT> solve(SolveRequest<ProblemT> request) {
    Solution<SolutionT> solution = this.getSolutionManager().createSolution();
    Problem<ProblemT> problem = new Problem<>(request.requestContent, this.getProblemType());

    SolverT solver = this
            .getSolver(request.requestedSolverId)
            .orElseGet(() -> this.findSolver(problem, request.requestedMetaSolverSettings));

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
}
