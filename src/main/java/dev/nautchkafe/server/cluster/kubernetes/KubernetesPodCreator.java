package dev.nautchkafe.server.cluster.kubernetes;

import dev.nautchkafe.server.cluster.global.ServerSpecification;
import dev.nautchkafe.server.cluster.global.ServerAllocationDetails;
import dev.nautchkafe.server.cluster.global.ServerMonitor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.concurrent.CompletionException;
import java.util.Objects;

import io.kubernetes.client.openapi.models.Pod;
import io.kubernetes.client.openapi.models.PodBuilder;
import io.kubernetes.client.openapi.models.ContainerBuilder;
import io.kubernetes.client.openapi.models.ContainerPortBuilder;
import io.kubernetes.client.openapi.models.EnvVarBuilder;
import io.kubernetes.client.openapi.models.Container;
import io.kubernetes.client.util.Watch;

final class KubernetesPodCreator implements KubernetesCluster {

    private final KubernetesClient kubernetesClient;
    private final Function<ServerSpecification, Pod> podTemplateFactory;

    KubernetesPodCreator(
        final KubernetesClient kubernetesClient,
        final Function<ServerSpecification, Pod> podTemplateFactory
    ) {
        this.kubernetesClient = Objects.requireNonNull(kubernetesClient);
        this.podTemplateFactory = Objects.requireNonNull(podTemplateFactory);
    }

    @Override
    public Function<ServerSpecification, CompletableFuture<String>> createGameServerPod() {
        return serverSpec -> CompletableFuture.supplyAsync(() -> {
            try {
                final Pod pod = podTemplateFactory.apply(serverSpec);
                kubernetesClient.pods().resource(pod).create();
                
                return pod.getMetadata().getName();
            } catch (final Exception e) {
                throw new CompletionException("Failed to create Kubernetes pod", e);
            }
        });
    }

    @Override
    public Function<String, CompletableFuture<Void>> removeGameServerPod() {
        return podName -> CompletableFuture.runAsync(() -> {
            try {
                kubernetesClient.pods().withName(podName).delete();
            } catch (final Exception e) {
                throw new CompletionException("Failed to delete Kubernetes pod", e);
            }
        });
    }

    public static Pod createDefaultPodTemplate(final ServerSpecification spec) {
        return new PodBuilder()
            .withNewMetadata()
                .withGenerateName("mc-pod-")
                .addToLabels("app", "minecraft")
                .addToLabels("version", spec.minecraftVersion())
                .addToLabels("type", spec.type().name())
            .endMetadata()
            .withNewSpec()
                .addToContainers(new ContainerBuilder()
                    .withName("minecraft-server")
                    .withImage("spigot:" + spec.minecraftVersion())
                    .addToPorts(new ContainerPortBuilder()
                        .withContainerPort(25565)
                        .build())
                    .addToEnv(new EnvVarBuilder()
                        .withName("SERVER_TYPE")
                        .withValue(spec.type().name())
                        .build())
                    .build())
            .endSpec()
            .build();
    }
}