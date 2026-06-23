package net.earthmc.emcapi.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Make {
    public static <T> T make(final T initial, final Consumer<T> initializer) {
        initializer.accept(initial);
        return initial;
    }

    public static <T> T make(final T initial, final Function<T, T> initializer) {
        return initializer.apply(initial);
    }

    public static <T> T make(final Supplier<T> supplier) {
        return supplier.get();
    }
}
