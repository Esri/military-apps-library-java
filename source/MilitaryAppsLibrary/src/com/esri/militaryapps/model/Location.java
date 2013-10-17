/*******************************************************************************
 * Copyright 2013 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.model;

import com.esri.militaryapps.util.Utilities;
import java.util.Calendar;

/**
 * A bean that holds a point, a timestamp (Calendar), a speed, and a heading. Location objects
 * are Comparable and are ordered by timestamp ascending when compared or sorted.
 */
public class Location implements Comparable<Location> {

    private double longitude;
    private double latitude;
    private Calendar timestamp;
    private double speed;
    private double heading;

    /**
     * Creates a new Location with no data.
     */
    public Location() {
    }

    /**
     * Creates a new Location with data.
     * @param longitude the longitude.
     * @param latitude the latitude.
     * @param timestamp the timestamp.
     * @param speed the speed.
     * @param heading the heading in degrees.
     */
    public Location(double longitude, double latitude, Calendar timestamp,
            double speed, double heading) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.speed = speed;
        this.heading = heading;
    }

    /**
     * Creates a new Location with data.
     * @param longitude the longitude.
     * @param latitude the latitude.
     * @param timestamp the timestamp.
     * @param speed the speed.
     * @param previousLocation the previous location, used for calculating heading.
     *                         If previousLocation is null, the heading will be zero.
     */
    public Location(double longitude, double latitude, Calendar timestamp,
            double speed, Location previousLocation) {
        this(longitude, latitude, timestamp, speed, 0);
        if (null != previousLocation) {
            double rise = latitude - previousLocation.getLatitude();
            double run = longitude - previousLocation.getLongitude();
            double trigHeadingRadians = Math.atan(rise / run);
            if (0 > run) {
                trigHeadingRadians += Math.PI;
            }
            double compassHeadingRadians = Utilities.toCompassHeadingRadians(trigHeadingRadians);
            final double course = Math.toDegrees(compassHeadingRadians);
            setHeading(course);
        }
    }

    /**
     * @return the timestamp
     */
    public Calendar getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Compares the timestamp of this Location to another, using Calendar.compareTo.
     * @see Comparable
     * @see Calendar
     * @param location the other Location.
     * @return a negative number if this Location's timestamp is earlier than
     *         the other point's timestamp; 0 if the timestamps are the same; and
     *         a positive number otherwise.
     */
    @Override
    public int compareTo(Location location) {
        return this.timestamp.compareTo(location.getTimestamp());
    }

    /**
     * @return the speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the heading in degrees
     */
    public double getHeading() {
        return heading;
    }

    /**
     * @param heading the heading in degrees
     */
    public final void setHeading(double heading) {
        this.heading = heading;
    }

}
