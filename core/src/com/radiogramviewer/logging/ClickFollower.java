package com.radiogramviewer.logging;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Record clicks on an image
 */
public class ClickFollower {

    public static boolean clickLock=false;

    private int depth;
    private ArrayList<ArrayList<Node>> clicks; //each slide has its own list of clicks
    private ArrayList<ArrayList<Node>> highlights; //each slide has its own list of clicks
    private Texture click, highlight;
    private final boolean markClicks;
    private Config c;
    private TreeSet<ClickNode> orderedClicks;

    public ClickFollower(int slices, boolean markClicks, Texture click, Texture highlight, Config c, ClickFollower oldLog){
        this.c=c;

        if(oldLog!=null){
            clicks=oldLog.clicks;
            orderedClicks=oldLog.orderedClicks;
            highlights=oldLog.highlights;
            resetScreenPos();
        }
        else
            createClickList(slices);

        this.click=click;
        this.highlight=highlight;
        this.markClicks=markClicks;
        this.depth=c.click.depth;
    }

    private void createClickList(int slices){
        clicks=new ArrayList<ArrayList<Node>>(slices);
        for(int i=0; i<slices; ++i)
            clicks.add(new ArrayList<Node>());

        orderedClicks=new TreeSet<ClickNode>();

        resetHighlights();
    }

    public void resetHighlights(){
        int slices=clicks.size();
        highlights=new ArrayList<ArrayList<Node>>(slices);
        for(int i=0; i<slices; ++i)
            highlights.add(new ArrayList<Node>());
    }

    public void resetScreenPos(){
        for(ArrayList<Node> nl : clicks)
            for(Node n : nl) {
                n.reset(c.click.radius);
            }
        for(ArrayList<Node> nl : highlights)
            for(Node n : nl)
                n.reset(c.click.highlightRadius);
    }

//    private void transferLogs(ClickFollower old){
//        int slices=old.clicks.size();
//        clicks=new ArrayList<ArrayList<Node>>(slices);
//        for(ArrayList<Node> ol : old.clicks)
//            clicks.add(new ArrayList<Node>(ol.size()));
//
//        orderedClicks=new TreeSet<ClickNode>();
//
//        float downScale=1/scale;
//
//        for(ClickNode o : old.orderedClicks){
//            //message node's have no position data, so no need to rescale, just stick them in.
//            if(o.p.x==-1 && o.p.y==-1){
//                orderedClicks.add(o);
//                continue;
//            }
//            //otherwise, scale old node up to config dimensions
//            int x=sclUp(o.p.x,old.radius,old.scale);
//            int y=sclUp(o.p.y,old.radius,old.scale);
//
//            //then scale down to new viewer dimensions
//            x=sclDn(x,radius,downScale);
//            y=sclDn(y,radius,downScale);
//
//            Node n=new Node(x,y,o.z,o.time);
//            orderedClicks.add(n);
//            add(n.z,n);
//        }
//
//        highlights=new ArrayList<ArrayList<Node>>(slices);
//        for(ArrayList<Node> ol : old.highlights) {
//            ArrayList nl=new ArrayList<Node>(ol.size());
//            highlights.add(nl);
//            for (Node o : ol) {
//                int x = sclUp(o.p.x, old.radius, old.scale);
//                int y = sclUp(o.p.y, old.radius, old.scale);
//
//                //then scale down to new viewer dimensions
//                x = sclDn(x, radius, downScale);
//                y = sclDn(y, radius, downScale);
//
//                Node n = new Node(x, y, o.z, o.time);
//                nl.add(n);
//            }
//        }
//    }




    /**
     * Warning, this will clear all the previous clicks, make sure they are
     * saved if you wish to retain them somehow.
     * It also clears highlights, so those will have to be re added if desired.
     */
    public void reset(){
        createClickList(clicks.size());
    }

    /**
     * Add or remove a click
     * @param x The position of the click
     * @param y The position of the click
     * @param slide The slide on which the click occured
     */
    public void updateClick(int x, int y, int slide){
        Relay.inputOccured();
        if(!markClicks || clickLock)
            return;

        int ox=x-c.click.radius;
        int oy=y-c.click.radius;

        for(int z=lo(slide); z<hi(slide); ++z) {
            ArrayList<Node> l = clicks.get(z);
            for (int i = 0; i < l.size(); ++i) {
                if (overlap(ox, oy, l.get(i).sP)) {
                    Relay.clickRemoved(l.get(i).toString());
                    remove(i, l);
                    return;
                }
            }
        }
        Node n=new Node(x,y,slide,Timing.getMillis());
        n.reset(c.click.radius);
        Relay.clickAdded(n.toString());
        add(slide,n);
    }

    /**
     * inject a message into the click log
     * @param msg
     */
    public void addMessage(String msg){
        orderedClicks.add(new MessageNode(msg));
    }

    /**
     * Add a highlight
     * @param x The position of the click
     * @param y The position of the click
     * @param slide The slide on which the click occured
     */
    public void addHighlight(int x, int y, int slide){
//        x-=highlightRadius;
//        y-=highlightRadius;
        if(slide<0||slide>=highlights.size()){
            P.e("Highlight index out of range "+slide+", max size "+highlights.size());
            return;
        }
        Node n=new Node(x,y,slide,0);
        n.reset(c.click.highlightRadius);
        n.reset(c.click.highlightRadius);
        highlights.get(slide).add(n);
    }

    /**
     * See if two clicks overlap
     * @param x the location of one click
     * @param y The location of one click
     * @param v The location of the other click
     * @return true if the two clicks overlap
     */
    private boolean overlap(int x, int y, Vector2 v){
        return Vector2.dst(x,y,v.x,v.y)<c.click.radius;
    }


    /**
     * Remove a click
     * @param index which click
     * @param l from which slide list
     */
    private void remove(int index, ArrayList<Node> l){
        ClickNode n=l.get(index);
        l.set(index,l.get(l.size()-1)); //replace the click with the last click
        l.remove(l.size()-1); //remove the last click
        orderedClicks.remove(n);
    }
    private void add(int slide, Node n){
        clicks.get(slide).add(n);
        orderedClicks.add(n);
    }

    /**
     * Draw clicks on, above, and below current slide
     * @param batch the batch to group all the sprites onto (for efficiency)
     * @param slide the currently focused slide
     * @return
     */
    public boolean drawClicks(SpriteBatch batch, int slide) {
        if(!markClicks)
            return true;
        for(int i=lo(slide); i<hi(slide); ++i){
            for(Node n : clicks.get(i))
                batch.draw(click, n.sP.x, n.sP.y);
        }
        return true;
    }
    /**
     * Draw highlights on, above, and below current slide
     * @param batch the batch to group all the sprites onto (for efficiency)
     * @param slide the currently focused slide
     * @return
     */
    public boolean drawHighlights(SpriteBatch batch, int slide) {
        for(int i=lo(slide); i<hi(slide); ++i){
            for(Node n : highlights.get(i))
                batch.draw(highlight, n.sP.x, n.sP.y);
        }
        return true;
    }

    /**
     * Get all the clicks as a string for saving
     * @param cs what separates each click's components (eg, x, y, slide, time, and focus)
     * @param vs what separates each click
     * @return all the clicks, note, these will be given with the coordinates specified in
     *  the config file.
     */
    public String getClicks(String cs, String vs){
        StringBuilder b=new StringBuilder();

        for(ClickNode n : orderedClicks)
            n.append(b,cs,vs);

        return b.toString();
    }

    //Scale the absolute pixel coordinates to the config file pixels.
//    private int scl(float val){
//        return sclUp(val,radius, scale);
//    }
//    private int sclUp(float val, int radius, float upScale){
//        return (int)Math.ceil((val+radius)*upScale);
//    }
//    private int sclDn(float val, int radius, float dnScale){
//        return (int)Math.ceil((val-radius)*dnScale);
//    }

    /**
     * @param index the clicked slide
     * @return the lowest slide which should show the click
     */
    private int lo(int index){
        if(index-depth<0)
            return 0;
        else
            return index-depth;
    }

    /**
     * @param index the clicked slide
     * @return the highest slide which should show the click
     */
    private int hi(int index){
        //note 1 is added to offset the clicked on slide itself
        if(index+depth+1>clicks.size())
            return clicks.size();
        else
            return index+depth+1;
    }

    class Node extends ClickNode{
        public Vector2 sP;
        Node(int x, int y, int z, long time) {
            super(x, y, z, time);
        }
//
//        @Override
//        public void append(StringBuilder b, String cs, String vs){
//            b.append(scl(p.x)).append(cs).append(scl(p.y)).append(cs).append(z+1).append(cs).append(time).append(vs);
//        }

        public void reset(int offset){
            sP=new Vector2(p.x*Relay.getWidth()-offset,p.y*Relay.getHeight()-offset);
        }
    }
    class MessageNode extends ClickNode{
        final String message;
        MessageNode(String msg) {
            super(-1, -1, Timing.getMillis());
            message=msg;
        }

        @Override
        public void append(StringBuilder b, String cs, String vs){
            b.append(message).append(cs).append(time).append(vs);
        }
    }
    

}
