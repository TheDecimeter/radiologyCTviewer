package com.radiogramviewer.relay;

import com.radiogramviewer.MainViewer;
import com.radiogramviewer.SubViewer;
import com.radiogramviewer.config.ShapeMaker;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ScrollFollower;

import java.util.List;

public class Relay {
    public static final int dont=-1, loaded=21,ready=22,error=-3,pending=-2;

    private static int viewWidth, viewHeight;

    private static List<ClickFollower> click;
    private static List<ScrollFollower> scrollTimes;
    private static Constants constants;
    private static ShapeMaker shapes;

    private static int loadingState; //informs external javascript of loading state when it changes

    public static void initMain(Constants constants, int width, int height){
        loadingState=pending;
        Relay.constants=constants;
        Relay.viewWidth=width;
        Relay.viewHeight=height;
    }
    public static void initLogs(List<ClickFollower> click, List<ScrollFollower> scrollTimes, ShapeMaker shapes){
        Relay.click=click;
        Relay.scrollTimes=scrollTimes;
        Relay.shapes=shapes;
    }

    public static String getClicksAt(int at, String compDiv, String itemDiv){
        if(!at(at)) return "";
        return click.get(at).getClicks(compDiv,itemDiv);
    }
    public static String getScrollTimesFor(int at, String compDiv, String itemDiv){
        if(!at(at)) return "";
        return scrollTimes.get(at).getScrolls(compDiv,itemDiv);
    }
    public static void resetClicksFor(int at){
        if(!at(at)) return;
        click.get(at).reset();
    }
    public static void resetScrollsFor(int at){
        if(!at(at)) return;
        scrollTimes.get(at).reset();
    }
    public static void addViewMessage(int at, String msg){
        if(!at(at)) return;
        scrollTimes.get(at).logMessage(msg);
    }
    public static boolean addClick(int at, int x, int y, int slide){
        if(!at(at)) return false;
        return click.get(at).updateClick(x,y,slide);
    }
    public static void addInputMessage(int at, String message){
        if(!at(at)) return;
        click.get(at).addMessage(message);
    }
    public static void addHighlight(int at, int x, int y, int slide){
        if(!at(at)) return;
        click.get(at).addHighlight(x,y,slide);
    }
    public static void clearHighlights(int at){
        if(!at(at)) return;
        click.get(at).resetHighlights();
    }

    public static boolean addUIshape(int at, int x, int y, int slide, String name){
        int shapeIndex=shapes.get(name);
        if(shapeIndex==-1) return false;
        if(!at(at)) return false;
        return click.get(at).addUIshape(x,y,slide,shapeIndex);
    }
    public static boolean addImageshape(int at, int x, int y, int slide, String name){
        int shapeIndex=shapes.get(name);
        if(shapeIndex==-1) return false;
        if(!at(at)) return false;
        return click.get(at).addImageShape(x,y,slide,shapeIndex);
    }



    public static void inputOccured(){
        constants.inputOccured();
    }
    public static void clickAdded(String click){
        constants.clickAdded(""+ SubViewer.getSlideMode()+","+click);
    }
    public static void clickRemoved(String click){
        constants.clickRemoved(""+SubViewer.getSlideMode()+","+click);
    }

    public static void scrollMove(int index){
        constants.scrollMove(index+1);
    }
    public static void scrollStuck(int index){
        constants.scrollStuck(index+1);
    }

    public static void addToTextureInfoPacket(String msg){
        constants.addToPacket(msg);
    }
    public static int loadingState(){
        return loadingState;
    }
    public static void changeLoadingState(int state){
        if(loadingState==state)
            return;
        loadingState=state;
        constants.loadingStateChanged(loadingState);
    }
    public static int getWidth(){return viewWidth;}
    public static int getHeight(){return viewHeight;}
    public static Constants getConstants(){return constants;}

    private static boolean at(int at){
        if(at<0 ||at>19) {
            P.e("image index is out of bounds");
            return false;
        }
        return true;
    }
}
