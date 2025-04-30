package dev.nautchkafe.server.cluster.kubernetes;

import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

import dev.nautchkafe.server.cluster.global.ServerHeartbeat;

interface ClusterAutoScalingStrategy {
    
    BiConsumer<Predicate<ServerHeartbeat>, IntUnaryOperator> executeScaling();
}