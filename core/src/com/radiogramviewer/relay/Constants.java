package com.radiogramviewer.relay;

import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.Controls;

public interface Constants {
    int getMode();
    void print(String s, int type);
    long getTime();
    void addControls(Controls c);
    void inputOccured();
    void clickAdded(String click);
    void clickRemoved(String click);
    void scrollMove(int index);
    void scrollStuck(int index);
    void loadingStateChanged(int state);
    void processingState(int remaining, float progress);
    float getScale(int originalWidth);
    void addToPacket(String msg);
    boolean hasCoroutine();
    void runCoroutine();
    void passKey(int key);
    void finishedResetting();
    void forceDraw();
}
