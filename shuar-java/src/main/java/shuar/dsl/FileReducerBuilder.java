package shuar.dsl;

import shuar.Shuar.Reducer;
import shuar.reducer.file.FileReducer;

import java.io.File;
import java.net.URI;
import java.util.List;

public interface FileReducerBuilder extends ReducerBuilder<String, URI> {
    class Config extends DSLConfig {
        String fileName;

        @Override
        List<Rule> rules() {
            return List.of(
                    new Rule(() -> fileName != null, "File name not specified")
            );
        }

        private static Config get(FileReducerBuilder instance) {
            return DSLConfig.get(instance, FileReducerBuilder.class, Config::new);
        }
    }

    @Override
    default Reducer<String, URI> buildReducer() {
        final Config config = Config.get(this).validate();
        return new FileReducer(new File(config.fileName));
    }

    default void outputFilename(String fileName) {
        Config.get(this).fileName = fileName;
    }
}
