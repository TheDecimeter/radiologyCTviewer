package com.radiogramviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

/**
 * Input handler, Note that drag distance scales with the pixel size of the viewer. This is
 * especially desirable if all viewers are supposed to be displayed at the same size in inches.
 * However it's desirablity is unknown if a custom scale is set. In these cases, in cases where
 * it is not wanted, it can be manually reset with viewerSetDragDistance in JavaScript. You can
 * also use viewerGetDensityFactor to get the factor to restore scaled pixels back to their
 * full coordinate (eg: getViewerDensityFactor() * getViewerWidth() should equal a number
 * close to the width in the config file with minor rounding errors).
 */
public class Controls implements InputProcessor {

    private SlideManager slides; //keep track of slide manager so slices can be scrolled
    private ClickFollower click; //keep track of click follower so clicks can be recorded
    private Config c;

    public static ClickNode lastClick;
    public static int z;
    private static int dragDist;
    private boolean up, down;

    private HashMap<Integer,Node> drag;

    public Controls(SlideManager slides, Config c){
        this.slides=slides;
        lastClick=new ClickNode(0,0,0);
        z=0;
        up=false;
        down=false;

        this.c=c;
        drag=new HashMap<Integer, Node>();
        dragDist=c.input.dragDist;
    }

    public void setSlideManager(SlideManager man,ClickFollower click){
        slides=man;
        this.click=click;
    }

    @Override
    public boolean keyDown(int keycode) {
        handleKeyDown(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        handleKeyUp(keycode);
        if(keycode== Input.Keys.M&&c.debug.advanceSlide)
        {
            int slideMode=MainViewer.getSlideMode()+1;
            if(slideMode>20)
                slideMode=0;
            MainViewer.setSlideMode(slideMode,0);
//            MainViewer.setSlideMode(MainViewer.getSlideMode()+1,0);
//            if(MainViewer.getSlideMode()>20)
//                MainViewer.setSlideMode(0,0);
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        startDrag(screenX,screenY,pointer);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(dragged(pointer)) {
            stopDrag(pointer);
            return false;
        }

        //Convert touch coordinates (top left) to screen coordinates (bottom left)
        screenY=MainViewer.getHeight()-(screenY+1);

        if(MainViewer.getSlideMode()!=MainViewer.none) {
            click.updateClick(screenX, screenY, slides.getSlide());
            if(saveClick()) {
                z = slides.getSlide()+1;
                lastClick=new ClickNode((int)(screenX*c.global.scale),(int)(screenY*c.global.scale),Timing.getMillis());
            }
        }
        else if(saveClick()){
            lastClick=new ClickNode((int)(screenX*c.global.scale),(int)(screenY*c.global.scale),Timing.getMillis());
            z=1;
        }
        stopDrag(pointer);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(c.input.drag && MainViewer.getSlideMode()!=MainViewer.none)
            slides.advanceSlide(drag(screenX,screenY,pointer));
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
//        System.out.println("scrolled "+amount);
        if(c.input.wheel && MainViewer.getSlideMode()!=MainViewer.none)
            slides.advanceSlide(amount);
        return false;
    }

    private boolean saveClick(){
        if(c.click.overwriteLastClick)
            return true;
        return lastClick.time==0;
    }


    private void handleKeyDown(int keycode){
        if(!c.input.arrow&&!c.input.wasd)
            return;
        if(MainViewer.getSlideMode()==MainViewer.none)
            return;

        if(c.input.arrow) {
            if (keycode == Input.Keys.DOWN)
                down = true;

            if (keycode == Input.Keys.UP)
                up = true;
        }
        if(c.input.wasd) {
            if (keycode == Input.Keys.S)
                down = true;

            if (keycode == Input.Keys.W)
                up = true;
        }

        if(up&&down)
            slides.move(SlideManager.stay);
        else if(up)
            slides.move(SlideManager.up);
        else if(down)
            slides.move(SlideManager.down);
        else
            slides.move(SlideManager.stay);
    }

    private void handleKeyUp(int keycode){
        if(!c.input.arrow&&!c.input.wasd)
            return;
        if(MainViewer.getSlideMode()==MainViewer.none)
            return;

        if(c.input.arrow) {
            if (keycode == Input.Keys.DOWN)
                down = false;

            if (keycode == Input.Keys.UP)
                up = false;
        }
        if(c.input.wasd) {
            if (keycode == Input.Keys.S)
                down = false;

            if (keycode == Input.Keys.W)
                up = false;
        }
        if(up&&down)
            slides.move(SlideManager.stay);
        else if(up)
            slides.move(SlideManager.up);
        else if(down)
            slides.move(SlideManager.down);
        else
            slides.move(SlideManager.stay);
    }



    //Drag methods
    public static void setDragDist(int dragDist){
        Controls.dragDist=dragDist;
    }

    /**
     * Was this a drag or a click
     * @param index the pointer index
     * @return true if drag
     */
    private boolean dragged(int index){
        if(c.input.drag){
            if(drag.containsKey(index))
                return drag.get(index).dragged;
        }
        return false;
    }

    /**
     * Release the pointer if the user lifted their finger (or released their mouse button)
     * @param index the index of the pointer that was released
     */
    private void stopDrag(int index){
        if(!c.input.drag)
            return;
        if(drag.containsKey(index))
            drag.remove(index);
    }

    /**
     * A touch or click occured, save it to see if it becomes a drag
     * @param x where the touch originated
     * @param y where the touch originated
     * @param index the index of the pointer that clicked/touched
     */
    private void startDrag(int x, int y, int index){
        if(!c.input.drag)
            return;

        if(drag.containsKey(index))
            return;
        drag.put(index,new Node(x,y));
    }

    /**
     * drag calculations
     * @param x touch coordinate
     * @param y touch coordinate
     * @param index touch id (for multitouch devices)
     * @return distance dragged
     */
    private int drag(int x, int y, int index){
        if(!c.input.drag)
            return 0;

        //A touch down event should have already stored the index with "startDrag"
        if(!drag.containsKey(index)) {
            MainViewer.println("checking drag on unsaved index "+index,Constants.e);
            return 0;
        }

        //See if the drag was long enough to trigger any slide change
        Node v=drag.get(index);
        int dist=Math.abs(v.y-y);
        if(dist>dragDist){
            //if so, see how many slide changes
            dist=(v.y-y)/dragDist;
            //and save the remainder to be calculated in the next drag event
            v.y -=dist*dragDist;
            v.dragged=true; //mark drag as successful so that it doesn't count as a click
//            MainViewer.println("dist "+dist,Constants.d);
            return dist;
        }
        else if(!v.dragged && Math.abs(v.x - x)>dragDist) //also check dragging along the x axis to prevent
            v.dragged=true;                               // silly clicks

        return 0;
    }

    //keep track of basic drag information
    private class Node{
        int x,y;
        boolean dragged=false;
        Node(int x, int y){
            this.y=y;
            this.x=x;
        }
    }
}
