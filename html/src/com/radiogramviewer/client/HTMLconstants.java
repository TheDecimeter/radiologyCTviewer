package com.radiogramviewer.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.radiogramviewer.SubViewer;
import com.radiogramviewer.graphics.SlideManager;
import com.radiogramviewer.graphics.shaders.ShaderManager;
import com.radiogramviewer.logging.ClickFollower;
import com.radiogramviewer.logging.ClickNode;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.logging.ShaderLogger;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.Controls;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.graphics.shaders.WindowingShaders;
import com.radiogramviewer.relay.P;
import com.radiogramviewer.relay.Relay;

public class HTMLconstants implements Constants {

    private final static int keyPressDown =0, keyPressUp =1, keyScrollDown=-2, keyScrollUp=-1;
    private static int mode=SubViewer.none;
    private static Controls controller;
    private static StringBuilder packet;

    public static String getViewerVersion() {return "v3";}


    public HTMLconstants(){
        exportStaticMethods();
        exportStaticVariables(Relay.ready, Relay.loaded, Relay.pending, Relay.error, keyScrollDown,keyScrollUp, keyPressDown, keyPressUp);
    }

    @Override
    public int getMode() {
        packet=new StringBuilder();
        mode=SubViewer.none;
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

    public static String getShaderLog(String cs, String vs) {
        return ShaderManager.logger.get(cs,vs);
    }
    public static void resetShaderLog() {
        ShaderManager.logger.reset();
    }

    public static void resetEverything(){MainViewer.setToReset();}

    public static void resetClicksFor(int slideSet){
        Relay.resetClicksFor(slideSet-1);
    }
    public static void resetScrollsFor(int slideSet){
        Relay.resetScrollsFor(slideSet-1);
    }
    public static void addViewMessage(int slideSet, String msg){
        Relay.addViewMessage(slideSet-1,msg);
    }
    public static boolean addClick(int slideSet, int x, int y, int slide){
        if(!validSlide(slide))return false;
        float scale=1/SubViewer.getConfig().global.scale;
        return Relay.addClick(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
    }
    public static void addInputMessage(int slideSet, String msg){
        Relay.addInputMessage(slideSet-1,msg);
    }
    public static void addHighlight(int slideSet, int x, int y, int slide){
        if(!validSlide(slide))return;
        float scale=1/SubViewer.getConfig().global.scale;
        Relay.addHighlight(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1);
    }
    public static void clearHighlightsFor(int slideSet){
        Relay.clearHighlights(slideSet-1);
    }


    public static boolean addShapeImage(String name, int slideSet, int x, int y, int slide){
        if(!validSlide(slide))return false;
        float scale=1/SubViewer.getConfig().global.scale;
        return Relay.addImageshape(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1,name);
    }
    public static boolean addShapeUI(String name, int slideSet, int x, int y, int slide){
        if(!validSlide(slide))return false;
        float scale=1/SubViewer.getConfig().global.scale;
        return Relay.addUIshape(slideSet-1,(int)(x*scale),(int)(y*scale),slide-1,name);
    }


    private static boolean validSlide(int slide){
        if(slide==0){
            P.e("Slides start at 1");
            return false;
        }
        return true;
    }

    @Override
    public void print(String s, int code) {

        switch(code){
            case P.d:
                if(nativeConsoleOut("Msg: "+s))
                    return;
                ConsolePrint("Msg: "+s);
                return;
            case P.w:
                if(nativeConsoleOut("Warning: "+s))
                    return;
                ConsolePrintWarning("Warning: "+s);
                return;
            case P.e:
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

    @Override
    public boolean hasCoroutine() {
        return nativeHasCoroutine();
    }

    @Override
    public void runCoroutine() {
        nativeRunCoroutine();
    }


    public static String getTextureInfoPacket(){
        return packet.toString();
    }


//    public static void setMode(int mode){
//        SubViewer.setSlideMode(mode,SubViewer.dont);
//        HTMLconstants.mode=mode;
//    }

    public static void setModeAt(int mode, int at){
        SubViewer.setSlideMode(mode,optional(at,SubViewer.dont+1)-1);
        HTMLconstants.mode=mode;
    }
    public static void setDragDistance(int distance){
        Controls.setDragDist(distance);
    }
    public static void setHoldTime(float seconds) { SlideManager.holdFor=seconds;}
    public static void setScrollMax(int max){
        Controls.setScrollMax(max);
    }
    public static void setScrollSensitivity(float sensitivity){
        Controls.setScrollSensitivity(sensitivity);
    }
    public static String getLastClick(){
        return Controls.lastClick.toString();
    }
    public static int getCurrentSlide(){
        return controller.getCurrentSlide()+1;
    }
    public static int getCurrentMode(){
        return SubViewer.getSlideMode();
    }

    public static void resetLastClick(){
        Controls.lastClick=new ClickNode(0,0,0,0);
//        Controls.z=0;
    }


    public static boolean addShader(String name, String vertex, String fragment, boolean log){
        if(illegalName(name))
            return false;
        vertex=Config.fix(vertex);
        fragment=Config.fix(fragment);
        ShaderProgram p= WindowingShaders.generateShader(vertex,fragment);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.custom,name,vertex,fragment);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }

    public static boolean addWindowShaderGray16(String name, float level, float width, boolean log){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowGray16(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.highDepth,name,level,width);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }

    public static boolean addWindowShaderGray(String name, float level, float width, boolean log){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowGray(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.gray,name,level,width);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }
    public static boolean addWindowShaderGrayFull(String name, float level, float width, boolean log){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowGrayFull(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.fullGray,name,level,width);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }
    public static boolean addWindowShaderValueFull(String name, float level, float width, boolean log){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowValueFull(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.fullValue,name,level,width);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }
    public static boolean addWindowShaderValue(String name, float level, float width, boolean log){
        if(illegalName(name))
            return false;
        ShaderProgram p= WindowingShaders.windowValue(level,width);
        if(p==null||!p.isCompiled())
            return false;
        ShaderLogger.log=optional(log,true);
        ShaderManager.logger.add(ShaderLogger.value,name,level,width);
        ShaderManager.addShader(name, p);
        ShaderLogger.log=true;
        return true;
    }

    private static boolean illegalName(String name){
        if(!name.equals("off"))
            return false;
        P.e("The name \"off\" is reserved.");
        return true;
    }

    public static void addShaderMessage(String msg){
        boolean prev=ShaderLogger.log;
        ShaderLogger.log=true;
        ShaderManager.logger.message(msg);
        ShaderLogger.log=prev;
    }

    public static void removeShader(String name, boolean log){
        ShaderLogger.log=optional(log,true);
        ShaderManager.removeShader(name);
        ShaderLogger.log=true;
    }

    public static void setShader(String name, boolean log){
        ShaderLogger.log=optional(log,true);
        ShaderManager.setShader(name);
        ShaderLogger.log=true;
    }

    public static void scrollLock(boolean lock){
        SlideManager.scrollLock=lock;
    }
    public static void clickLock(boolean lock){
        ClickFollower.clickLock=lock;
    }
    public static void pipeInput(int key, int eventType){
        if(controller!=null){
            if(eventType== keyPressDown){
                controller.keyDown(htmlToJavaKeyConverter(key));
            }
            else if(eventType== keyPressUp){
                controller.keyUp(htmlToJavaKeyConverter(key));
            }
        }
    }
    private static int htmlToJavaKeyConverter(int htmlKey){
        switch(htmlKey){
            case keyScrollUp: return Input.Keys.UP;
            case keyScrollDown: return Input.Keys.DOWN;
            case 87: return Input.Keys.W;
            case 83: return Input.Keys.S;
            case 38: return Input.Keys.UP;
            case 40: return Input.Keys.DOWN;
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
        return SubViewer.getConfig().global.scale;
    }

    public static double getUpTime(){
        return Timing.getMillis();
    }
    public static double getStartTime(){
        return Timing.getStartTime();
    }

    @Override
    public void passKey(int key){
        switch(key){
            case Input.Keys.NUM_0:
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
//                addWindowShaderGray16("customPassKey1",.5f,1,true);
//                addWindowShaderGrayFull("customPassKey2",.5f,1,true);
//                addWindowShaderGray("customPassKey3",.5f,1,true);
                ShaderManager.addShader("customPassKey1", WindowingShaders.windowGray16(.5f,1));
                ShaderManager.addShader("customPassKey2", WindowingShaders.windowGrayFull(.5f,1));
                ShaderManager.addShader("customPassKey3", WindowingShaders.windowGray(.5f,1));
                break;
            case Input.Keys.NUM_3:
                ShaderManager.setShader("customPassKey1");
                break;
            case Input.Keys.NUM_4:
                addWindowShaderGray16("customPassKey1", .515f,0.075f,true);
                addWindowShaderGrayFull("customPassKey2", .515f,0.075f,true);
                addWindowShaderGray("customPassKey3", .515f,0.075f,true);
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
                P.d("CLICKS:\n"+Relay.getClicksAt(getCurrentMode(),",","\n")+"\nLast Click: "+s);
                String[] t=s.split(",");
                int x=Integer.parseInt(t[0]);
                int y=Integer.parseInt(t[1]);
                int z=Integer.parseInt(t[2]);

                addHighlight(getCurrentSlide(),x,y,getCurrentSlide());
                addShapeImage("mark",getCurrentMode(),x,y,getCurrentSlide());
                resetClicksFor(getCurrentMode());
                P.d("finished keypress");
                break;

            case Input.Keys.NUM_9:
                s=Controls.lastClick.toString();
                P.d("CLICKS:\n"+Relay.getClicksAt(getCurrentMode(),",","\n")+"\nLast Click: "+s);
                t=s.split(",");
                x=Integer.parseInt(t[0]);
                y=Integer.parseInt(t[1]);
                z=Integer.parseInt(t[2]);

                addShapeUI("mark",getCurrentMode(),x,y,getCurrentSlide());
                resetClicksFor(getCurrentMode());
                P.d("finished keypress");
                break;
        }
    }

    @Override
    public void finishedResetting() {
        nativeFinishedReset();
    }

    @Override
    public void forceDraw() {
        //TODO
    }

    private static native boolean optional(boolean b, boolean d)/*-{
            if (typeof b === "undefined" || b==null){
                return d;
            }
            else
                return b;
            }-*/;
    private static native int optional(int x, int d)/*-{
            if (typeof x === "undefined" || x==null){
                return d;
            }
            else
                return x;
            }-*/;


    public static native boolean nativeConsoleOut(String msg)/*-{
            if (typeof $wnd.viewerConsoleOut === "function"){
                $wnd.viewerConsoleOut(msg);
                return true;
            }
            else
                return false;
            }-*/;

    public static native boolean nativeFinishedReset()/*-{
            if (typeof $wnd.viewerEventResetFinished === "function"){
                $wnd.viewerEventResetFinished();
            }
            }-*/;

    public static native boolean nativeWindowSizeSpecified()/*-{
            return (typeof $wnd.viewerStatsWidth === "function");
            }-*/;

    public static native float nativeWindowWidth()/*-{
            return $wnd.viewerStatsWidth();
            }-*/;

    public static native boolean nativeHasCoroutine()/*-{
            return (typeof $wnd.viewerCoroutine === "function");
            }-*/;

    public static native float nativeRunCoroutine()/*-{
            $wnd.viewerCoroutine();
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

    public static native void exportStaticVariables(int readyFlag, int loadedFlag, int pendingFlag, int failedFlag, int keyScrollDN, int keyScrollUP, int keyPressDN, int keyPressUp)/*-{
            $wnd.FLAGviewerReady=readyFlag;
            $wnd.FLAGviewerLoaded=loadedFlag;
            $wnd.FLAGviewerPendingUnknown=pendingFlag;
            $wnd.FLAGviewerFailedLoad=failedFlag;

            $wnd.FLAGviewerKeyScrollUp=keyScrollUP;
            $wnd.FLAGviewerKeyScrollDown=keyScrollDN;
            $wnd.FLAGviewerKeyPressRelease=keyPressUp;
            $wnd.FLAGviewerKeyPressDown=keyPressDN;
            }-*/;

    public static native void exportStaticMethods() /*-{
       $wnd.viewerGetVersion = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerVersion());
       $wnd.viewerGetViewerDensityFactor = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerDensityFactor());
       $wnd.viewerGetWidth = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerWidth());
       $wnd.viewerGetWidthInches = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerWidthInches());
       $wnd.viewerGetHeight = $entry(@com.radiogramviewer.client.HTMLconstants::getViewerHeight());
       $wnd.viewerGetTextureInfo = $entry(@com.radiogramviewer.client.HTMLconstants::getTextureInfoPacket());

       $wnd.viewerSetDragDistance = $entry(@com.radiogramviewer.client.HTMLconstants::setDragDistance(I));
       $wnd.viewerSetButtonTime = $entry(@com.radiogramviewer.client.HTMLconstants::setHoldTime(F));
       $wnd.viewerSetScrollLimit = $entry(@com.radiogramviewer.client.HTMLconstants::setScrollMax(I));
       $wnd.viewerSetScrollSensitivity = $entry(@com.radiogramviewer.client.HTMLconstants::setScrollSensitivity(F));
       $wnd.viewerPipeInput = $entry(@com.radiogramviewer.client.HTMLconstants::pipeInput(II));
       $wnd.viewerScrollLock = $entry(@com.radiogramviewer.client.HTMLconstants::scrollLock(Z));
       $wnd.viewerClickLock = $entry(@com.radiogramviewer.client.HTMLconstants::clickLock(Z));

       $wnd.viewerResetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::resetLastClick());
       $wnd.viewerGetLastClick = $entry(@com.radiogramviewer.client.HTMLconstants::getLastClick());
       $wnd.viewerGetCurrentSlide = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentSlide());
       $wnd.viewerGetCurrentSet = $entry(@com.radiogramviewer.client.HTMLconstants::getCurrentMode());

       $wnd.viewerResetSize = $entry(@com.radiogramviewer.client.HTMLconstants::resetEverything());
       $wnd.viewerGetUpTime = $entry(@com.radiogramviewer.client.HTMLconstants::getUpTime());
       $wnd.viewerGetStartTime = $entry(@com.radiogramviewer.client.HTMLconstants::getStartTime());

       $wnd.viewerSetSlide = $entry(@com.radiogramviewer.client.HTMLconstants::setModeAt(II));

       $wnd.viewerGetShaderLog = $entry(@com.radiogramviewer.client.HTMLconstants::getShaderLog(Ljava/lang/String;Ljava/lang/String;));
       $wnd.viewerGetClicksFor = $entry(@com.radiogramviewer.client.HTMLconstants::getClicksFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerGetScrollTimesFor = $entry(@com.radiogramviewer.client.HTMLconstants::getScrollTimesFor(ILjava/lang/String;Ljava/lang/String;));
       $wnd.viewerResetScrolls = $entry(@com.radiogramviewer.client.HTMLconstants::resetScrollsFor(I));
       $wnd.viewerResetClicks = $entry(@com.radiogramviewer.client.HTMLconstants::resetClicksFor(I));
       $wnd.viewerResetHighlights = $entry(@com.radiogramviewer.client.HTMLconstants::clearHighlightsFor(I));
       $wnd.viewerResetShaderLog = $entry(@com.radiogramviewer.client.HTMLconstants::resetShaderLog());

       $wnd.viewerLogClickMessage = $entry(@com.radiogramviewer.client.HTMLconstants::addInputMessage(ILjava/lang/String;));
       $wnd.viewerLogScrollMessage = $entry(@com.radiogramviewer.client.HTMLconstants::addViewMessage(ILjava/lang/String;));
       $wnd.viewerLogShaderMessage = $entry(@com.radiogramviewer.client.HTMLconstants::addShaderMessage(Ljava/lang/String;));

       $wnd.viewerSimulateClick = $entry(@com.radiogramviewer.client.HTMLconstants::addClick(IIII));
       $wnd.viewerAddHighlight = $entry(@com.radiogramviewer.client.HTMLconstants::addHighlight(IIII));
       $wnd.viewerAddShapeToImage = $entry(@com.radiogramviewer.client.HTMLconstants::addShapeImage(Ljava/lang/String;IIII));
       $wnd.viewerAddShapeToUI = $entry(@com.radiogramviewer.client.HTMLconstants::addShapeUI(Ljava/lang/String;IIII));

       $wnd.viewerAddCustomShader = $entry(@com.radiogramviewer.client.HTMLconstants::addShader(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z));
       $wnd.viewerAddWindowingShaderGray16 = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderGray16(Ljava/lang/String;FFZ));
       $wnd.viewerAddWindowingShaderGray = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderGray(Ljava/lang/String;FFZ));
       $wnd.viewerAddWindowingShaderGrayFull = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderGrayFull(Ljava/lang/String;FFZ));
       $wnd.viewerAddWindowingShaderValue = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderValue(Ljava/lang/String;FFZ));
       $wnd.viewerAddWindowingShaderValueFull = $entry(@com.radiogramviewer.client.HTMLconstants::addWindowShaderValueFull(Ljava/lang/String;FFZ));
       $wnd.viewerRemoveShader = $entry(@com.radiogramviewer.client.HTMLconstants::removeShader(Ljava/lang/String;Z));
       $wnd.viewerSetShader = $entry(@com.radiogramviewer.client.HTMLconstants::setShader(Ljava/lang/String;Z));
    }-*/;
}
