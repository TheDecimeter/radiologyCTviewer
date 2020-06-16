package com.radiogramviewer.desktop;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.SubViewer;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.graphics.shaders.WindowingShaders;
import com.radiogramviewer.logging.ClickNode;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.Controls;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

/**
 * Mostly a dummy class to mock what would be an interface with outside JavaScript
 * So that a pc build can be made quickly for debugging
 */
public class PCconstants implements Constants {

    private float width=555;
    private boolean small=true;

    @Override
    public int getMode() {
        return 3;
    }




    @Override
    public void print(String s, int code) {
        switch(code){
            case P.d:
                System.out.println("Msg: "+s);
                return;
            case P.w:
                System.out.println("Warning: "+s);
                return;
            case P.e:
                System.out.println(" ERROR: "+s);
                return;
        }
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void addControls(Controls c) {

    }

    @Override
    public void inputOccured() {

    }

    @Override
    public void clickAdded(String click) {

    }

    @Override
    public void clickRemoved(String click) {

    }
    @Override
    public void scrollMove(int index) {
    }

    @Override
    public void scrollStuck(int index) {
    }

    @Override
    public void loadingStateChanged(int state) {

    }

    @Override
    public void processingState(int remaining, float progress) {
        System.out.println("  processing. "+remaining+"tasks remaining, current is "+(int)(progress*100)+"% done");
    }

    @Override
    public float getScale(int originalWidth) {
        //desired width/original width, for now, always return 1
        return 1;
        //return width/originalWidth;
    }

    @Override
    public void addToPacket(String msg) {
        System.out.println(" adding to packet:"+msg);
    }

    @Override
    public boolean hasCoroutine() {
        return false;
    }

    @Override
    public void runCoroutine() {
    }

    @Override
    public void passKey(int key) {
        switch(key){
            case Input.Keys.NUM_0:
                P.d("         RESET");
                if(small)width=666;
                else width=555;
                small=!small;
                MainViewer.setToReset();
                break;
            case Input.Keys.NUM_1:
                if(SubViewer.getSlideMode()==6) {
                    SubViewer.setSlideMode(5, -1);
                    ShaderManager.setShader("customPassKey1");
                    P.d("slide 5 (16 bit)");
                }
                else if(SubViewer.getSlideMode()==7){
                    SubViewer.setSlideMode(6, -1);
                    ShaderManager.setShader("customPassKey2");
                    P.d("slide 6  (8 bit winFull)");
                }
                else{
                SubViewer.setSlideMode(7, -1);
                ShaderManager.setShader("customPassKey3");
                P.d("slide 7  (8 bit win2000)");
            }
                break;
            case Input.Keys.NUM_2:
                ShaderManager.addShader("customPassKey1", WindowingShaders.windowGray16(.5f,1));
                ShaderManager.addShader("customPassKey2", WindowingShaders.windowGrayFull(.5f,1));
                ShaderManager.addShader("customPassKey3", WindowingShaders.windowGray(.5f,1));
                break;
            case Input.Keys.NUM_3:
                ShaderManager.setShader("customPassKey1");
                break;
            case Input.Keys.NUM_4:
//                ShaderManager.addShader("customPassKey1", WindowingShaders.windowGray16(.515f,0.075f));
//                ShaderManager.addShader("customPassKey2", WindowingShaders.windowGrayFull(.515f,0.075f));
//                ShaderManager.addShader("customPassKey3", WindowingShaders.windowGray(.515f,0.075f));
                ShaderManager.addShader("customPassKey1", WindowingShaders.windowGray16(1.5f,1));
                ShaderManager.addShader("customPassKey2", WindowingShaders.windowGrayFull(1.5f,1));
                ShaderManager.addShader("customPassKey3", WindowingShaders.windowGray(1.5f,1));
                break;
            case Input.Keys.NUM_5:
                ShaderManager.setShader("off");
//                ShaderManager.removeShader("customPassKey1");
                break;
            case Input.Keys.NUM_6:
                P.d("Shader Log:\n"+ShaderManager.logger.get(",","\n"));
                break;


            case Input.Keys.NUM_7:
                P.d("SCROLLS:\n"+Relay.getScrollTimesFor(2,",","\n"));
                break;
            case Input.Keys.NUM_8:
                String s=Controls.lastClick.toString();
                P.d("CLICKS:\n"+Relay.getClicksAt(2,",","\n")+"\nLast Click: "+s);
                String[] t=s.split(",");
                int x=Integer.parseInt(t[0]);
                int y=Integer.parseInt(t[1]);
                int z=Integer.parseInt(t[2]);

                //addHighlight(2,x,y,z);
                //addUIshape(2,x,y,z,"box");
                addUIshape(2,x,y,z,"mark");
                Controls.lastClick=new ClickNode(0,0,0,0);

                break;

            case Input.Keys.NUM_9:
                SlideManager.scrollLock=!SlideManager.scrollLock;
                break;
        }
    }

    @Override
    public void finishedResetting() {
        P.d("finished resetting state="+ Relay.loadingState());
    }

    @Override
    public void forceDraw() {
//        try {
            Display.update(false);
//        } catch (LWJGLException e) {
//            P.e("failed to swap buffers");
//            e.printStackTrace();
//        }
    }


    private static void addHighlight(int slideSet, int x, int y, int slide){
        float scale=1/SubViewer.getConfig().global.scale;
        Relay.addHighlight(slideSet,(int)(x*scale),(int)(y*scale),slide-1);
    }
    private static void addUIshape(int slideSet, int x, int y, int slide, String name){
        float scale=1/SubViewer.getConfig().global.scale;
        Relay.addImageshape(slideSet,(int)(x*scale),(int)(y*scale),slide-1, name);
    }
}
