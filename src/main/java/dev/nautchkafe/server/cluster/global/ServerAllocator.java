package dev.nautchkafe.server.cluster.global;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ServerAllocator {
    
    Function<ServerSpecification, CompletableFuture<ServerAllocationDetails>> allocateGameServer();
}