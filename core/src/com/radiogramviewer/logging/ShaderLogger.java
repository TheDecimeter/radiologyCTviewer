package com.radiogramviewer.logging;

import java.util.ArrayList;

public class ShaderLogger {
    public final static char custom='c',gray='g',value='v',remove='r', invoke='i';

    public static boolean log=true;

    ArrayList<Node> shaders;
    public ShaderLogger(){
        reset();
    }

    public void reset(){
        shaders=new ArrayList<Node>();
        log=true;
    }

    public void add(char type, String name, String vertex, String fragment){
        if(log)
            shaders.add(new Node(type,name,vertex,fragment,Timing.getMillis()));
    }
    public void add(char type, String name, float level, float width){
        add(type, name,""+level, ""+width);
    }
    public void remove(String name){
        add(remove,name,"","");
    }
    public void invoke(String name){
        add(invoke,name,"","");
    }

    public String get(String cs, String vs){
        StringBuilder r=new StringBuilder();
        for(Node n : shaders){
            n.appendSelf(r,cs);
            r.append(vs);
        }
        return r.toString();
    }

    class Node{
        final long time;
        final char type;
        final String v,f,name;

        public Node(char type, String name, String v, String f, long time){
            this.time=time;
            this.v=v;
            this.f=f;
            this.type=type;
            this.name=name;
        }
        public void appendSelf(StringBuilder b, String cs){
            if(type==invoke)
                b.append(type).append(cs).append(name).append(cs).append(time);
            else if(type==remove)
                b.append(type).append(cs).append(name).append(cs).append(time);
            else
                b.append(type).append(cs).append(name).append(cs).append(v).append(cs).append(f).append(cs).append(time);
        }
    }
}
