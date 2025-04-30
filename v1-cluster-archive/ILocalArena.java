import io.vavr.control.Option;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.function.Function;

interface ILocalArena {

    Option<Arena> findArenaById(String id);

    Arena createArena(String id, World world, Function<String, Location> centerGenerator, 
                             int sizeX, int sizeY, int sizeZ, String schematicPath);
}
