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

import com.esri.militaryapps.model.DomNodeAndDocument;
import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utilities that don't belong in a specific class. This class is a necessary evil. :-)
 */
public class Utilities {
    
    /**
     * The number of meters in a mile.
     */
    public static final double METERS_PER_MILE = (254.0 / 10000.0) * 12.0 * 5280.0;

    private static final double FIVE_PI_OVER_TWO = 5.0 * Math.PI / 2.0;
    private static final double TWO_PI = 2.0 * Math.PI;
    
    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

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
     *         schema.  ArcGIS Runtime supports the following colors:<br/>
     *         <br/>
     *         <ul>
     *             <li>0xFFFF0000 (Color.RED) returns "1"</li>
     *             <li>0xFF00FF00 (Color.GREEN) returns "2"</li>
     *             <li>0xFF0000FF (Color.BLUE) returns "3"</li>
     *             <li>0xFFFFFF00 (Color.YELLOW) returns "4"</li>
     *         </ul>
     *         For other colors, this method returns a hex string of the form #AARRGGBB
     *         (alpha byte, red byte, green byte, and blue byte).
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
    
    /**
     * Parses an XML Schema Part 2 dateTime string and returns a corresponding Calendar.
     * Java SE includes this capability in javax.xml.bind.DataTypeConverter.parseDateTime(String),
     * but Android does not.
     * @param xmlDateTime an XML Schema Part 2 dateTime string, as defined in
     *                    http://www.w3.org/TR/xmlschema-2 . This method ignores
     *                    any spaces in the string.
     * @return a Calendar set to the time corresponding to xmlDateTime.
     * @throws Exception if the input is null or improperly formatted.
     */
    public static Calendar parseXmlDateTime(String xmlDateTime) throws Exception {
        xmlDateTime = xmlDateTime.replace(" ", "");
        int dashIndex = xmlDateTime.indexOf('-');
        if (0 == dashIndex) {
            dashIndex = xmlDateTime.indexOf('-', 1);
        }
        int year = Integer.parseInt(xmlDateTime.substring(0, dashIndex));

        int cursor = dashIndex + 1;
        dashIndex += 3;
        int month = Integer.parseInt(xmlDateTime.substring(cursor, dashIndex));
        
        cursor = dashIndex + 1;
        int tIndex = dashIndex + 3;
        int day = Integer.parseInt(xmlDateTime.substring(cursor, tIndex));
        
        cursor = tIndex + 1;
        int colonIndex = tIndex + 3;
        int hour = Integer.parseInt(xmlDateTime.substring(cursor, colonIndex));
        
        cursor = colonIndex + 1;
        colonIndex += 3;
        int minute = Integer.parseInt(xmlDateTime.substring(cursor, colonIndex));
        
        cursor = colonIndex + 1;
        int wholeSeconds = Integer.parseInt(xmlDateTime.substring(cursor, cursor + 2));
        cursor = cursor += 2;
        
        //Fractional seconds and time zone are optional. That means we might be done.
        float fractionalSeconds = 0f;
        TimeZone timeZone = null;
        if (xmlDateTime.length() > (colonIndex + 3)) {
            //Check for fractional seconds
            if ('.' == xmlDateTime.charAt(cursor)) {
                //Get all the numeric chars
                char nextChar;
                float factor = 0.1f;
                while ('0' <= (nextChar = xmlDateTime.charAt(++cursor)) && '9' >= nextChar) {
                    fractionalSeconds += factor * (float) (nextChar - 48);
                    factor *= 0.1;
                }
            }
            
            //Check for time zone
            if (cursor < xmlDateTime.length()) {
                String tzString = xmlDateTime.substring(cursor);
                if ("Z".equals(tzString)) {
                    tzString = "UTC";
                }
                timeZone = TimeZone.getTimeZone(tzString);
            }
        }
        
        if (null == timeZone) {
            timeZone = TimeZone.getTimeZone("UTC");
        }
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(year, month, day, hour, minute, wholeSeconds);
        cal.set(Calendar.MILLISECOND, Math.round(fractionalSeconds * 1000f));
        return cal;
    }
    
    /**
     * Returns the abbreviation for the angular unit with the specified WKID. ArcGIS
     * SDKs typically have an AngularUnit class with a getAbbreviation method, but
     * sometimes getAbbreviation doesn't return the abbreviation you might expect.
     * For example, it might return "deg" for degrees instead of returning the degrees
     * symbol. This method offers better abbreviations for some angular units. You could
     * call it this way:<br/>
     * <pre>String abbr = getAngularUnitAbbreviation(
     *     myAngularUnit.getID(),
     *     myAngularUnit.getAbbreviation());</pre>
     * @param wkid the angular unit's WKID.
     * @param defaultValue the value to be returned if this method does not know about
     *                     an abbreviation for the angular unit with the specified WKID.
     * @return the angular unit's abbreviation, or <code>defaultValue</code> if this
     *         method does not know about an abbreviation for the angular unit with
     *         the specified WKID.
     */
    public static String getAngularUnitAbbreviation(int wkid, String defaultValue) {
        if (9102 == wkid) { //degrees
            return "\u00B0";
        } else if (9114 == wkid) {// mils
            return "\u20A5";
        } else {
            return defaultValue;
        }
    }
    
    /**
     * A convenience method for creating the following structure:
     * &lt;geomessages&gt;
     *     &lt;geomessage /&gt;
     * &lt;/geomessages&gt;
     * @return
     * @throws ParserConfigurationException 
     */
    public static DomNodeAndDocument createGeomessageDocument() throws ParserConfigurationException {
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        Element geomessagesElement = doc.createElement("geomessages");
        doc.appendChild(geomessagesElement);
        Element geomessageElement = doc.createElement("geomessage");
        geomessageElement.setAttribute("v", "1.0");
        geomessagesElement.appendChild(geomessageElement);
        return new DomNodeAndDocument(geomessageElement, doc);
    }
    
    /**
     * Convenience method for adding an XML text element. For example, if you call
     * the method like this:<br/>
     * <br/>
     * <code>Utilities.addTextElement(parent, "lastName", "Lockwood");</code><br/>
     * <br/>
     * The new node, when rendered as a string, will look like this:<br/>
     * <br/>
     * <code>&lt;lastName&gt;Lockwood&lt;/lastName&gt;
     * @param document the document where the parent resides, and where the new text
     *                 element will reside. The document may or may not be the parent
     *                 node itself.
     * @param parentNode the parent node of the new text element.
     * @param key the name of the element.
     * @param value the string within the element.
     */
    public static void addTextElement(Document document, Node parentNode, String elementName, String elementText) {
        Element textElement = document.createElement(elementName);
        textElement.appendChild(document.createTextNode(elementText));
        parentNode.appendChild(textElement);
    }
    
    /**
     * Converts a DOM Document to a string.
     * @param doc the DOM Document.
     * @return the string representation.
     * @throws TransformerConfigurationException
     * @throws TransformerException 
     */
    public static String documentToString(Document doc) throws TransformerException {
        StringWriter xmlStringWriter = new StringWriter();
        transformerFactory.newTransformer().transform(
                new DOMSource(doc), new StreamResult(xmlStringWriter));
        return xmlStringWriter.toString();
    }
    
}
