package com.radiogramviewer.logging;

import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.SubViewer;

/**
 * Simple class for holding and displaying click information
 */
public class ClickNode{

    public final Vector2 p;
    public final long time;

    public ClickNode(int x, int y, long time){
        p=new Vector2(x,y);
        this.time=time;
    }
    private int o(float val, float offset){
        return (int)Math.ceil((val+offset)*SubViewer.getConfig().global.scale);
    }

    //stringify the coordinates with added z (slide index) coordinate
    public String toString(int z, float offset){
        return ""+o(p.x,offset)+","+o(p.y,offset)+","+z+","+time;
    }
}