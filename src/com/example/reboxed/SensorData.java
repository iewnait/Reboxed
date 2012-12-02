package com.example.reboxed;

public class SensorData {
    public float smoke;
    public float motion;
    // this is stupid
    public float accel;
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    
        return "smoke: " + smoke + " motion:"+motion + " accel:"+accel;
    }     

}
