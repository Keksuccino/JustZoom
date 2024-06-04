package de.keksuccino.justzoom.util;

import de.keksuccino.konkrete.config.Config;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public abstract class AbstractOptions {

    @SuppressWarnings("unused")
    public static class Option<T> {

        protected final Config config;
        protected final String key;
        protected final T defaultValue;
        protected final String category;

        public Option(@NotNull Config config, @NotNull String key, @NotNull T defaultValue, @NotNull String category) {
            this.config = Objects.requireNonNull(config);
            this.key = Objects.requireNonNull(key);
            this.defaultValue = Objects.requireNonNull(defaultValue);
            this.category = Objects.requireNonNull(category);
            this.register();
        }

        protected void register() {
            boolean unsupported = false;
            try {
                if (this.defaultValue instanceof Integer i) {
                    this.config.registerValue(this.key, i, this.category);
                } else if (this.defaultValue instanceof Double d) {
                    this.config.registerValue(this.key, d, this.category);
                } else if (this.defaultValue instanceof Long l) {
                    this.config.registerValue(this.key, l, this.category);
                } else if (this.defaultValue instanceof Float f) {
                    this.config.registerValue(this.key, f, this.category);
                } else if (this.defaultValue instanceof Boolean b) {
                    this.config.registerValue(this.key, b, this.category);
                } else if (this.defaultValue instanceof String s) {
                    this.config.registerValue(this.key, s, this.category);
                } else {
                    unsupported = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (unsupported) throw new UnsupportedOptionTypeException("Tried to register Option with unsupported type: " + this.key + " (" + this.defaultValue.getClass().getName() + ")");
        }

        @NotNull
        public T getValue() {
            return this.config.getOrDefault(this.key, this.defaultValue);
        }

        public Option<T> setValue(T value) {
            try {
                if (value == null) value = this.getDefaultValue();
                if (value instanceof Integer i) {
                    this.config.setValue(this.key, i);
                } else if (value instanceof Double d) {
                    this.config.setValue(this.key, d);
                } else if (value instanceof Long l) {
                    this.config.setValue(this.key, l);
                } else if (value instanceof Float f) {
                    this.config.setValue(this.key, f);
                } else if (value instanceof Boolean b) {
                    this.config.setValue(this.key, b);
                } else if (value instanceof String s) {
                    this.config.setValue(this.key, s);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return this;
        }

        public Option<T> resetToDefault() {
            this.setValue(null);
            return this;
        }

        @NotNull
        public T getDefaultValue() {
            return this.defaultValue;
        }

        @NotNull
        public String getKey() {
            return this.key;
        }

    }

    /**
     * Thrown when trying to register an Option with an unsupported type.
     */
    @SuppressWarnings("unused")
    public static class UnsupportedOptionTypeException extends RuntimeException {

        public UnsupportedOptionTypeException() {
            super();
        }

        public UnsupportedOptionTypeException(String msg) {
            super(msg);
        }

    }

}
