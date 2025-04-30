package dev.nautchkafe.server.cluster.global;

public record ServerHeartbeat(
    ServerIdentifier serverId,
    ServerSpecification specification,
    int currentPlayers,
    boolean isOperational
) {
}