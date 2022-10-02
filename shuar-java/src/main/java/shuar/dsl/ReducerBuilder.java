package shuar.dsl;

import shuar.Shuar.Reducer;

public interface ReducerBuilder<O, R> extends DSL {
    Reducer<O, R> buildReducer();
}
