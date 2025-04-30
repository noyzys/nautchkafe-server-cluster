import io.vavr.control.Option;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Bukkit;

final class App {

    public static void main(String[] args) {
        ArenaDispatcherClient arenaClient = new ArenaDispatcherClient("default");
        ArenaServerCreator arenaCreator = new ArenaServerCreator();
        
        ArenaCoordinator arenaServer = new ArenaCoordinator(arenaClient, arenaCreator);

        World world = Bukkit.getWorld("world"); 
        String arenaId = "arena-1";
        Arena arena = arenaCreator.createArena(
            arenaId, 
            world, 
            id -> new Location(world, 100, 64, 100), 
            50,  
            50, 
            50,   
            "arena-1.schematic"
        );

        World world = Bukkit.getWorld("end"); 
        String arenaId = "arena-2";
        Arena arena_east = arenaCreator.createArena(
            arenaId,
            world,
            id -> new Location(world, 100, 64, 100),  
            50,  
            50,  
            50,  
            "arena-2.schematic"
        );
        
        System.out.println("Created arena: " + arena);

        arenaServer.processArena(arena, pod -> {
            pod.createServerForArena(arena);
            pod.createServerForArena(arena_east);
            pod.scaleServerForArena(arena, 2);
        });

        arenaServer.deleteServerForArena(arena);
        arena_east.deleteServerForArena(arena_east);

        Option<Arena> foundArena = arenaCreator.findArenaById(arenaId);
        foundArena.peek(a -> System.out.println("Found arena: " + a));
    }
}
