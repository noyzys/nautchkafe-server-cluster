package dev.nautchkafe.server.cluster.global;

public record ServerType(
    String name, 
    String lobbyType, 
    String limboType
) {
    
    public ServerType {
        Objects.requireNonNull(name, "Server type name cannot be null");
    }
}