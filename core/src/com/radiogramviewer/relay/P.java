package com.radiogramviewer.relay;

/**
 * system specific printing
 */
public class P {
    public static final int d=1,w=2,e=3;

    private static Constants constants;

    public static void init(Constants constants){
        P.constants=constants;
    }

    public static void d(String msg){
        constants.print(msg,d);
    }
    public static void w(String msg){
        constants.print(msg,w);
    }
    public static void e(String msg){
        constants.print(msg,e);
    }
}
