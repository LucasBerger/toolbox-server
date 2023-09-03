package edu.kit.provideq.toolbox.maxcut;

import edu.kit.provideq.toolbox.maxcut.solvers.CirqMaxCutSolver;
import edu.kit.provideq.toolbox.maxcut.solvers.GamsMaxCutSolver;
import edu.kit.provideq.toolbox.maxcut.solvers.MaxCutSolver;
import edu.kit.provideq.toolbox.maxcut.solvers.QiskitMaxCutSolver;
import edu.kit.provideq.toolbox.meta.MetaSolver;
import edu.kit.provideq.toolbox.meta.Problem;
import edu.kit.provideq.toolbox.meta.ProblemType;
import edu.kit.provideq.toolbox.meta.setting.MetaSolverSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Simple {@link MetaSolver} for MaxCut problems.
 */
@Component
public class MetaSolverMaxCut extends MetaSolver<String, String, MaxCutSolver> {

  @Autowired
  public MetaSolverMaxCut(QiskitMaxCutSolver qiskitSolver,
                          GamsMaxCutSolver gamsSolver,
                          CirqMaxCutSolver cirqSolver) {
    super(ProblemType.MAX_CUT, qiskitSolver, gamsSolver, cirqSolver);
  }

  @Override
  public MaxCutSolver findSolver(
          Problem<String> problem,
          List<MetaSolverSetting> metaSolverSettings) {
    return (new ArrayList<>(this.solvers)).get((new Random()).nextInt(this.solvers.size()));
  }

  @Override
  public List<String> getExampleProblems() {
    return List.of("""
            graph [
                id 42
                node [
                    id 1
                    label "1"
                ]
                node [
                    id 2
                    label "2"
                ]
                node [
                    id 3
                    label "3"
                ]
                edge [
                    source 1
                    target 2
                ]
                edge [
                    source 2
                    target 3
                ]
                edge [
                    source 3
                    target 1
                ]
            ]"""
    );
  }
}
