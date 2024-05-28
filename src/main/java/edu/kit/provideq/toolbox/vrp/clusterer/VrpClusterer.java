package edu.kit.provideq.toolbox.vrp.clusterer;

import static edu.kit.provideq.toolbox.SolutionStatus.INVALID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import edu.kit.provideq.toolbox.meta.*;
import edu.kit.provideq.toolbox.process.BinaryProcessRunner;
import edu.kit.provideq.toolbox.process.ProcessResult;
import edu.kit.provideq.toolbox.vrp.VrpConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import edu.kit.provideq.toolbox.ResourceProvider;
import edu.kit.provideq.toolbox.Solution;
import reactor.core.publisher.Mono;

/**
 * A solver for SAT problems.
 */
public abstract class VrpClusterer implements ProblemSolver<String, String> {

    protected final ApplicationContext context;
    protected final String binaryDir;
    protected final String binaryName;
    protected ResourceProvider resourceProvider;

    protected VrpClusterer(
        String binaryDir,
        String binaryName,
        ApplicationContext context
    ) {
        this.binaryDir = binaryDir;
        this.binaryName = binaryName;
        this.context = context;
    }

    @Override
    public ProblemType<String, String> getProblemType() {
        return ClusterVrpConfiguration.CLUSTER_VRP;
    }

    @Autowired
    public void setResourceProvider(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public List<SubRoutineDefinition<?, ?>> getSubRoutines() {
        return List.of(
            new SubRoutineDefinition<>(VrpConfiguration.VRP,
                "How should the clusters be solved?")
        );
    }

    public Mono<Solution<String>> solveClusters(
            Solution<String> solution,
            SubRoutineResolver resolver,
            ProcessResult<HashMap<Path, String>> cluster
    ) {

        // Retrieve the problem directory
        String problemDirectoryPath;
        try {
            problemDirectoryPath = resourceProvider
                .getProblemDirectory(getProblemType(), solution.getId())
                .getAbsolutePath();
        } catch (IOException e) {
            solution.setDebugData("Failed to retrieve problem directory.");
            solution.abort();
            return Mono.just(solution);
        }

        //TODO: refactor the following code, needs to work with the new model
        /*
        // solve each subproblem
        for (var subproblemEntry : cluster.output().orElse(new HashMap<>()).entrySet()) {
            var vrpSolver = subRoutinePool.<String, String>getSubRoutine(ProblemType.VRP);
            var vrpSolution = vrpSolver.apply(subproblemEntry.getValue());
            if (vrpSolution.getStatus() == INVALID) {
                solution.setDebugData(vrpSolution.getDebugData());
                solution.abort();
                return;
            }

            var fileName = subproblemEntry.getKey().getFileName().toString().replace(".vrp", ".sol");

            var solutionFilePath = Path.of(problemDirectoryPath, ".vrp", fileName);

            try {
				Files.writeString(solutionFilePath, vrpSolution.getSolutionData());
			} catch (IOException e) {
				solution.setDebugData("Failed to write solution file. Path: " + solutionFilePath.toString());
                solution.abort();
                return;
			}
        }

        // combine the solution paths
        var combineProcessRunner = context.getBean(
          BinaryProcessRunner.class,
          binaryDir,
          binaryName,
          "solve",
          new String[] { "%1$s", "cluster-from-file", "solution-from-file", "--build-dir", "%3$s/.vrp", "--solution-dir", "%3$s/.vrp", "--cluster-file", "%3$s/.vrp/problem.map"}
        )
        .problemFileName("problem.vrp")
        .solutionFileName("problem.sol")
        .run(getProblemType(), solution.getId(), input);
        
        
        if (!combineProcessRunner.success()) {
            solution.setDebugData(combineProcessRunner.errorOutput().orElse("Unknown error occurred."));
            solution.abort();
            return;
        }
    
        solution.setSolutionData(combineProcessRunner.output().orElse("Empty Solution"));
        */

        solution.complete();
        return Mono.just(solution);
    }
}
