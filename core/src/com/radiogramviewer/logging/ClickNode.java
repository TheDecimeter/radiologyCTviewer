package com.radiogramviewer.logging;

import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.SubViewer;
import com.radiogramviewer.relay.Relay;

/**
 * Simple class for holding and displaying click information
 * Takes coordinates in screen pixel dimensions, stores them
 * as floats between 0 and 1, prints them according to the dimensions
 * specified in the config file.
 */
public class ClickNode implements Comparable<ClickNode>, LogNode{

    public final Vector2 p;
    public final int z;
    public final long time;

    public ClickNode(int x, int y, long time){
        this(x,y,-1,time);
    }
    public ClickNode(int x, int y, int z, long time){
        p=new Vector2(ux(x),uy(y));
        this.time=time;
        this.z=z;
    }
//    private int o(float val, float offset){
//        return (int)Math.ceil((val+offset)*SubViewer.getConfig().global.scale);
//    }

    private static float ux(int val){
        return (float)val/ Relay.getWidth();
    }
    private static float uy(int val){
        return (float)val/Relay.getHeight();
    }
    public static int x(float val){
        return (int)Math.ceil(val*SubViewer.getConfig().window.dimensionWidth);
    }
    public static int y(float val){
        return (int)Math.ceil(val*SubViewer.getConfig().window.dimensionHeight);
    }

    //stringify the coordinates with added z (slide index) coordinate
    public String toString(){
        if(time==0)
            return("0,0,0,0");
        if(z!=-1)
            return ""+x(p.x)+","+y(p.y)+","+(z+1)+","+time;
        return ""+x(p.x)+","+y(p.y)+","+time;
    }

    @Override
    public int compareTo(ClickNode other) {
        if(this.time != other.time){
            if(this.time>other.time)
                return 1;
            else
                return -1;
        }
        if(this.p.y!=other.p.y) {
            if (this.p.y > other.p.y)
                return 1;
            else
                return -1;
        }
        if(this.p.x!=other.p.x) {
            if (this.p.x > other.p.x)
                return 1;
            else
                return -1;
        }
        return this.z-other.z;
    }

    @Override
    public void append(StringBuilder b, String cs, String vs) {
        b.append(x(p.x)).append(cs).append(y(p.y)).append(cs).append(z).append(cs).append(time).append(vs);
    }
}