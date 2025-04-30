import io.vavr.control.Option;
import java.util.function.Consumer;

interface IArenaServer {

    void createServerForArena(Arena arena);

    void scaleServerForArena(Arena arena, int replicas);

    void deleteServerForArena(Arena arena);

    void processArena(Arena arena, Consumer<ArenaCoordinator> operation);
}
