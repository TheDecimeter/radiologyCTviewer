package com.radiogramviewer;

import com.badlogic.gdx.utils.TimeUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * simple class to keep track of timing methods and specific key times
 */
public class Timing {
    private static long startTime, calendarTime;

    public static void start(long startedCalanderTime){
        startTime= TimeUtils.nanoTime();//System.currentTimeMillis();
        calendarTime=startedCalanderTime;
    }

    public static long getStartTime(){
        return calendarTime;
    }

    public static long getMillis(){
        return TimeUtils.nanosToMillis(TimeUtils.timeSinceNanos(startTime));
    }


}
