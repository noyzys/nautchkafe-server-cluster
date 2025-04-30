import io.vavr.control.Option;
import org.bukkit.Location;

import java.util.function.BiFunction;

record ArenaCuboid(Location corner1, Location corner2) {

    public static ArenaCuboid generate(Location center, int sizeX, int sizeY, int sizeZ) {
        Location corner1 = center.clone().subtract(sizeX / 2.0, sizeY / 2.0, sizeZ / 2.0);
        Location corner2 = center.clone().add(sizeX / 2.0, sizeY / 2.0, sizeZ / 2.0);
        return new ArenaCuboid(corner1, corner2);
    }

    public boolean contains(Location point) {
        return inRange(point.getX(), corner1.getX(), corner2.getX()) &&
               inRange(point.getY(), corner1.getY(), corner2.getY()) &&
               inRange(point.getZ(), corner1.getZ(), corner2.getZ());
    }

    public double bountyBox(Location point) {
        BiFunction<Double, Double, Double> distance = (a, b) -> Math.pow(a - b, 2);

        double dx = Math.max(0, Math.min(point.getX(), Math.max(corner1.getX(), corner2.getX())) - point.getX());
        double dy = Math.max(0, Math.min(point.getY(), Math.max(corner1.getY(), corner2.getY())) - point.getY());
        double dz = Math.max(0, Math.min(point.getZ(), Math.max(corner1.getZ(), corner2.getZ())) - point.getZ());

        return Math.sqrt(distance.apply(dx, 0.0) + distance.apply(dy, 0.0) + distance.apply(dz, 0.0));
    }

    public Option<Location> findPointIfInside(Location point) {
        return contains(point) ? Option.of(point) : Option.none();
    }

    private boolean inRange(double value, double min, double max) {
        double lower = Math.min(min, max);
        double upper = Math.max(min, max);
        return value >= lower && value <= upper;
    }

    @Override
    public String toString() {
        return String.format("Cuboid[(%s), (%s)]", corner1, corner2);
    }
}
