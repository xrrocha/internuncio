package shuar.dsl;

import shuar.Shuar.Source;

public interface SourceBuilder<I> extends DSL {
    Source<I> buildSource();
}
