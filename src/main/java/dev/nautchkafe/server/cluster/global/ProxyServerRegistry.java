package dev.nautchkafe.server.cluster.global;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ProxyServerRegistry {
    
    Function<ServerAllocationDetails, CompletableFuture<Void>> registerWithProxy();
    
    Function<ServerIdentifier, CompletableFuture<Void>> unregisterFromProxy();
}