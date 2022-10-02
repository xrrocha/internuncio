package shuar.dsl;

import shuar.Shuar.Mapper;

public interface MapperBuilder<I, O> extends DSL {
    Mapper<I, O> buildMapper();
}
