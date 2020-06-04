package com.radiogramviewer.logging;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Record clicks on an image
 */
public class ClickFollower {

    public static boolean clickLock=false;

    private int radius,depth;
    private ArrayList<ArrayList<Node>> clicks; //each slide has its own list of clicks
    private ArrayList<ArrayList<Node>> highlights; //each slide has its own list of clicks
    private Texture click, highlight;
    private final boolean markClicks;
    private Config c;
    private TreeSet<ClickNode> orderedClicks;

    public ClickFollower(int slices, int radius, int depth, boolean markClicks, Texture click, Texture highlight, Config c){
        createClickList(slices);
        updateRadius(radius,depth);
        this.click=click;
        this.highlight=highlight;
        this.markClicks=markClicks;
        this.c=c;
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


    /**
     * Warning, this will clear all the previous clicks, make sure they are
     * saved if you wish to retain them somehow.
     * It also clears highlights, so those will have to be re added if desired.
     */
    public void reset(){
        createClickList(clicks.size());
    }

    private void updateRadius(int radius, int depth){
        this.depth=depth;
        this.radius=radius;
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

        x-=radius;
        y-=radius;

        for(int z=lo(slide); z<hi(slide); ++z) {
            ArrayList<Node> l = clicks.get(z);
            for (int i = 0; i < l.size(); ++i) {
                if (overlap(x, y, l.get(i).p)) {
                    Relay.clickRemoved(l.get(i).toString(radius));
                    remove(i, l);
                    return;
                }
            }
        }
        Node n=new Node(x,y,slide,Timing.getMillis());
        Relay.clickAdded(n.toString(radius));
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
        x-=c.click.highlightRadius;
        y-=c.click.highlightRadius;
        if(slide<0||slide>=highlights.size()){
            P.e("Highlight index out of range "+slide+", max size "+highlights.size());
            return;
        }
        highlights.get(slide).add(new Node(x,y,slide,0));
    }

    /**
     * See if two clicks overlap
     * @param x the location of one click
     * @param y The location of one click
     * @param v The location of the other click
     * @return true if the two clicks overlap
     */
    private boolean overlap(int x, int y, Vector2 v){
        return Vector2.dst(x,y,v.x,v.y)<radius;
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
            for(ClickNode n : clicks.get(i))
                batch.draw(click, n.p.x, n.p.y);
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
            for(ClickNode n : highlights.get(i))
                batch.draw(highlight, n.p.x, n.p.y);
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
    private int scl(float val){
        return (int)Math.ceil((val+radius)*c.global.scale);
    }

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
        Node(int x, int y, int z, long time) {
            super(x, y, z, time);
        }

        @Override
        public void append(StringBuilder b, String cs, String vs){
            b.append(scl(p.x)).append(cs).append(scl(p.y)).append(cs).append(z+1).append(cs).append(time).append(vs);
        }
    }
    class MessageNode extends ClickNode{
        final String message;
        MessageNode(String msg) {
            super(0, 0, Timing.getMillis());
            message=msg;
        }

        @Override
        public void append(StringBuilder b, String cs, String vs){
            b.append(message).append(cs).append(time).append(vs);
        }
    }
    

}
