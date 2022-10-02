package shuar.util;

import java.io.*;
import java.util.List;
import java.util.function.Function;
import java.util.logging.LogManager;

public class Utils {

    public static void initLogger() {
        initLogger("logging.properties");
    }

    public static void initLogger(String resourceName) {
        try {
            LogManager.getLogManager().readConfiguration(openResource(resourceName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readResource(String resourceName) throws IOException {
        return getUnchecked(() -> {
            try (var is = openResource(resourceName)) {
                int charsRead;
                var buffer = new char[4096];
                var sb = new StringBuilder();
                var reader = new InputStreamReader(is);

                while ((charsRead = reader.read(buffer)) > 0) {
                    sb.append(buffer, 0, charsRead);
                }

                return sb.toString();
            }
        });
    }

    public static InputStream openResource(String resourceName) {
        final var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IllegalArgumentException("No such resource: %s".formatted(resourceName));
        }
        return is;
    }

    public static List<String> readLines(File file) {
        return getUnchecked(() -> readLines(new FileReader(file)));
    }

    public static List<String> readLines(Reader reader) {
        return new BufferedReader(reader)
                .lines()
                .toList();
    }

    public interface UncheckedFunction<I, O> {
        O apply(I input) throws Exception;
    }

    public interface UncheckedConsumer<I> {
        void accept(I input) throws Exception;
    }

    public interface UncheckedSupplier<O> {
        O get() throws Exception;
    }

    public interface UncheckedRunnable {
        void run() throws Exception;
    }

    public static <I, O> Function<I, O> unchecked(UncheckedFunction<I, O> block) {
        return input -> {
            try {
                return block.apply(input);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        };
    }

    public static <O> O getUnchecked(UncheckedSupplier<O> block) {
        try {
            return block.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void runUnchecked(UncheckedRunnable block) {
        try {
            block.run();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <I> void consumeUnchecked(I input, UncheckedConsumer<I> block) {
        try {
            block.accept(input);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);

        }
    }
}
