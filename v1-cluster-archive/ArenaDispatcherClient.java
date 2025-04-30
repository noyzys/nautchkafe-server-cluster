import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.*;
import io.vavr.collection.List;

final class ArenaDispatcherClient implements IArenaServerDeployment {

    private final KubernetesClient client;
    private final String namespace;

    KubernetesManagerImpl(String namespace) {
        this.client = new DefaultKubernetesClient();
        this.namespace = namespace;
    }

    @Override
    public Deployment createServerDeployment(Arena arena) {
        String serverId = "server-" + arena.getId();
        Container container = new ContainerBuilder()
                .withName("minecraft-server")
                .withImage("itzz/spigot:latest")
                .addNewPort().withContainerPort(25565).endPort()
                .build();

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata().withName(serverId).endMetadata()
                .withNewSpec()
                .withReplicas(1)
                .withSelector(new LabelSelectorBuilder().withMatchLabels("app", "minecraft").build())
                .withTemplate(new PodTemplateSpecBuilder()
                        .withNewMetadata().addToLabels("app", "minecraft").endMetadata()
                        .withNewSpec().addToContainers(container).endSpec())
                .endSpec()
                .build();

        return client.apps().deployments().inNamespace(namespace).create(deployment);
    }

    @Override
    public void deleteServerDeployment(String serverId) {
        client.apps().deployments().inNamespace(namespace).withName(serverId).delete();
    }

    @Override
    public void scaleServerDeployment(String serverId, int replicas) {
        Deployment deployment = client.apps().deployments().inNamespace(namespace).withName(serverId).get();
        
        if (deployment != null) {
            deployment.getSpec().setReplicas(replicas);
            client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
        }
    }

    @Override
    public List<Deployment> listDeployments() {
        return client.apps().deployments().inNamespace(namespace)
                .list().getItems();
    }
}
