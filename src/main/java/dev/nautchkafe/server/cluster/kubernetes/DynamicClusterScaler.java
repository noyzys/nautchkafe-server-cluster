package dev.nautchkafe.server.cluster.kubernetes;

import dev.nautchkafe.server.cluster.global.ServerSpecification;
import dev.nautchkafe.server.cluster.global.ServerHeartbeat;
import dev.nautchkafe.server.cluster.global.GameServerAllocator;
import dev.nautchkafe.server.cluster.global.KubernetesCluster;
import dev.nautchkafe.server.cluster.global.ProxyServerRegistry;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Objects;

public final class DynamicClusterScaler implements ClusterAutoScalingStrategy {

    private final ServerAllocator serverAllocator;
    private final KubernetesCluster cluster;
    private final ProxyServerRegistry proxyRegistry;

    public DynamicClusterScaler(
        final ServerAllocator serverAllocator,
        final KubernetesCluster cluster,
        final ProxyServerRegistry proxyRegistry
    ) {
        this.serverAllocator = Objects.requireNonNull(serverAllocator);
        this.cluster = Objects.requireNonNull(cluster);
        this.proxyRegistry = Objects.requireNonNull(proxyRegistry);
    }

    @Override
    public BiConsumer<Predicate<ServerHeartbeat>, IntUnaryOperator> executeScaling() {
        return (serverFilter, scalingFunction) -> {
            final Map<ServerSpecification, Long> serverCounts = serverAllocator.monitorGameServers().get()
                .filter(serverFilter)
                .collect(Collectors.groupingBy(
                    ServerHeartbeat::specification,
                    Collectors.counting()
                ));

            serverCounts.forEach((spec, count) -> {
                final int desiredCount = scalingFunction.applyAsInt(count);
                adjustServerCount(spec, desiredCount);
            });
        };
    }

    private void adjustServerCount(final ServerSpecification spec, final int desiredCount) {
        final int currentCount = (int) serverAllocator.monitorGameServers().get()
                .filter(status -> status.specification().equals(spec))
                .count();

        int difference = desiredCount - currentCount;
        if (difference != 0) {
            (difference > 0 ? this::scaleUp : this::scaleDown).accept(spec, Math.abs(difference));
        }
    }


    private void scaleUp(final ServerSpecification spec, final int count) {
        IntStream.range(0, count).forEach(i -> 
            serverAllocator.allocateGameServer().apply(spec)
                .thenCompose(allocation -> cluster.createGameServerPod().apply(spec))
                .thenCompose(podName -> proxyRegistry.registerWithProxy().apply(allocation))
                .exceptionally(ex -> {
                    System.err.println("Scaling up failed: " + ex.getMessage());
                    return null;
                })
        );
    }

    private void scaleDown(final ServerSpecification spec, final int count) {
        serverAllocator.monitorGameServers().get()
            .filter(status -> status.specification().equals(spec))
            .limit(count)
            .map(ServerHeartbeat::serverId)
            .forEach(serverId -> 
                proxyRegistry.unregisterFromProxy().apply(serverId)
                    .thenCompose(__ -> serverAllocator.terminateGameServer().apply(serverId))
                    .thenCompose(__ -> cluster.removeGameServerPod().apply("pod-" + serverId.value()))
                    .exceptionally(ex -> {
                        System.err.println("Scaling down failed: " + ex.getMessage());
                        return null;
                    })
            );
    }
}