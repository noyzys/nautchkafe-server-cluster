package dev.nautchkafe.server.cluster.global;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface ServerTry<TYPE> {

    TYPE get() throws Exception;

    static <TYPE> ServerTry<TYPE> of(final ServerTry<TYPE> action) {
        return action;
    }

    default <RESULT> ServerTry<RESULT> flatMap(final Function<? super TYPE, ? extends ServerTry<RESULT>> mapper) {
        try {
            return mapper.apply(get());
        } catch (final Exception ex) {
            return () -> { throw ex; };
        }
    }

    default ServerTry<Void> andThen(final Runnable action) {
        try {
            get();
            return () -> {
                action.run();
                return null;
            };
        } catch (final Exception ex) {
            return () -> { throw ex; };
        }
    }

    default void onSuccess(final Consumer<TYPE> consumer) {
        try {
            consumer.accept(get());
        } catch (final Exception ignored) {
        }
    }

    default void onFailure(final Consumer<Exception> handler) {
        try {
            get();
        } catch (final Exception ex) {
            handler.accept(ex);
        }
    }
}
