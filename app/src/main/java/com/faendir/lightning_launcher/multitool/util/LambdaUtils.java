package com.faendir.lightning_launcher.multitool.util;

import android.support.annotation.NonNull;

import java8.lang.FunctionalInterface;
import java8.util.Optional;
import java8.util.function.BiConsumer;
import java8.util.function.BiFunction;
import java8.util.function.Consumer;
import java8.util.function.Function;
import java8.util.function.Supplier;

/**
 * @author F43nd1r
 * @since 30.04.2017
 */

public final class LambdaUtils {
    private LambdaUtils() {
    }

    @NonNull
    public static Runnable ignoreExceptions(ExceptionalRunnable<?> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable ignored) {
            }
        };
    }

    @NonNull
    public static <T> Consumer<T> ignoreExceptions(ExceptionalConsumer<T, ?> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable ignored) {
            }
        };
    }

    @NonNull
    public static <T, U> BiConsumer<T, U> ignoreExceptions(ExceptionalBiConsumer<T, U, ?> consumer) {
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Throwable ignored) {
            }
        };
    }

    @NonNull
    public static <T> Supplier<Optional<T>> exceptionToOptional(ExceptionalSupplier<T, ?> supplier) {
        return () -> {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        };
    }

    @NonNull
    public static <T, R> Function<T, Optional<R>> exceptionToOptional(ExceptionalFunction<T, R, ?> function) {
        return t -> {
            try {
                return Optional.ofNullable(function.apply(t));
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        };
    }

    @NonNull
    public static <T, U, R> BiFunction<T, U, Optional<R>> exceptionToOptional(ExceptionalBiFunction<T, U, R, ?> function) {
        return (t, u) -> {
            try {
                return Optional.ofNullable(function.apply(t, u));
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        };
    }

    @FunctionalInterface
    public interface ExceptionalRunnable<E extends Throwable> {
        void run() throws E;
    }

    @FunctionalInterface
    public interface ExceptionalConsumer<T, E extends Throwable> {
        void accept(T t) throws E;
    }

    @FunctionalInterface
    public interface ExceptionalBiConsumer<T, U, E extends Throwable> {
        void accept(T t, U u) throws E;
    }

    @FunctionalInterface
    public interface ExceptionalSupplier<T, E extends Throwable> {
        T get() throws E;
    }

    public interface ExceptionalFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;
    }

    public interface ExceptionalBiFunction<T, U, R, E extends Throwable> {
        R apply(T t, U u) throws E;
    }
}
