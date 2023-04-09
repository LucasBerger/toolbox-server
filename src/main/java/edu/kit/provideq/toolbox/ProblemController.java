package edu.kit.provideq.toolbox;

import edu.kit.provideq.toolbox.meta.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract Controller, offers generic post and get methods
 *
 * @param <ProblemFormatType>  the type in which problem input is expected to arrive
 * @param <SolutionFormatType> the type in which a solution will be formatted
 * @param <SolverType>         the type of solver that is to be used to solve a problem
 */
@Component
@RestController
public abstract class ProblemController<ProblemFormatType, SolutionFormatType, SolverType extends ProblemSolver<ProblemFormatType, SolutionFormatType>> {
    @Lazy
    @Autowired
    private ProblemControllerProvider problemControllerProvider;

    @Autowired
    private ApplicationContext context;

    public abstract ProblemType getProblemType();

    public abstract MetaSolver<SolverType> getMetaSolver();

    public SolutionHandle solve(SolveRequest<ProblemFormatType> request) {
        Solution<SolutionFormatType> solution = SolutionManager.createSolution();
        Problem<ProblemFormatType> problem = new Problem<>(request.requestContent, getProblemType());

        SolverType solver = getMetaSolver()
                .getSolver(request.requestedSolverId)
                .orElseGet(() -> getMetaSolver().findSolver(problem));

        solution.setSolverName(solver.getName());

        SubRoutinePool subRoutinePool =
                request.requestedSubSolveRequests == null
                        ? context.getBean(SubRoutinePool.class)
                        : context.getBean(SubRoutinePool.class,
                        request.requestedSubSolveRequests
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> (Function<Object, Solution>) (content -> {
                                            ProblemController problemController = problemControllerProvider.getProblemController(e.getKey());
                                            SolveRequest solveRequest = e.getValue();
                                            solveRequest.requestContent = content;
                                            return (Solution) problemController.solve(solveRequest);
                                        }))));

        long start = System.currentTimeMillis();
        solver.solve(problem, solution, subRoutinePool);
        long finish = System.currentTimeMillis();

        solution.setExecutionMilliseconds(finish - start);

        return solution;
    }

    public SolutionHandle getSolution(long id) {
        var solution = SolutionManager.getSolution(id);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Unable to find solution process with id %d", id));
        }

        return solution;
    }

    public SolverType getSolver(String id) {
        Optional<SolverType> solver = getMetaSolver()
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

    public List<ProblemDefinition> getSubRoutines(String id) {
        SolverType solver = getSolver(id);
        return solver.getSubRoutines();
    }
}
