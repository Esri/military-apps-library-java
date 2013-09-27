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
package com.esri.militaryapps.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities that don't belong in a specific class. This class is a necessary evil. :-)
 */
public class Utilities {

    private static final double FIVE_PI_OVER_TWO = 5.0 * Math.PI / 2.0;
    private static final double TWO_PI = 2.0 * Math.PI;

    /**
     * A DateFormat object for datetimevalid timestamps.
     */
    public static final SimpleDateFormat DATE_FORMAT_GEOMESSAGE =
            new SimpleDateFormat("yyyy-dd-MM' 'HH:mm:ss");
    static {
        DATE_FORMAT_GEOMESSAGE.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * A DateFormat object for military date/time in Zulu time.
     */
    public static final SimpleDateFormat DATE_FORMAT_MILITARY_ZULU =
            new SimpleDateFormat("ddHHmmss'Z 'MMM' 'yy");
    static {
        DATE_FORMAT_MILITARY_ZULU.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    /**
     * A DateFormat object for military date/time in local time.
     */
    public static final SimpleDateFormat DATE_FORMAT_MILITARY_LOCAL =
            new SimpleDateFormat("ddHHmmss'J 'MMM' 'yy");
    private static final Timer localDateFormatTimer = new Timer(true);
    static {
        localDateFormatTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                //Force re-read of OS time zone
                System.getProperties().remove("user.timezone");
                TimeZone.setDefault(null);
                
                //Adjust local format
                TimeZone tz = TimeZone.getDefault();
                DATE_FORMAT_MILITARY_LOCAL.setTimeZone(tz);
                DATE_FORMAT_MILITARY_LOCAL.applyPattern("ddHHmmss'" + getMilitaryTimeZoneCharacter(tz) + " 'MMM' 'yy");
            }
            
        }, 0, 1000 / 24);
    }
    private static final int MILLISECONDS_IN_HOUR = 60 * 60 * 1000;
    private static char getMilitaryTimeZoneCharacter(TimeZone tz) {
        int offset = tz.getOffset(System.currentTimeMillis());
        //If it's not a whole number of hours, just return 'J'
        int offsetHours = offset / MILLISECONDS_IN_HOUR;
        if (0 != offset % MILLISECONDS_IN_HOUR || 12 < offsetHours || -12 > offsetHours) {
            return 'J';
        } else {
            if (0 == offsetHours) {
                return 'Z';
            } else if (0 < offsetHours) {
                char c = (char) ('A' + offsetHours - 1);
                if ('J' <= c) {
                    c += 1;
                }
                return c;
            } else {
                return (char) ('N' - offsetHours - 1);
            }
        }
    }

    /**
     * All non-alphanumeric ASCII characters except '-' and '*', contained in a
     * single String.
     */
    public static final String MIL_2525C_WHITESPACE_CHARS;
    static {
        StringBuilder sb = new StringBuilder();
        for (char c = 0; c < 128; c++) {
            if ((c < '0' || c > '9') && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z')
                    && c != '-' && c != '*') {
                sb.append(c);
            }
        }
        MIL_2525C_WHITESPACE_CHARS = sb.toString();
    }

    /**
     * Protected constructor because Utilities is not meant to be instantiated.
     * Protected instead of private so that an application can extend it for convenience
     * (so that you only have to have one Utilities class).
     */
    protected Utilities() {
    }

    /**
     * Converts a trigonometric angle to a compass heading. In trigonometry, 0
     * radians is east, pi / 2 is north, pi is west, and 3 * pi / 2 is south. In
     * compass headings, 0 radians is north, pi / 2 is east, pi is south, and
     * 3 * pi / 2 is west.
     * @param trigHeading the trigonometric heading, in radians.
     * @return the compass heading, in radians.
     */
    public static double toCompassHeadingRadians(double trigHeadingRadians) {
        double compassHeading = FIVE_PI_OVER_TWO - trigHeadingRadians;
        if (TWO_PI <= compassHeading) {
            compassHeading -= TWO_PI;
        }
        else if(0.0 > compassHeading) {
            compassHeading += TWO_PI;
        }
        return compassHeading;
    }

    /**
     * Calculates the compass bearing from one point to another and returns the
     * result in degrees.
     * @param fromLon the longitude of the location from which the bearing is to
     *                be calculated.
     * @param fromLat the latitude of the location from which the bearing is to
     *                be calculated.
     * @param toLon the longitude of the location to which the bearing is to
     *                be calculated.
     * @param toLat the latitude of the location to which the bearing is to
     *                be calculated.
     * @return the compass bearing from one point to another, in degrees.
     */
    public static double calculateBearingDegrees(double fromLon, double fromLat, double toLon, double toLat) {
        double currentLatRad = fromLat * Math.PI / 180;
        double destinationLatRad = toLat * Math.PI / 180;
        double currentLonRad = fromLon * Math.PI / 180;
        double destinationLonRad = toLon * Math.PI / 180;
        double deltaLonRad = (destinationLonRad - currentLonRad);

        double y = Math.sin(deltaLonRad) * Math.cos(destinationLatRad);
        double x = Math.cos(currentLatRad) * Math.sin(destinationLatRad) - Math.sin(currentLatRad) * Math.cos(destinationLatRad) * Math.cos(deltaLonRad);
        double bearing = Math.atan2(y, x) / Math.PI * 180;

        return (bearing + 360) % 360;
    }

    /**
     * Normalizes an angle in degrees to fall between specified minimum and maximum
     * values.
     * @param angle the angle to normalize.
     * @param min the minimum value.
     * @param max the maximum value.
     * @return an angle equivalent to the input angle, normalized to fall between
     *         the specified minimum and maximum values.
     */
    public static double fixAngleDegrees(double angle, double min, double max) {
        while (angle < min) {
            angle += 360;
        }
        while (angle > max) {
            angle -= 360;
        }
        return angle;
    }
    
    /**
     * Loads a JAR file so the application can access its classes.
     * @param jarPath the JAR file.
     * @throws Exception if the JAR file cannot be loaded.
     */
    public static void loadJar(String jarPath) throws Exception {
        File f = new File(jarPath);
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        method.invoke(sysloader, new Object[]{f.toURI().toURL()});
    }
    
    /**
     * Translates the given color to a color string used by the ArcGIS for the Military
     * GeoEvent Processor schema.
     * @param color the color RGB.
     * @return a color string used by the ArcGIS for the Military GeoEvent Processor
     *         schema.
     */
    public static String getAFMGeoEventColorString(int colorRgb) {
        if (-65536 == colorRgb) {//red
            return "1";
        } else if (-256 == colorRgb) {//yellow
            return "4";
        } else if (-16711936 == colorRgb) {//green
            return "2";
        } else if (-16776961 == colorRgb) {//blue
            return "3";
        } else {
            /**
             * ArcGIS Runtime does not currently support custom chem light colors.
             * But we can send a hex string in case some client can use it.
             */
            String hex = Integer.toHexString(colorRgb & 0xffffff);
            while (hex.length() < 6) {
                hex = "0" + hex;
            }
            return "#" + hex;
        }
    }
    
    /**
     * Converts the string to a best-guess valid MGRS string, if possible.<br/>
     * <br/>
     * This method checks only the pattern, not the coordinate itself. For example,
     * 60CVS1234567890 is valid, but 60CVR1234567890 is not, because zone 60C has
     * a VS square but not a VR square. This method considers both of those strings
     * to be valid, because both of them match the pattern.<br/>
     * <br/>
     * This method will check and try to correct at least the following:
     * <ul>
     *     <li>Digits before zone A, B, Y, Z (correction: omit the numbers)</li>
     *     <li>More than two digits before zone letter (no correction)</li>
     *     <li>Grid zone number higher than 60 (no correction available)</li>
     *     <li>100,000-meter square with more than two letters (no correction)</li>
     *     <li>100,000-meter square with fewer than two letters (no correction available)</li>
     *     <li>Odd number of easting/northing digits (no correction)</li>
     * </ul>
     * TODO this method might go away when fromMilitaryGrid handles bad strings gracefully.
     * @param mgrs the MGRS string.
     * @param referenceMgrs a reference MGRS location for calculating a missing grid
     *        zone identifier. If mgrs does not include a grid zone identifier,
     *        this parameter's grid zone identifier will be prepended to mgrs.
     *        This parameter can be null if mgrs contains a grid zone identifier.
     * @return the string itself, or a best guess at a valid equivalent of the string,
     *         or null if the string is known to be invalid and cannot be converted.
     */
    public static String convertToValidMgrs(String mgrs, String referenceMgrs) {
        if (null == mgrs) {
            return null;
        }
        
        //Remove non-alphanumeric
        mgrs = mgrs.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        //Check for MGRS without grid zone identifier and add it if necessary
        Matcher gzlessMatcher = Pattern.compile("[A-Z]{2}[0-9]*").matcher(mgrs);
        if (null != referenceMgrs && gzlessMatcher.matches()) {
            Matcher gzMatcher = Pattern.compile("[0-9]{0,2}[A-Z]").matcher(referenceMgrs);
            if (gzMatcher.find() && 0 == gzMatcher.start()) {
                mgrs = referenceMgrs.substring(0, gzMatcher.end()) + mgrs;
            }
        }
        
        /**
         * A good MGRS string looks like this:
         * <grid zone ID><2 letters><even number of digits>
         * A grid zone ID looks like this:
         * <1-2 digits><letter C-X>
         * or
         * <letter A, B, Y, or Z>
         * That means every MGRS string looks like this:
         * <0-2 digits><3 letters><even number of digits>
         */
        Pattern pattern = Pattern.compile("[A-Z]+");
        Matcher matcher = pattern.matcher(mgrs);
        if (!matcher.find()) {
            //There are no letters; nothing we can do
            return null;
        }
        Pattern polarPattern = Pattern.compile("[ABYZ][A-Z]{2}[0-9]*");
        if (0 == matcher.start()) {
            //This string starts with letters; make sure it's polar
            if (!polarPattern.matcher(mgrs).matches()) {
                return null;
            }
        } else {
            //If the first letter is A, B, Y, or Z, omit the leading digits
            char firstLetter = mgrs.charAt(matcher.start());
            if ('A' == firstLetter || 'B' == firstLetter || 'Y' == firstLetter || 'Z' == firstLetter) {
                mgrs = mgrs.substring(matcher.start());
                if (!polarPattern.matcher(mgrs).matches()) {
                    return null;
                }
            } else {
                Matcher nonPolarMatcher = Pattern.compile("[0-9]{1,2}[C-X][A-Z]{2}[0-9]*").matcher(mgrs);
                if (!nonPolarMatcher.matches()) {
                    return null;
                }
                //This string starts with numbers; see what they are
                int gridZoneNumber = Integer.parseInt(mgrs.substring(0, matcher.start()));
                if (0 >= gridZoneNumber || 60 < gridZoneNumber) {
                    return null;
                }
            }
        }
        
        //Last thing: return null if there's an odd number of easting/northing digits
        Matcher threeLetters = Pattern.compile("[A-Z]{3}").matcher(mgrs);
        threeLetters.find();
        if (threeLetters.end() < mgrs.length()) {
            String eastingNorthing = mgrs.substring(threeLetters.end());
            if (1 == eastingNorthing.length() % 2) {
                return null;
            }
        }
        
        return mgrs;
    }
    
}
