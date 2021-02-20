package com.radiogramviewer.logging;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.config.ShapeMaker;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Record clicks on an image as well as drawing other shapes and highlights
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
    private ShapeMaker shapes;
    private ArrayList<ArrayList<ShapeNode>> imageShapes,uiShapes;

    public ClickFollower(int slices, boolean markClicks, Texture click, Texture highlight, Config c, ShapeMaker shapes, ClickFollower oldLog){
        this.c=c;
        this.shapes=shapes;

        if(oldLog!=null){
            clicks=oldLog.clicks;
            orderedClicks=oldLog.orderedClicks;
            highlights=oldLog.highlights;
            uiShapes=oldLog.uiShapes;
            imageShapes=oldLog.imageShapes;
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

        imageShapes=new ArrayList<ArrayList<ShapeNode>>(slices);
        for(int i=0; i<slices; ++i)
            imageShapes.add(new ArrayList<ShapeNode>());
        uiShapes=new ArrayList<ArrayList<ShapeNode>>(slices);
        for(int i=0; i<slices; ++i)
            uiShapes.add(new ArrayList<ShapeNode>());
    }

    public void resetScreenPos(){
        for(ArrayList<Node> nl : clicks)
            for(Node n : nl) {
                n.reset(c.click.radius,c.click.radius);
            }
        for(ArrayList<Node> nl : highlights)
            for(Node n : nl)
                n.reset(c.click.highlightRadius,c.click.highlightRadius);

        for(ArrayList<ShapeNode> nl : uiShapes)
            for(ShapeNode n : nl) {
                int ox=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getWidth()/2);
                int oy=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getHeight()/2);
                n.reset(ox, oy);
            }
        for(ArrayList<ShapeNode> nl : imageShapes)
            for(ShapeNode n : nl) {
                int ox=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getWidth()/2);
                int oy=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getHeight()/2);
                n.reset(ox, oy);
            }
    }




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
    public boolean updateClick(int x, int y, int slide){
        if(slide<0||slide>=clicks.size()){
            P.e("click slide out of range "+slide+", max size "+clicks.size());
            return false;
        }
        Relay.inputOccured();
        if(!markClicks || clickLock)
            return false;

        int ox=x-c.click.radius;
        int oy=y-c.click.radius;

        for(int z=lo(slide,depth); z<hi(slide,depth); ++z) {
            ArrayList<Node> l = clicks.get(z);
            for (int i = 0; i < l.size(); ++i) {
                if (overlap(ox, oy, l.get(i).sP)) {
                    Relay.clickRemoved(l.get(i).toString());
                    remove(i, l);
                    return true;
                }
            }
        }
        Node n=new Node(x,y,slide,Timing.getMillis());
        n.reset(c.click.radius,c.click.radius);
        Relay.clickAdded(n.toString());
        add(slide,n);
        return true;
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
     * @param slide The slide on which to highlight
     */
    public void addHighlight(int x, int y, int slide){
        if(slide<0||slide>=highlights.size()){
            P.e("Highlight index out of range "+slide+", max size "+highlights.size());
            return;
        }
        Node n=new Node(x,y,slide,0);
        n.reset(c.click.highlightRadius,c.click.highlightRadius);
        highlights.get(slide).add(n);
    }


    public boolean addUIshape(int x, int y, int slide, int shapeIndex){
        return addShape(x,y,slide,shapeIndex,uiShapes);
    }
    public boolean addImageShape(int x, int y, int slide, int shapeIndex){
        return addShape(x,y,slide,shapeIndex,imageShapes);
    }

    private boolean addShape(int x, int y, int slide, int shapeIndex, ArrayList<ArrayList<ShapeNode>> l){
        if(slide<0||slide>=l.size()){
            P.e("shape index out of range "+slide+", max size "+l.size());
            return false;
        }
        ShapeNode n=new ShapeNode(x,y,slide,shapeIndex);
        int ox=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getWidth()/2);
        int oy=(int)Math.ceil(shapes.get(n.shapeIndex).img(0).getHeight()/2);
        n.reset(ox, oy);
        l.get(slide).add(n);
        return true;
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
        for(int i=lo(slide,depth); i<hi(slide,depth); ++i){
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
        for(int i=lo(slide,depth); i<hi(slide,depth); ++i){
            for(Node n : highlights.get(i))
                batch.draw(highlight, n.sP.x, n.sP.y);
        }
        return true;
    }

    public boolean drawImageShapes(SpriteBatch batch, int slide) {
        for(int i=lo(slide,shapes.depth); i<hi(slide,shapes.depth); ++i){
            for(ShapeNode n : imageShapes.get(i))
                drawShape(batch,slide,n);
        }
        return true;
    }
    public boolean drawUIshapes(SpriteBatch batch, int slide) {
        for(int i=lo(slide,shapes.depth); i<hi(slide,shapes.depth); ++i){
            for(ShapeNode n : uiShapes.get(i))
                drawShape(batch,slide,n);
        }
        return true;
    }

    private void drawShape(SpriteBatch batch, int slide, ShapeNode n){
        Texture t=shapes.get(n.shapeIndex).img(Math.abs(slide-n.z));
        if(t!=null)
            batch.draw(t,n.sP.x,n.sP.y);
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
    private int lo(int index,int depth){
        if(index-depth<0)
            return 0;
        else
            return index-depth;
    }

    /**
     * @param index the clicked slide
     * @return the highest slide which should show the click
     */
    private int hi(int index,int depth){
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

        public void reset(int offsetx, int offsety){
            sP=new Vector2(p.x*Relay.getWidth()-offsetx,p.y*Relay.getHeight()-offsety);
        }
    }

    class ShapeNode extends Node{
        final int shapeIndex;
        ShapeNode(int x, int y, int z, int shapeIndex) {
            super(x, y, z, 0);
            this.shapeIndex=shapeIndex;
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
