package edu.kit.provideq.toolbox.vrp.clusterer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import edu.kit.provideq.toolbox.Solution;
import edu.kit.provideq.toolbox.SubRoutinePool;
import edu.kit.provideq.toolbox.meta.Problem;
import edu.kit.provideq.toolbox.meta.setting.MetaSolverSetting;
import edu.kit.provideq.toolbox.process.BinaryProcessRunner;
import edu.kit.provideq.toolbox.process.MultiFileProcessResultReader;

@Component
public class TwoPhaseClusterer extends VrpClusterer {
    
    @Autowired
    public TwoPhaseClusterer(
        @Value("${vrp.directory}") String binaryDir,
        @Value("${vrp.bin.meta-solver}") String binaryName,
        ApplicationContext context) {
            super(binaryDir, binaryName, context);
    }


    @Override
    public String getName() {
        return "Two Phase VRP To TSP Clusterer (Classical)";
    }

    @Override
    public void solve(Problem<String> problem, Solution<String> solution, SubRoutinePool subRoutinePool, List<MetaSolverSetting> settings) {
        
        // cluster with tsp/two-phase clustering
        var processResult = context.getBean(
          BinaryProcessRunner.class,
          binaryDir,
          binaryName,
          "partial",
          new String[] { "cluster", "%1$s", "tsp", "--build-dir", "%3$s/.vrp"}
        )
        .problemFileName("problem.vrp")
        .run(problem.type(), solution.getId(), problem.problemData(), new MultiFileProcessResultReader("./.vrp/problem_*.vrp"));
        
        if (!processResult.success()) {
          solution.setDebugData(processResult.errorOutput().orElse("Unknown error occurred."));
          solution.abort();
          return;
        }

        // solve the clusters
        solveClusters(problem, solution, subRoutinePool, processResult);
    }

}
