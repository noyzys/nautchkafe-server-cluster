import io.vavr.control.Option;
import io.vavr.collection.List;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Bukkit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

record Arena(
    String id, 
    World world, 
    ArenaCuboid cuboid, 
    Path schematicPath
) {

    public static Arena create(String id, World world, Location center, 
        int sizeX, int sizeY, int sizeZ, String schematicFileName) {
        ArenaCuboid cuboid = ArenaCuboid.generate(center, sizeX, sizeY, sizeZ);
        Path schematicPath = Paths.get("schematics", schematicFileName); 

        return new Arena(id, world, cuboid, schematicPath);
    }

    public boolean isPlayerInside(Location playerLocation) {
        return cuboid.contains(playerLocation);
    }

    public Path getSchematicPath() {
        return schematicPath;
    }

    public Option<Path> generateSchematic() {
        // logika generowania schematu z fawe api
        return Option.of(schematicPath);
    }

    @Override
    public String toString() {
        return String.format("Arena{id='%s', world='%s', cuboid=%s, schematicPath='%s'}", id, world.getName(), cuboid, schematicPath);
    }
}