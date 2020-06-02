package com.radiogramviewer.coroutine;


import com.radiogramviewer.MainViewer;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.logging.Timing;
import com.radiogramviewer.relay.P;

import java.util.ArrayList;

/**
 * This runner will run as many coroutines as it can in the alloted time. It first tries
 * to finish one co routine before working on the next.
 */
public class CoroutineRunner {
    private ArrayList<Coroutine> coroutines;
    final float time;
    final Constants constants;
    /**
     * Create a new coroutine runner
     * @param time how long to run routines for before yielding (milliseconds)
     */
    public CoroutineRunner(float time, Constants constants) {
        coroutines = new ArrayList<Coroutine>();
        this.time=time;
        this.constants=constants;
    }

    public void add(Coroutine c) {
        if (c.workAvailable()) {
//            MainViewer.println("adding " + c, Constants.d);
            coroutines.add(c);
        }
    }

    public void invertWork(){
        ArrayList<Coroutine> n=new ArrayList<Coroutine>(coroutines.size());
        for (int i=coroutines.size()-1; i>=0; i--)
            n.add(coroutines.get(i));
        coroutines=n;
    }

    public boolean done(){
        if(coroutines.isEmpty())
            return true;
        for (int i=coroutines.size()-1; i>=0; i--) {
            if (coroutines.get(i).workAvailable()) {
                return false;
            }
            else {
                coroutines.remove(i);
            }
        }
        return true;
    }

    public boolean runOne() {
        boolean ret=false;
        float startTime= Timing.getMillis();
        for (int i=coroutines.size()-1; i>=0; i--) {
            Coroutine t = coroutines.get(i);
            if (t.workAvailable()) {
                while(t.workAvailable()) {
                    ret = true;
                    t.run();
                    if (Timing.getMillis() - startTime > time) {
                        constants.processingState(coroutines.size(),t.progress());
                        return true;
                    }
                }
                //MainViewer.println("      running one "+t, Constants.d);

            }
            else {
                coroutines.remove(i);
                P.d("removing one "+t);
            }
        }
        return ret;
    }
}
