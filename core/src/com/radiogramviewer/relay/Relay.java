package com.radiogramviewer.relay;

import com.radiogramviewer.MainViewer;
import com.radiogramviewer.SubViewer;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ScrollFollower;

import java.util.List;

public class Relay {
    public static final int dont=-1, loaded=21,ready=22,error=-3,pending=-2;

    private static int viewWidth, viewHeight;

    private static List<ClickFollower> click;
    private static List<ScrollFollower> scrollTimes;
    private static Constants constants;

    private static int loadingState=pending; //informs external javascript of loading state when it changes

    public static void initMain(Constants constants, int width, int height){
        Relay.constants=constants;
        Relay.viewWidth=width;
        Relay.viewHeight=height;
    }
    public static void initLogs(List<ClickFollower> click, List<ScrollFollower> srollTimes){
        Relay.click=click;
        Relay.scrollTimes=scrollTimes;
    }

    public static String getClicksAt(int at, String compDiv, String itemDiv){
        return click.get(at).getClicks(compDiv,itemDiv);
    }
    public static String getScrollTimesFor(int at, String compDiv, String itemDiv){
        return scrollTimes.get(at).getScrolls(compDiv,itemDiv);
    }
    public static void resetClicksFor(int at){
        click.get(at).reset();
    }
    public static void resetScrollsFor(int at){
        scrollTimes.get(at).reset();
    }
    public static void addClick(int at, int x, int y, int slide){
        click.get(at).updateClick(x,y,slide);
    }
    public static void addHighlight(int at, int x, int y, int slide){
        click.get(at).addHighlight(x,y,slide);
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
        constants.scrollMove(index);
    }
    public static void scrollStuck(int index){
        constants.scrollStuck(index);
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
}
