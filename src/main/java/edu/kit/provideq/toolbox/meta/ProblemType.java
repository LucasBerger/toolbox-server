package edu.kit.provideq.toolbox.meta;

import edu.kit.provideq.toolbox.SolveRequest;
import edu.kit.provideq.toolbox.featuremodel.SolveFeatureModelRequest;
import edu.kit.provideq.toolbox.maxcut.SolveMaxCutRequest;
import edu.kit.provideq.toolbox.sat.SolveSatRequest;

/**
 * The type of problem to solve.
 */
public enum ProblemType {
  /**
   * A satisfiability problem:
   * For a given boolean formula, check if there is an interpretation that satisfies the formula.
   */
  SAT("sat", SolveSatRequest.class),

  /**
   * An optimization problem:
   * For a given graph, find the optimal separation of vertices that maximises the cut crossing edge
   * weight sum.
   */
  MAX_CUT("max-cut", SolveMaxCutRequest.class),

  /**
   * A searching problem:
   * For a given feature model, check for Void Feature Model, Dead Features,
   * False-Optional Features, Redundant Constraints.
   * <a href="https://sdq.kastel.kit.edu/publications/pdfs/kowal2016b.pdf">More information</a>
   */
  FEATURE_MODEL_ANOMALY("feature-model-anomaly", SolveFeatureModelRequest.class),
  ;

  private final String id;
  private final Class<? extends SolveRequest<?>> requestType;

  ProblemType(String id, Class<? extends SolveRequest<?>> requestType) {
    this.id = id;
    this.requestType = requestType;
  }

  /**
   * Returns a unique identifier for this problem type.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the java class representing the body of a REST request to solve a problem of this type.
   */
  public Class<? extends SolveRequest<?>> getRequestType() {
    return requestType;
  }
}
