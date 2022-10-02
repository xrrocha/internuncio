package shuar.dsl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface DSL {

    abstract class DSLConfig {

        abstract List<Rule> rules();

        @SuppressWarnings("unchecked")
        final <D extends DSLConfig> D validate() {
            final var violations = rules().stream()
                    .filter(rule -> !rule.condition.get())
                    .map(rule -> rule.errorMessage)
                    .toList();
            if (violations.isEmpty()) {
                return (D) this;
            } else {
                throw new IllegalArgumentException(String.join(". ", violations));
            }
        }

        @SuppressWarnings("unchecked")
        static <C extends DSLConfig> C get(DSL dsl, Class<? extends DSL> clazz, Supplier<C> newConfig) {
            final var configs = state.computeIfAbsent(dsl, d -> new HashMap<>());
            return (C) configs.computeIfAbsent(clazz, c -> newConfig.get());
        }

        private static final Map<DSL, Map<Class<? extends DSL>, DSLConfig>> state = new HashMap<>();

        record Rule(Supplier<Boolean> condition, String errorMessage) {
        }
    }
}
