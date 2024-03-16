package edu.kit.provideq.toolbox.vrp.solvers;

import edu.kit.provideq.toolbox.BinaryProcessRunner;
import edu.kit.provideq.toolbox.ProcessResult;
import edu.kit.provideq.toolbox.ProcessRunner;
import edu.kit.provideq.toolbox.PythonProcessRunner;
import edu.kit.provideq.toolbox.ResourceProvider;
import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.SubRoutinePool;
import edu.kit.provideq.toolbox.meta.Problem;
import edu.kit.provideq.toolbox.meta.ProblemType;
import edu.kit.provideq.toolbox.meta.SubRoutineDefinition;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@link ProblemType#SAT} solver using a GAMS implementation.
 */
@Component
public class LkhVrpSolver extends VrpSolver {
  private final String scriptDir;
  private final ApplicationContext context;

  @Autowired
  public LkhVrpSolver(
    @Value("${lkh.directory.vrp}") String scriptDir,
      ApplicationContext context) {
    this.scriptDir = scriptDir;
    this.context = context;
  }

  @Override
  public String getName() {
    return "LKH VRP Solver";
  }

  @Override
  public boolean canSolve(Problem<String> problem) {
    return problem.type() == ProblemType.VRP;
  }

  @Override
  public void solve(Problem<String> problem, Solution<String> solution,
                    SubRoutinePool subRoutinePool) {
      
      var processResult = context.getBean(
          BinaryProcessRunner.class,
          scriptDir,
          "../venv/bin/python",
          "vrp_lkh.py"
        )
        .addProblemFilePathToProcessCommand()
        .addSolutionFilePathToProcessCommand("--output-file", "%s")
        .problemFileName("problem.vrp")
        .solutionFileName("problem.sol")
        .run(problem.type(), solution.getId(), problem.problemData());
        
      if (!processResult.success()) {
        solution.setDebugData(processResult.output());
        solution.abort();
        return;
      }
  
      solution.setSolutionData(processResult.output());
      solution.complete();
  }
}