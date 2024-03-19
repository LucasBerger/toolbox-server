package edu.kit.provideq.toolbox.meta.setting;

import java.util.List;
import javax.annotation.Nullable;

public class Select<T> extends MetaSolverSetting {
  public List<T> options;
  @Nullable
  public T selectedOption;

  public Select(String name, String title, List<T> options) {
    this(name, title, options, null);
  }

  public Select(String name, String title, List<T> options, T selectedOption) {
    super(name, title, MetaSolverSettingType.SELECT);

    this.options = options;
    this.selectedOption = selectedOption;
  }
}
