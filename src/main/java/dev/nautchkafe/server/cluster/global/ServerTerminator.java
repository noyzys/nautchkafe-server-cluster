package dev.nautchkafe.server.cluster.global;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ServerTerminator {
    
    Function<ServerIdentifier, CompletableFuture<Void>> terminateGameServer();
}