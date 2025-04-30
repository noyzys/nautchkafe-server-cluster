import io.vavr.control.Option;
import java.util.function.Consumer;

final class ArenaCoordinator implements IArenaServer {

    private final ArenaDispatcherClient kubernetesManager;
    private final ArenaServerCreator arenaService;

    ArenaCoordinator(ArenaDispatcherClient kubernetesManager, ArenaServerCreator arenaService) {
        this.kubernetesManager = kubernetesManager;
        this.arenaService = arenaService;
    }

    @Override
    public void createServerForArena(Arena arena) {
        String serverId = "server-" + arena.getId();
        
        kubernetesManager.createServerDeployment(arena);
        System.out.println("Created new server for arena: " + arena);
    }

    @Override
    public void scaleServerForArena(Arena arena, int replicas) {
        String serverId = "server-" + arena.getId();
        
        kubernetesManager.scaleServerDeployment(serverId, replicas);
        System.out.println("Scaled server for arena: " + arena + " to " + replicas + " replicas.");
    }

    @Override
    public void deleteServerForArena(Arena arena) {
        String serverId = "server-" + arena.getId();
        
        kubernetesManager.deleteServerDeployment(serverId);
        System.out.println("Deleted server for arena: " + arena);
    }

    @Override
    public void processArena(Arena arena, Consumer<ArenaServerManager> operation) {
        operation.accept(this);
    }

    public void createServerIfPresent(Option<Arena> arenaOption) {
        arenaOption.peek(this::createServerForArena);
    }
}
