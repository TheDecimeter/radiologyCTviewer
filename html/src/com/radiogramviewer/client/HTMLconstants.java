package com.radiogramviewer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.logging.ClickNode;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.Controls;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.graphics.shaders.WindowingShaders;
import com.radiogramviewer.relay.Relay;

public class HTMLconstants implements Constants {

    private final static int keyDown=0, keyUp=1;
    private static int mode=MainViewer.none;
    private static Controls controller;
    private static StringBuilder packet;

    public HTMLconstants(){
        packet=new StringBuilder();
        mode=MainViewer.none;
        exportStaticMethods();
        exportStaticVariables(Relay.ready, Relay.loaded, Relay.pending, Relay.error);
    }

    @Override
    public int getMode() {
        return mode;
    }

    public static String getClicksFor(int slideSet, String compDiv, String itemDiv) {
        compDiv= Config.fix(compDiv);
        itemDiv=Config.fix(itemDiv);
        return Relay.getClicksAt(slideSet-1,compDiv,itemDiv);
    }

    public static String getScrollTimesFor(int slideSet, String compDiv, String itemDiv) {
        compDiv= Config.fix(compDiv);
        itemDiv=Config.fix(itemDiv);
        return Relay.getScrollTimesFor(slideSet-1,compDiv,itemDiv);
    }

    public static void resetClicksFor(int slideSet){
        Relay.resetClicksFor(slideSet-1);
    }
    public static void resetScrollsFor(int slideSet){
        Relay.resetScrollsFor(slideSet-1);
    }
    public static void addClick(int slideSet, int x, int y, int slide){
        float scale=1/MainViewer.getConfig().global.scale;
        Relay.addClick(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
    }
    public static void addHighlight(int slideSet, int x, int y, int slide){
        float scale=1/MainViewer.getConfig().global.scale;
        Relay.addHighlight(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
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
    public void clickAdded(String click) {
        nativeClickAdded(click);
    }

    @Override
    public void clickRemoved(String click) {
        nativeClickRemoved(click);
    }

    @Override
    public void scrollMove(int index) {
        nativeScrollMoved(index);
    }

    @Override
    public void scrollStuck(int index) {
        nativeScrollStuck(index);
    }

    @Override
    public void loadingStateChanged(int state) {
        nativeLoadingStateChanged(state);
    }

    @Override
    public void processingState(int remaining, float progress) {
        nativeProcessingChanged(remaining,progress);
    }

    @Override
    public float getScale(int originalWidth) {
        if(nativeWindowSizeSpecified())
            return nativeWindowWidth()/originalWidth;
        return 1;
    }

    @Override
    public void addToPacket(String msg) {
        packet.append(msg).append(";");
    }
    public static String getTextureInfoPacket(){
        return packet.toString();
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
        return controller.getCurrentSlide()+1;
    }
    public static int getCurrentMode(){
        return MainViewer.getSlideMode();
    }

    public static void resetLastClick(){
        Controls.lastClick=new ClickNode(0,0,0);
        Controls.z=0;
    }


    public static boolean addShader(String name, String vertex, String fragment){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.generateShader(vertex,fragment);
        if(p==null||!p.isCompiled())
            return false;
        ShaderManager.addShader(name, p);
        return true;
    }

    public static boolean addWindowShaderGray(String name, float level, float width){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowGray(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderManager.addShader(name, p);
        return true;
    }
    public static boolean addWindowShaderValue(String name, float level, float width){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowValue(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderManager.addShader(name, p);
        return true;
    }

    private static boolean illegalName(String name){
        if(!name.equals("off"))
            return false;
        MainViewer.println("The name \"off\" is reserved.",Constants.e);
        return true;
    }


    public static void removeShader(String name){
        ShaderManager.removeShader(name);
    }

    public static void setShader(String name){
        ShaderManager.setShader(name);
    }

    public static void freezeInput(boolean freeze){
        Controls.freeze=freeze;
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
        return Relay.getHeight();
    }
    public static int getViewerWidth(){
        return Relay.getWidth();
    }
    public static double getViewerWidthInches(){
        return Relay.getWidth()/(Gdx.graphics.getDensity()*160);
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
                $wnd.viewerListenerInput();
            }
            }-*/;
    public static native int nativeClickAdded(String click)/*-{
            if (typeof $wnd.viewerListenerClickAdded === "function"){
                $wnd.viewerListenerClickAdded(click);
            }
            }-*/;
    public static native int nativeClickRemoved(String click)/*-{
            if (typeof $wnd.viewerListenerClickRemoved === "function"){
                $wnd.viewerListenerClickRemoved(click);
            }
            }-*/;
    public static native int nativeScrollMoved(int index)/*-{
            if (typeof $wnd.viewerListenerScrollSuccess === "function"){
                $wnd.viewerListenerScrollSuccess(index);
            }
            }-*/;
    public static native int nativeScrollStuck(int index)/*-{
            if (typeof $wnd.viewerListenerScrollFail === "function"){
                $wnd.viewerListenerScrollFail(index);
            }
            }-*/;

    public static native void nativeLoadingStateChanged(int state)/*-{
            if (typeof $wnd.viewerListenerLoadingStateChanged === "function"){
                $wnd.viewerListenerLoadingStateChanged(state);
            }
             else{
                console.log("failed to notify viewerListenerLoadingStateChanged(int) of loading occurrence");
            }
            }-*/;
    public static native void nativeProcessingChanged(int remaining, float progress)/*-{
            if (typeof $wnd.viewerListenerProcessing === "function"){
                $wnd.viewerListenerProcessing(remaining,progress);
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

    public static native void exportStaticVariables(int readyFlag, int loadedFlag, int pendingFlag, int failedFlag)/*-{
            $wnd.FLAGviewerReady=readyFlag;
            $wnd.FLAGviewerLoaded=loadedFlag;
            $wnd.FLAGviewerPendingUnknown=pendingFlag;
            $wnd.FLAGviewerFailedLoad=failedFlag;
            }-*/;

    public static native void exportStaticMethods() /*-{
       $wnd.viewerGetViewerDensityFactor = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerDensityFactor());
       $wnd.viewerGetWidth = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerWidth());
       $wnd.viewerGetWidthInches = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerWidthInches());
       $wnd.viewerGetHeight = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerHeight());
       $wnd.viewerGetTextureInfo = $entry(@com.radiogramviewer.client.HTMLconstants::getTextureInfoPacket());

       $wnd.viewerSetDragDistance = $entry(@com.radiogramviewer.client.HTMLconstants::setDragDistance(I));
       $wnd.viewerPipeInput = $entry(@com.radiogramviewer.client.HTMLconstants::pipeInput(II));
       $wnd.viewerFreezeInput = $entry(@com.radiogramviewer.client.HTMLconstants::freezeInput(Z));

       $wnd.viewerResetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::resetLastClick());
       $wnd.viewerGetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::getLastClick());
       $wnd.viewerGetCurrentSlide = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentSlide());
       $wnd.viewerGetCurrentSet = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentMode());

       $wnd.viewerGetUpTime = $entry(@com.radiogramviewer.client.HTMLconstants::getUpTime());
       $wnd.viewerGetStartTime = $entry(@com.radiogramviewer.client.HTMLconstants::getStartTime());

       $wnd.viewerSetSlide = $entry(@com.radiogramviewer.client.HTMLconstants::setMode(I));
       $wnd.viewerSetSlideAt = $entry(@com.radiogramviewer.client.HTMLconstants::setModeAt(II));

       $wnd.viewerGetClicksFor = $entry(@com.radiogramviewer.client.HTMLconstants::getClicksFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerGetScrollTimesFor = $entry(@com.radiogramviewer.client.HTMLconstants::getScrollTimesFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerResetScrolls = $entry(@com.radiogramviewer.client.HTMLconstants::resetScrollsFor(I));
       $wnd.viewerResetClicks = $entry(@com.radiogramviewer.client.HTMLconstants::resetClicksFor(I));

       $wnd.viewerSimulateClick = $entry(@com.radiogramviewer.client.HTMLconstants::addClick(IIII));
       $wnd.viewerAddHighlight = $entry(@com.radiogramviewer.client.HTMLconstants::addHighlight(IIII));

       $wnd.viewerAddCustomShader = $entry(@com.radiogramviewer.client.HTMLconstants::addShader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;));
       $wnd.viewerAddWindowingShaderGray = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderGray(Ljava/lang/String;FF));
       $wnd.viewerAddWindowingShaderValue = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderValue(Ljava/lang/String;FF));
       $wnd.viewerRemoveShader = $entry(@com.radiogramviewer.client.HTMLconstants::removeShader(Ljava/lang/String;));
       $wnd.viewerSetShader = $entry(@com.radiogramviewer.client.HTMLconstants::setShader(Ljava/lang/String;));
    }-*/;
}
