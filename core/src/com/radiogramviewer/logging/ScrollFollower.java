package com.radiogramviewer.logging;

import com.radiogramviewer.config.Config;

import java.util.ArrayList;

/**
 * Keep track of scrolling behavior
 */
public class ScrollFollower {

    private static final String focus="f",scroll="s", blur="b";

    private ArrayList<Node> slideTimes;
    private Config c;
    private int lastIndex=0;

    public ScrollFollower(Config c, int slices){
        createTimings(slices);
        this.c=c;
    }

    private void createTimings(int slices){
        slideTimes=new ArrayList<Node>(slices); //size is technically unnecessary
    }

    /**
     * Make sure to save timings before resetting.
     */
    public void reset(){
        createTimings(slideTimes.size());
    }

    /**
     * record an event where focus is gained on a particular slide set
     * @param slide the slide that was visible when the slide set gained focus
     */
    public void logOpen(int slide){
        if(!c.record.logScrolling)
            return;
        recordChange(slide,focus);
    }
    /**
     * record an event where focus is lost on a particular slide set
     * @param slide the slide that was visible when the slide set lost focus
     */
    public void logClose(int slide){
        if(!c.record.logScrolling)
            return;
        recordChange(slide,blur);
    }

    /**
     * record an event where a new slide was scrolled to
     * @param slide the slide that is now visible
     */
    public void logScroll(int slide){
        if(!c.record.logScrolling)
            return;
        if(lastIndex!=slide) {//don't record focus being gained on a slide, and a scroll to it
            recordChange(slide, scroll);
        }
    }

    private void recordChange(int index, String transition){
        lastIndex=index;
        slideTimes.add(new Node(index,Timing.getMillis(),transition));
    }

    /**
     * Save the scroll behavior as a string
     * @param cs what separates each scroll's component (slide index, time, focus flag)
     * @param vs what separates each scroll
     * @return
     */
    public String getScrolls(String cs, String vs){
        StringBuilder b=new StringBuilder();
        for(Node n : slideTimes)
            b.append(n.slide+1).append(cs).append(n.time).append(cs).append(n.focus).append(vs);
        return b.toString();
    }

    /**
     * simple class for keeping track of each scroll
     */
    private class Node{
        public int time;
        public int slide;
        public String focus;
        public Node(int slide, long time, String focus){
            this.time=(int)time;
            this.slide=slide;
            this.focus=focus;
        }
    }
}
