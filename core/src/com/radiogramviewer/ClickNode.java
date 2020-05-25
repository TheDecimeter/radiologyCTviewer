package com.radiogramviewer;

import com.badlogic.gdx.math.Vector2;

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
    //simple to string with available information
    @Override
    public String toString(){
        return ""+p.x+","+p.y+","+time;
    }
    //stringify the coordinates with added z (slide index) coordinate
    public String toString(int z){
        return ""+p.x+","+p.y+","+z+","+time;
    }
    //stringify the coordinate with z (slide index) and custom separater
    public String toString(int z, String s){
        return ""+p.x+s+p.y+s+z+s+time;
    }
}