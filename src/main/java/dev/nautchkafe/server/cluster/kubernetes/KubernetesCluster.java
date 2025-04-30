package dev.nautchkafe.server.cluster.kubernetes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import dev.nautchkafe.server.cluster.global.ServerSpecification;

public interface KubernetesCluster {

    Function<ServerSpecification, CompletableFuture<String>> createGameServerPod();
    
    Function<String, CompletableFuture<Void>> removeGameServerPod();
}