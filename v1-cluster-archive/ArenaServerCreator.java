import io.vavr.control.Option;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;
import java.util.function.Function;

import org.bukkit.World;
import org.bukkit.Location;

final class ArenaServerCreator implements ILocalArena {

    private final Map<String, Arena> arenaMap = HashMap.empty();

    @Override
    public Option<Arena> findArenaById(String id) {
        return Option.of(arenaMap.get(id));
    }

    @Override
    public Arena createArena(String id, World world, Function<String, Location> centerGenerator, 
                             int sizeX, int sizeY, int sizeZ, String schematicPath) {

        Location center = centerGenerator.apply(id);
        return Arena.generate(id, world, center, sizeX, sizeY, sizeZ, schematicPath);
    }

    public List<Arena> filterArenas(Predicate<Arena> criteria) {
        return arenas.filter(criteria);
    }
}
