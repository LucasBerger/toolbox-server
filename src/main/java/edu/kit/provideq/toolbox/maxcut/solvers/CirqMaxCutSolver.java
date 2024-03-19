package edu.kit.provideq.toolbox.maxcut.solvers;

import edu.kit.provideq.toolbox.PythonProcessRunner;
import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.SubRoutinePool;
import edu.kit.provideq.toolbox.meta.ProblemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@link ProblemType#MAX_CUT} solver using a Cirq QAOA implementation.
 */
@Component
public class CirqMaxCutSolver extends MaxCutSolver {
  private final ApplicationContext context;
  private final String scriptDir;

  @Autowired
  public CirqMaxCutSolver(
      @Value("${cirq.directory.max-cut}") String scriptDir,
      ApplicationContext context) {
    this.scriptDir = scriptDir;
    this.context = context;
  }

  @Override
  public String getName() {
    return "Cirq MaxCut";
  }

  @Override
  public void solve(String input, Solution<String> solution,
                    SubRoutinePool subRoutinePool) {
    var processResult = context.getBean(
        PythonProcessRunner.class,
        scriptDir,
        "max_cut_cirq.py")
        .addProblemFilePathToProcessCommand()
        .addSolutionFilePathToProcessCommand()
        .run(getProblemType(), solution.getId(), input);

    if (!processResult.success()) {
      solution.setDebugData(processResult.output());
      solution.abort();
      return;
    }

    solution.setSolutionData(processResult.output());
    solution.complete();
  }
}
