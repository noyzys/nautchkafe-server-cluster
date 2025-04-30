import io.fabric8.kubernetes.api.model.Deployment;
import io.vavr.collection.List;

interface IArenaServerDeployment {

    Deployment createServerDeployment(Arena arena);

    void deleteServerDeployment(String serverId);

    void scaleServerDeployment(String serverId, int replicas);

    List<Deployment> listDeployments();
}
