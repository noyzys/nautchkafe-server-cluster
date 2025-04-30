package dev.nautchkafe.server.cluster;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import dev.nautchkafe.server.cluster.global.ServerAllocator;
import dev.nautchkafe.server.cluster.global.ServerTerminator;
import dev.nautchkafe.server.cluster.global.ServerMonitor;
import dev.nautchkafe.server.cluster.global.KubernetesCluster;
import dev.nautchkafe.server.cluster.global.ProxyServerRegisServerTry;
import dev.nautchkafe.server.cluster.global.ClusterAutoScalingStrategy;
import dev.nautchkafe.server.cluster.global.ServerSpecification;
import dev.nautchkafe.server.cluster.global.ServerIdentifier;
import dev.nautchkafe.server.cluster.global.ServerHeartbeat;
import dev.nautchkafe.server.cluster.global.ServerType;
import dev.nautchkafe.server.cluster.global.ServerTry;
import dev.nautchkafe.server.cluster.kubernetes.DynamicClusterScaler;

final class MinecraftServerClusterDaemon {
    
    private final ServerAllocator serverAllocator;
    private final ServerTerminator serverTerminator;
    private final ServerMonitor serverMonitor;
    private final KubernetesCluster cluster;
    private final ProxyServerRegisServerTry proxyRegisServerTry;
    private final ClusterAutoScalingStrategy autoScaler;
    private final ExecutorService virtualThreadExecutor;
    private final ScheduledExecutorService scheduler;

    MinecraftServerClusterDaemon(
        final ServerAllocator serverAllocator,
        final ServerTerminator serverTerminator,
        final ServerMonitor serverMonitor,
        final KubernetesCluster cluster,
        final ProxyServerRegisServerTry proxyRegisServerTry
    ) {
        this.serverAllocator = Objects.requireNonNull(serverAllocator);
        this.serverTerminator = Objects.requireNonNull(serverTerminator);
        this.serverMonitor = Objects.requireNonNull(serverMonitor);
        this.cluster = Objects.requireNonNull(cluster);
        this.proxyRegisServerTry = Objects.requireNonNull(proxyRegisServerTry);
        this.autoScaler = new DynamicClusterScaler(serverAllocator, cluster, proxyRegisServerTry);
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.scheduler = Executors.newScheduledThreadPool(4, Thread.ofVirtual().factory());
    }

    void initialize() {
        scheduleHealthChecks();
        scheduleAutoScaling();
        ensureLobbyServers();
    }

    void shutdown() {
        scheduler.close();
        virtualThreadExecutor.close();
        terminateAllServers();
    }

    private void scheduleHealthChecks() {
        scheduler.scheduleAtFixedRate(
            () -> ServerTry.run(this::performHealthChecks)
                .onFailure(ex -> System.err.println("Health check failed: " + ex.getMessage())),
            0, 30, TimeUnit.SECONDS
        );
    }

    private void scheduleAutoScaling() {
        scheduler.scheduleAtFixedRate(
            () -> ServerTry.run(this::performAutoScaling)
                .onFailure(ex -> System.err.println("Auto-scaling failed: " + ex.getMessage())),
            0, 1, TimeUnit.MINUTES
        );
    }

    private void performHealthChecks() {
        serverMonitor.monitorGameServers().get()
            .filter(Predicate.not(ServerHeartbeat::isOperational))
            .map(ServerHeartbeat::serverId)
            .forEach(this::terminateServer);
    }

    private void performAutoScaling() {
        autoScaler.executeScaling().accept(
            status -> (double) status.currentPlayers() / status.specification().maxPlayerCapacity() > 0.8,
            current -> current + 1);

        autoScaler.executeScaling().accept(
            status -> (double) status.currentPlayers() / status.specification().maxPlayerCapacity() < 0.2,
            current -> Math.max(1, current - 1));
    }

    private void ensureLobbyServers() {
        final ServerType lobbyType = new ServerType("lobby", "main", "limbo");
        final ServerSpecification lobbySpec = new ServerSpecification("1.21", lobbyType, 100, "lobby");

        final boolean hasLobby = serverMonitor.monitorGameServers().get()
            .anyMatch(status -> status.specification().type().equals(lobbyType));

        if (!hasLobby) {
            createServer(lobbySpec);
        }
    }

    private void createServer(final ServerSpecification spec) {
        CompletableFuture.supplyAsync(() -> ServerTry.of(() -> serverAllocator.allocateGameServer().apply(spec))
            .flatMap(allocation -> ServerTry.of(() -> cluster.createGameServerPod().apply(spec))
            .flatMap(podName -> ServerTry.of(() -> proxyRegisServerTry.registerWithProxy().apply(allocation)))
            .thenAccept(result -> result.onSuccess(__ -> System.out.println("Created server: " + spec.type().name()))
                .onFailure(ex -> System.err.println("Server creation failed: " + ex.getMessage())));
    }

    private void terminateServer(final ServerIdentifier serverId) {
        CompletableFuture.runAsync(() -> ServerTry.run(() -> proxyRegisServerTry.unregisterFromProxy().apply(serverId).get()))
        .thenRun(() -> ServerTry.run(() -> serverTerminator.terminateGameServer().apply(serverId).get()))
        .thenRun(() -> ServerTry.run(() -> cluster.removeGameServerPod().apply("pod-" + serverId.value()).get()))
        .whenComplete((ignored, ex) -> {
            if (ex != null) {
                System.err.println("Server termination failed: " + ex.getMessage());
            }
        });
    }

    private void terminateAllServers() {
        serverMonitor.monitorGameServers().get()
            .map(ServerHeartbeat::serverId)
            .forEach(this::terminateServer);
    }
}