package dev.nautchkafe.server.cluster.global;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ServerMonitor {
    
    Supplier<Stream<ServerHeartbeat>> monitorGameServers();
}