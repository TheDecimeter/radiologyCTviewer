package com.radiogramviewer.logging;

import com.badlogic.gdx.utils.TimeUtils;

/**
 * simple class to keep track of timing methods and specific key times
 */
public class Timing {

    private static long startTime=0, calendarTime=0;

    public static void start(long startedCalanderTime){
        if(calendarTime==0&&startTime==0) {
            startTime = TimeUtils.nanoTime();//System.currentTimeMillis();
            calendarTime = startedCalanderTime;
        }
    }

    public static long getStartTime(){
        return calendarTime;
    }

    public static long getMillis(){
        return TimeUtils.nanosToMillis(TimeUtils.timeSinceNanos(startTime));
    }


}
