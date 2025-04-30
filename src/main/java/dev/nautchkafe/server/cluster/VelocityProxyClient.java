package dev.nautchkafe.server.cluster;

import dev.nautchkafe.server.cluster.global.ServerIdentifier;
import dev.nautchkafe.server.cluster.global.ServerAllocationDetails;
import dev.nautchkafe.server.cluster.global.ServerInfo;
import dev.nautchkafe.server.cluster.global.ProxyServerRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.util.Objects;

final class VelocityProxyClient implements ProxyServerRegistry {
    
    private final ProxyServer velocityProxy;
    private final Map<ServerIdentifier, ServerInfo> registeredServers;

    VelocityProxyClient(final ProxyServer velocityProxy) {
        this.velocityProxy = Objects.requireNonNull(velocityProxy);
        this.registeredServers = new ConcurrentHashMap<>();
    }

    @Override
    public Function<ServerAllocationDetails, CompletableFuture<Void>> registerWithProxy() {
        return allocation -> CompletableFuture.runAsync(() -> {
            final ServerInfo serverInfo = createServerInfo(allocation);
            
            velocityProxy.registerServer(serverInfo);
            registeredServers.put(allocation.serverId(), serverInfo);
        });
    }

    @Override
    public Function<ServerIdentifier, CompletableFuture<Void>> unregisterFromProxy() {
        return serverId -> CompletableFuture.runAsync(() -> {
            final ServerInfo serverInfo = registeredServers.remove(serverId);
            if (serverInfo != null) {
                velocityProxy.unregisterServer(serverInfo);
            }
        });
    }

    private ServerInfo createServerInfo(final ServerAllocationDetails allocation) {
        return new ServerInfo(allocation.serverId().value(),
            new InetSocketAddress(allocation.hostname(), allocation.portNumber())
        );
    }
}