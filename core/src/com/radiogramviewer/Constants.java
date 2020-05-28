package com.radiogramviewer;

import com.badlogic.gdx.math.Vector2;

public interface Constants {
    int d=1,w=2,e=3;
    int getMode();
    void print(String s, int type);
    long getTime();
    void addControls(Controls c);
    void inputOccured();
    void clickAdded(String click);
    void clickRemoved(String click);
    void loadingStateChanged(int state);
    float getScale(int originalWidth);
}
