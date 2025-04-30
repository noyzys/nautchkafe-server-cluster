package dev.nautchkafe.server.cluster.global;

public record ServerSpecification(
    String minecraftVersion,
    ServerType type,
    int maxPlayerCapacity,
    String worldName
) {

    public ServerSpecification {
        Objects.requireNonNull(minecraftVersion, "Version cannot be null");
        Objects.requireNonNull(type, "Server type cannot be null");
        
        if (maxPlayerCapacity <= 0) {
            throw new IllegalArgumentException("Max players must be positive");
        }
    }
}