package dev.nautchkafe.server.cluster;

import dev.agones.sdk.AgonesSDK;
import com.velocitypowered.api.proxy.ProxyServer;
import io.fabric8.kubernetes.client.KubernetesClient;

final class MinecraftServerClusterApp {

    public static void main(String[] args) {
        final AgonesSDK agonesSdk = new AgonesSDK();
        final KubernetesClient k8sClient = new KubernetesClientBuilder().build();
        final ProxyServer velocityProxy = ProxyServer.init();
 
        final ServerAllocator allocator = new AgonesClient(agonesSdk);
        final KubernetesClient podManager = new KubernetesPodCreator(
            k8sClient,
            KubernetesPodCreator::createDefaultPodTemplate
        );
        final ProxyServerRegistry proxyRegistry = new VelocityProxyClient(velocityProxy);

        final MinecraftServerClusterDaemon daemon = new MinecraftServerClusterDaemon(
            allocator,
            allocator, 
            allocator, 
            podManager,
            proxyRegistry
        );
        
        daemon.initialize();
        registerShutdownHook(daemon, agonesSdk, k8sClient);
    }

    private registerShutdownHook(
        final MinecraftServerClusterDaemon daemon,
        final AgonesSDK agonesSdk,
        final KubernetesClient k8sClient
    ) {
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> {
            ServerTry.of(() -> daemon.shutdown())
               .andThen(() -> ServerTry.of(agonesSdk::close))
               .andThen(() -> ServerTry.of(k8sClient::close))
               .onFailure(ex -> System.err.println("Shutdown error: " + ex.getMessage()));
        }));
    }
}