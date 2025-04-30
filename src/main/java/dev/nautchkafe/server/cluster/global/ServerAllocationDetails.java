package dev.nautchkafe.server.cluster.global;

public record ServerAllocationDetails(
    ServerIdentifier serverId,
    ServerSpecification specification,
    String hostname,
    int portNumber,
    String kubernetesPodName
) {
}