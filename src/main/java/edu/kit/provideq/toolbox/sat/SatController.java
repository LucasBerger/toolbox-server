package edu.kit.provideq.toolbox.sat;

import edu.kit.provideq.toolbox.ProblemController;
import edu.kit.provideq.toolbox.SolutionHandle;
import edu.kit.provideq.toolbox.ProblemSolverInfo;
import edu.kit.provideq.toolbox.meta.MetaSolver;
import edu.kit.provideq.toolbox.meta.ProblemType;
import edu.kit.provideq.toolbox.sat.solvers.SATSolver;
import javax.validation.Valid;
import java.util.Set;

import org.springframework.web.bind.annotation.*;

@RestController
public class SatController extends ProblemController<String, String, SATSolver> {

  private final MetaSolver<SATSolver> metaSolver = new MetaSolverSAT();

  @Override
  public ProblemType getProblemType() {
    return ProblemType.SAT;
  }

  @Override
  public MetaSolver<SATSolver> getMetaSolver() {
    return metaSolver;
  }

  @CrossOrigin
  @PostMapping("/solve/sat")
  public SolutionHandle solveSat(@RequestBody @Valid SolveSatRequest request) {
    return super.solve(request);
  }

  @CrossOrigin
  @GetMapping("/solve/sat")
  public SolutionHandle getSolution(@RequestParam(name = "id", required = true) long id) {
    return super.getSolution(id);
  }

  @CrossOrigin
  @GetMapping("/solvers/sat")
  public Set<ProblemSolverInfo> getSolvers() {
    return super.getSolvers();
  }
}
