package com.example.reboxed;

public class SensorData {
    public String smoke;
    public String motion;
    // this is stupid
    public String accel;
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    
        return "smoke: " + smoke + " motion:"+motion + " accel:"+accel;
    }     

}
