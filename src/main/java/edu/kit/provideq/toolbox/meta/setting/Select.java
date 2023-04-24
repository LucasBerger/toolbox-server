package edu.kit.provideq.toolbox.meta.setting;

import javax.annotation.Nullable;
import java.util.List;

public class Select<T> extends MetaSolverSetting {
    public List<T> options;
    @Nullable
    public T selectedOption;

    public Select(String name, List<T> options) {
        this(name, options, null);
    }

    public Select(String name, List<T> options, T selectedOption) {
        super(name, MetaSolverSettingType.SELECT);

        this.options = options;
        this.selectedOption = selectedOption;
    }
}
