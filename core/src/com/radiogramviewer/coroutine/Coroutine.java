package com.radiogramviewer.coroutine;

public interface Coroutine extends Runnable {
    boolean workAvailable();
    float progress();
}
