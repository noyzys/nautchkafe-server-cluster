package dev.nautchkafe.server.cluster.global;

public record ServerIdentifier(
    String value
) {

    public ServerIdentifier {
        Objects.requireNonNull(value, "Server ID cannot be null");
    }
}