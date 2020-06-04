package com.radiogramviewer.logging;

import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.SubViewer;

/**
 * Simple class for holding and displaying click information
 */
public class ClickNode implements Comparable<ClickNode>, LogNode{

    public final Vector2 p;
    public final int z;
    public final long time;

    public ClickNode(int x, int y, long time){
        this(x,y,-1,time);
    }
    public ClickNode(int x, int y, int z, long time){
        p=new Vector2(x,y);
        this.time=time;
        this.z=z;
    }
    private int o(float val, float offset){
        return (int)Math.ceil((val+offset)*SubViewer.getConfig().global.scale);
    }

    //stringify the coordinates with added z (slide index) coordinate
    public String toString(float XYoffset){
        if(time==0)
            return("0,0,0,0");
        if(z!=-1)
            return ""+o(p.x,XYoffset)+","+o(p.y,XYoffset)+","+(z+1)+","+time;
        return ""+o(p.x,XYoffset)+","+o(p.y,XYoffset)+","+time;
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
        b.append(p.x).append(cs).append(p.y).append(cs).append(z).append(cs).append(time).append(vs);
    }
}