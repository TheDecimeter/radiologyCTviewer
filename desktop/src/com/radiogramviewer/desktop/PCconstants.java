package com.radiogramviewer.desktop;

import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.Controls;

/**
 * Mostly a dummy class to mock what would be an interface with outside JavaScript
 * So that a pc build can be made quickly for debugging
 */
public class PCconstants implements Constants {


    @Override
    public int getMode() {
        return 3;
    }




    @Override
    public void print(String s, int code) {
        switch(code){
            case Constants.d:
                System.out.println("Msg: "+s);
                return;
            case Constants.w:
                System.out.println("Warning: "+s);
                return;
            case Constants.e:
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
        //return 555f/originalWidth;
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
}
