package berlin.tu.ssas.group8.model.trafficLight;

import lombok.Getter;
import lombok.ToString;

import lombok.NonNull;

/**
 * This class represents a geo-location via latitude and longitude
 */
@Getter
@ToString
public class Location {
    private final double latitude;
    private final double longitude;

    /**
     * creates a Location with the specified latitude and longitude
     *
     * @param latitude  latitude of the Location, has to be between -90 and 90 degree
     * @param longitude longitude of the Location, has to be between -180 and 180 degree
     * @throws IllegalArgumentException If latitude and/or longitude are out of range
     */
    public Location(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude value");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude value");
        }
        if (longitude == -180) {
            longitude = 180; // this way we do not have two possible ways to describe the same location
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }


    /**
     * Calculates the distance between this Location and the specified Location in meters
     *
     * @param l Location to which the distance should be calculated, can't be null
     * @return A primitive Integer representing the distance to the specified Location in meters
     */
    public int distanceTo(@NonNull Location l) { // according to https://www.movable-type.co.uk/scripts/latlong.html
        float r = 6371000; // Earth radius
        double f1 = this.latitude * Math.PI / 180;
        double f2 = l.getLatitude() * Math.PI / 180;

        double deltaf = (l.getLatitude() - this.latitude) * Math.PI / 180;
        double deltal = (l.getLongitude() - this.longitude) * Math.PI / 180;

        double a = Math.sin(deltaf / 2) * Math.sin(deltaf / 2) + Math.cos(f1) * Math.cos(f2) * Math.sin(deltal / 2) * Math.sin(deltal / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) (r * c);
    }

    /**
     * Checks if the specified Location is within the specified range of this Location
     *
     * @param l     Location to which to check if it is in this Location's range
     * @param range Range radius of this Location
     * @return A Boolean representing if the Location is within the range
     * @throws IllegalArgumentException If range is not positive
     */
    public boolean inRangeOf(@NonNull Location l, int range) {
        if (range <= 0)
            throw new IllegalArgumentException("Range is not positive");

        return (this.distanceTo(l) < range);
    }

}
