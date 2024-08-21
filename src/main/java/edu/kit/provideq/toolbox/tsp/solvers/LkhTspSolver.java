package edu.kit.provideq.toolbox.tsp.solvers;

import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.meta.SubRoutineResolver;
import edu.kit.provideq.toolbox.process.PythonProcessRunner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Classical Solver for the TSP Problem that uses the LKH-3 heuristics.
 */
@Component
public class LkhTspSolver extends TspSolver {

  private final String scriptDir;
  private final ApplicationContext context;
  private final String solverBinary;

  @Autowired
  public LkhTspSolver(
      /*
       * uses the LKH script dir because it is the same as LKH-3 for VRP
       * (LKH can solve VRP and TSP)
       */
      @Value("${custom.lkh.directory}") String scriptDir,
      @Value("${custom.lkh.solver}") String solverBinary,
      ApplicationContext context) {
    this.scriptDir = scriptDir;
    this.solverBinary = solverBinary;
    this.context = context;
  }

  @Override
  public String getName() {
    return "LKH-3 TSP Solver";
  }

  @Override
  public Mono<Solution<String>> solve(String input, SubRoutineResolver subRoutineResolver) {
    var solution = new Solution<String>();
    var processResult = context.getBean(
            PythonProcessRunner.class,
            scriptDir,
            "vrp_lkh.py",
            new String[] {"--lkh-instance", solverBinary}
        )
        .addProblemFilePathToProcessCommand()
        .addSolutionFilePathToProcessCommand("--output-file", "%s")
        .problemFileName("problem.vrp")
        .solutionFileName("problem.sol")
        .run(getProblemType(), solution.getId(), adaptInput(input));

    return Mono.just(processResult.applyTo(solution));
  }

  /**
   * LKH-3 solver has an issue when the "EOF" tag is used in a TSP file.
   * This method removes this substring.
   *
   * @param originalInput original input of the TSP problem
   * @return adapted input with "EOF"
   */
  private String adaptInput(String originalInput) {
    String inputAsVrp = originalInput;
    if (inputAsVrp.endsWith("EOF")) {
      inputAsVrp = inputAsVrp.replaceAll("EOF$", "");
    }
    return inputAsVrp;
  }
}
