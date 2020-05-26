package com.radiogramviewer.client;

import com.badlogic.gdx.Input;
import com.radiogramviewer.ClickNode;
import com.radiogramviewer.Config;
import com.radiogramviewer.Constants;
import com.radiogramviewer.Controls;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.Timing;

public class HTMLconstants implements Constants {

    private final static int keyDown=0, keyUp=1;
    private static int mode=MainViewer.none;
    private static Controls controller;
    private static float scale;

    public HTMLconstants(){
        mode=MainViewer.none;
        exportStaticMethods();
        scale=1/MainViewer.getConfig().global.scale;
    }

    @Override
    public int getMode() {
        return mode;
    }

    public static String getClicksFor(int slideSet, String compDiv, String itemDiv) {
        compDiv= Config.fix(compDiv);
        itemDiv=Config.fix(itemDiv);
        return MainViewer.getClicksAt(slideSet-1,compDiv,itemDiv);
    }

    public static String getScrollTimesFor(int slideSet, String compDiv, String itemDiv) {
        compDiv= Config.fix(compDiv);
        itemDiv=Config.fix(itemDiv);
        return MainViewer.getScrollTimesFor(slideSet-1,compDiv,itemDiv);
    }

    public static void resetClicksFor(int slideSet){
        MainViewer.resetClicksFor(slideSet-1);
    }
    public static void resetScrollsFor(int slideSet){
        MainViewer.resetScrollsFor(slideSet-1);
    }
    public static void addClick(int slideSet, int x, int y, int slide){
        MainViewer.addClick(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
    }
    public static void addHighlight(int slideSet, int x, int y, int slide){
        MainViewer.addHighlight(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
    }

    @Override
    public void print(String s, int code) {

        switch(code){
            case Constants.d:
                if(nativeConsoleOut("Msg: "+s))
                    return;
                ConsolePrint("Msg: "+s);
                return;
            case Constants.w:
                if(nativeConsoleOut("Warning: "+s))
                    return;
                ConsolePrintWarning("Warning: "+s);
                return;
            case Constants.e:
                if(nativeConsoleOut("ERROR: "+s))
                    return;
                ConsolePrintError(" ERROR: "+s);
                return;
        }
    }

    @Override
    public long getTime() {
        return (long)getJSTime();
    }

    @Override
    public void addControls(Controls c) {
        controller=c;
    }

    @Override
    public void inputOccured() {
        nativeInputReceived();
    }

    @Override
    public void loadingStateChanged(int state) {
        nativeLoadingStateChanged(state);
    }

    @Override
    public float getScale(int originalWidth) {
        if(nativeWindowSizeSpecified())
            return nativeWindowWidth()/originalWidth;
        return 1;
    }


    public static void setMode(int mode){
        MainViewer.setSlideMode(mode,MainViewer.dont);
        HTMLconstants.mode=mode;
    }
    public static void setModeAt(int mode, int at){
        MainViewer.setSlideMode(mode,at-1);
        HTMLconstants.mode=mode;
    }
    public static void setDragDistance(int distance){
        Controls.setDragDist(distance);
    }
    public static String getLastClick(){
        return Controls.lastClick.toString(Controls.z);
    }
    public static int getCurrentSlide(){
        return controller.getCurrentSlide();
    }
    public static int getCurrentMode(){
        return MainViewer.getSlideMode();
    }

    public static void resetLastClick(){
        Controls.lastClick=new ClickNode(0,0,0);
        Controls.z=0;
    }

    public static void pipeInput(int key, int eventType){
        if(controller!=null){
            if(eventType==keyDown){
                controller.keyDown(htmlToJavaKeyConverter(key));
            }
            else if(eventType==keyUp){
                controller.keyUp(htmlToJavaKeyConverter(key));
            }
        }
    }
    private static int htmlToJavaKeyConverter(int htmlKey){
        switch(htmlKey){
            case 87: return Input.Keys.W;
            case 83: return Input.Keys.S;
            case 40: return Input.Keys.UP;
            case 38: return Input.Keys.DOWN;
        }
        return 0;
    }

    public static int getViewerHeight(){
        return MainViewer.getHeight();
    }
    public static int getViewerWidth(){
        return MainViewer.getWidth();
    }
    public static double getViewerDensityFactor(){
        return MainViewer.getConfig().global.scale;
    }

    public static double getUpTime(){
        return Timing.getMillis();
    }
    public static double getStartTime(){
        return Timing.getStartTime();
    }


    public static native boolean nativeConsoleOut(String msg)/*-{
            if (typeof $wnd.viewerConsoleOut === "function"){
                $wnd.viewerConsoleOut(msg);
                return true;
            }
            else
                return false;
            }-*/;

    public static native boolean nativeWindowSizeSpecified()/*-{
            return (typeof $wnd.viewerStatsWidth === "function");
            }-*/;

    public static native float nativeWindowWidth()/*-{
            return $wnd.viewerStatsWidth();
            }-*/;

    public static native int nativeInputReceived()/*-{
            if (typeof $wnd.viewerListenerInput === "function"){
                $wnd.viewerListenerInput()
            }
            }-*/;

    public static native void nativeLoadingStateChanged(int state)/*-{
            if (typeof $wnd.viewerListenerLoadingStateChanged === "function"){
                $wnd.viewerListenerLoadingStateChanged(state)
            }
             else{
                console.log("failed to notify viewerListenerLoadingStateChanged(int) of loading occurrence");
            }
            }-*/;

    public static native void ConsolePrint(String msg)/*-{
            console.log(msg);
            }-*/;

    public static native void ConsolePrintWarning(String msg)/*-{
            console.warn(msg);
            }-*/;
    public static native void ConsolePrintError(String msg)/*-{
            console.error(msg);
            }-*/;


    public static native double getJSTime()/*-{
            return Date.now();
            }-*/;


    public static native void exportStaticMethods() /*-{
       $wnd.viewerGetViewerDensityFactor = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerDensityFactor());
       $wnd.viewerGetViewerWidth = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerWidth());
       $wnd.viewerGetViewerHeight = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerHeight());
       $wnd.viewerSetDragDistance = $entry(@com.radiogramviewer.client.HTMLconstants::setDragDistance(I));
       $wnd.viewerPipeInput = $entry(@com.radiogramviewer.client.HTMLconstants::pipeInput(II));
       $wnd.viewerResetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::resetLastClick());
       $wnd.viewerGetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::getLastClick());
       $wnd.viewerGetCurrentSlide = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentSlide());
       $wnd.viewerGetCurrentMode = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentMode());
       $wnd.viewerGetUpTime = $entry(@com.radiogramviewer.client.HTMLconstants::getUpTime());
       $wnd.viewerGetStartTime = $entry(@com.radiogramviewer.client.HTMLconstants::getStartTime());
       $wnd.viewerSetMode = $entry(@com.radiogramviewer.client.HTMLconstants::setMode(I));
       $wnd.viewerSetModeAt = $entry(@com.radiogramviewer.client.HTMLconstants::setModeAt(II));
       $wnd.viewerGetClicksFor = $entry(@com.radiogramviewer.client.HTMLconstants::getClicksFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerGetScrollTimesFor = $entry(@com.radiogramviewer.client.HTMLconstants::getScrollTimesFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerResetScrolls = $entry(@com.radiogramviewer.client.HTMLconstants::resetScrollsFor(I));
       $wnd.viewerResetClicks = $entry(@com.radiogramviewer.client.HTMLconstants::resetClicksFor(I));
       $wnd.viewerAddClick = $entry(@com.radiogramviewer.client.HTMLconstants::addClick(IIII));
       $wnd.viewerAddHighlight = $entry(@com.radiogramviewer.client.HTMLconstants::addHighlight(IIII));
    }-*/;
}
