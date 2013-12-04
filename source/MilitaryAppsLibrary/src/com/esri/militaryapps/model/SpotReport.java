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
import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

/**
 * A bean that holds the details of a spot report. The spot report is in the standard
 * SALUTE format:
 * <ul>
 *   <li>Size</li>
 *   <li>Activity</li>
 *   <li>Location</li>
 *   <li>Unit (or Uniform)</li>
 *   <li>Time</li>
 *   <li>Equipment</li>
 * </ul>
 */
public class SpotReport implements Serializable {

    public enum Size {
        TEAM(0, "Team"),
        SQUAD(1, "Squad"),
        SECTION(11, "Section"),
        PLATOON(111, "Platoon/Detachment"),
        COMPANY(2, "Company/Battery/Troop"),
        BATTALION(22, "Battalion/Squadron"),
        REGIMENT(222, "Regiment/Group"),
        BRIGADE(3, "Brigade"),
        DIVISION(33, "Division"),
        CORPS(333, "Corps"),
        ARMY(3333, "Army"),
        ARMY_GROUP(33333, "Army Group/Front"),
        REGION(333333, "Region"),
        COMMAND(44, "Command");
        
        private final int code;
        private final String name;
        
        private Size(int code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public int getCode() {
            return code;
        }
    }
    
    public enum Activity {
        
        ATTACKING("Attacking", "Attacking"),
        DEFENDING("Defending", "Defending"),
        MOVING("Moving", "Moving"),
        STATIONARY("Stationary", "Stationary"),
        CACHE("Cache", "Cache"),
        CIVILIAN("Civilian", "Civilian"),
        PERSONNEL_RECOVERY("Personnel Recovery", "Personnel Recovery");
        
        private final String code;
        private final String name;
        
        private Activity(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public enum Unit {
        
        CONVENTIONAL("Conventional", "Conventional"),
        IRREGULAR("Irregular", "Irregular"),
        COALITION("Coalition", "Coalition"),
        HOST_NATION("Host Nation", "Host Nation"),
        NGO("NGO", "NGO"),
        CIVILIAN("Civilian", "Civilian"),
        FACILITY("Facility", "Facility");
        
        private final String code;
        private final String name;
        
        private Unit(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public enum Equipment {
        
        MISSILE_LAUNCHER("Missile Launcher H", "Hostile Missile Launcher"),
        GRENADE_LAUNCHER("Grenade Launcher H", "Hostile Grenade Laucher"),
        HOWITZER("Howitzer H", "Hostile Howitzer"),
        ARMORED_PERSONNEL_CARRIER("Armored Personnel Carrier H", "Hostile Armored Personnel Carrier"),
        GROUND_VEHICLE("Ground Vehicle H", "Hostile Ground Vehicle"),
        ARMORED_TANK("Armored Tank H", "Hostile Armored Tank"),
        RIFLE("Rifle H", "Hostile Rifle"),
        IED("IED H", "Hostile IED");
        
        private final String code;
        private final String name;
        
        private Equipment(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private Size size;
    private Activity activity;
    private double locationX;
    private double locationY;
    private int locationWkid;
    private Unit unit;
    private Calendar time;
    private Equipment equipment;
    private String messageId = null;

    /**
     * Creates a new SpotReport object.
     * @param mapController the MapController, used for converting coordinates.
     * @param outboundMessageController the OutboundMessageController, used for
     */
    public SpotReport() {
        regenerateMessageId();
    }

    /**
     * Creates a SpotReport with the specified field values.
     * @param mapController
     * @param size
     * @param activity
     * @param locationX
     * @param locationY
     * @param locationWkid
     * @param unit
     * @param time
     * @param equipment
     */
    public SpotReport(
            Size size,
            Activity activity,
            double locationX,
            double locationY,
            int locationWkid,
            Unit unit,
            Calendar time,
            Equipment equipment) {
        this();
        this.size = size;
        this.activity = activity;
        this.locationX = locationX;
        this.locationY = locationY;
        this.locationWkid = locationWkid;
        this.unit = unit;
        setTime(time);
        this.equipment = equipment;
    }

    /**
     * Resets the message ID to a new random GUID.
     */
    public final void regenerateMessageId() {
        messageId = UUID.randomUUID().toString();
    }

    /**
     * Returns the message ID.
     * @return the message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Returns the size of the observed unit.
     * @return the size of the observed unit (can be null).
     */
    public Size getSize() {
        return size;
    }

    /**
     * Sets the size of the observed unit.
     * @param size the size of the observed unit (can be null).
     */
    public void setSize(Size size) {
        this.size = size;
    }

    /**
     * @return the activity
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * @param activity the activity to set
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * @return the locationX
     */
    public double getLocationX() {
        return locationX;
    }

    /**
     * @param locationX the locationX to set
     */
    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    /**
     * @return the locationY
     */
    public double getLocationY() {
        return locationY;
    }

    /**
     * @param locationY the locationY to set
     */
    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    /**
     * @return the locationWkid
     */
    public int getLocationWkid() {
        return locationWkid;
    }

    /**
     * @param locationWkid the locationWkid to set
     */
    public void setLocationWkid(int locationWkid) {
        this.locationWkid = locationWkid;
    }
    
    /**
     * @return the unit observed.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * @param unit the unit observed.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * @return the time
     */
    public Calendar getTime() {
        return time;
    }

    /**
     * Returns the SpotReport's time, formatted as a standard date-time string.
     * @return the SpotReport's time, formatted as a standard date-time string.
     */
    public String getTimeString() {
        if (null == time) {
            return null;
        } else {
            return Utilities.DATE_FORMAT_GEOMESSAGE.format(time.getTime()).toUpperCase();
        }
    }

    /**
     * @param time the time to set
     */
    public final void setTime(Calendar time) {
        this.time = time;
    }

    /**
     * @return the equipment
     */
    public Equipment getEquipment() {
        return equipment;
    }

    /**
     * @param equipment the equipment to set
     */
    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }
    
}
