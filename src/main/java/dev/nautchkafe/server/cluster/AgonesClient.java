package dev.nautchkafe.server.cluster;

import dev.agones.sdk.AgonesSDK;
import dev.nautchkafe.server.cluster.global.ServerAllocator;
import dev.nautchkafe.server.cluster.global.ServerTerminator;
import dev.nautchkafe.server.cluster.global.ServerMonitor;
import dev.nautchkafe.server.cluster.global.ServerSpecification;
import dev.nautchkafe.server.cluster.global.ServerAllocationDetails;
import dev.nautchkafe.server.cluster.global.ServerIdentifier;
import dev.nautchkafe.server.cluster.global.ServerHeartbeat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import java.util.Objects;
import java.util.concurrent.CompletionException;

final class AgonesClient implements ServerAllocator, 
    ServerTerminator, ServerMonitor {
    
    private final AgonesSDK agonesSdk;
    private final Map<ServerIdentifier, ServerHeartbeat> serverStatusMap;

    AgonesClient(final AgonesSDK agonesSdk) {
        this.agonesSdk = Objects.requireNonNull(agonesSdk);
        this.serverStatusMap = new ConcurrentHashMap<>();
    }

    @Override
    public Function<ServerSpecification, CompletableFuture<ServerAllocationDetails>> allocateGameServer() {
        return serverSpec -> CompletableFuture.supplyAsync(() -> {
            try {
                agonesSdk.allocate();
                
                final ServerIdentifier newServerId = generateServerId();
                final ServerAllocationDetails allocation = createAllocationDetails(newServerId, serverSpec);
                final ServerHeartbeat initialStatus = createInitialStatus(newServerId, serverSpec);
                
                serverStatusMap.put(newServerId, initialStatus);
                return allocation;
            } catch (final Exception e) {
                throw new CompletionException("Failed to allocate game server", e);
            }
        });
    }

    @Override
    public Function<ServerIdentifier, CompletableFuture<Void>> terminateGameServer() {
        return serverId -> CompletableFuture.runAsync(() -> {
            try {
                agonesSdk.shutdown();
                serverStatusMap.remove(serverId);
            } catch (final Exception e) {
                throw new CompletionException("Failed to terminate game server", e);
            }
        });
    }

    @Override
    public Supplier<Stream<ServerHeartbeat>> monitorGameServers() {
        return () -> Collections.unmodifiableCollection(serverStatusMap.values()).stream();
    }

    private ServerIdentifier generateServerId() {
        return new ServerIdentifier("mc-" + UUID.randomUUID());
    }

    private ServerAllocationDetails createAllocationDetails(
        final ServerIdentifier serverId,
        final ServerSpecification spec
    ) {
        return new ServerAllocationDetails(
            serverId,
            spec,
            serverId.value() + ".nautchkafe.dev",
            25565,
            "pod-" + serverId.value()
        );
    }

    private ServerHeartbeat createInitialStatus(
        final ServerIdentifier serverId,
        final ServerSpecification spec
    ) {
        return new ServerHeartbeat(serverId, spec, 0, true);
    }
}