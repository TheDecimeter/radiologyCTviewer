package com.radiogramviewer.coroutine;

import com.radiogramviewer.relay.Constants;

public class CoroutineConstantRunner {
    private final boolean run;
    private final Constants constants;

    public CoroutineConstantRunner(Constants constants){
        this.constants=constants;
        run=constants.hasCoroutine();
    }

    public void run(){
        if(run)
            constants.runCoroutine();
    }
}
