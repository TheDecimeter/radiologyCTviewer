package com.radiogramviewer.logging;

import java.util.ArrayList;

public class ShaderLogger {
    public final static char custom='c',gray='g',value='v',remove='r', invoke='i', message='m';

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
        add(remove,name,null,null);
    }
    public void invoke(String name){
        add(invoke,name,null,null);
    }
    public void message(String msg){
        add(message,msg,null,null);
    }

    public String get(String cs, String vs){
        StringBuilder r=new StringBuilder();
        for(Node n : shaders){
            n.append(r,cs,vs);
        }
        return r.toString();
    }

    class Node implements LogNode{
        final long time;
        final char type;
        final String v,f,name;

        Node(char type, String name, String v, String f, long time){
            this.time=time;
            this.v=v;
            this.f=f;
            this.type=type;
            this.name=name;
        }

        @Override
        public void append(StringBuilder b, String cs, String vs) {
            if(type==invoke)
                b.append(invoke).append(cs).append(name).append(cs).append(time).append(vs);
            else if(type==remove)
                b.append(remove).append(cs).append(name).append(cs).append(time).append(vs);
            else if (type==message)
                b.append(message).append(cs).append(name).append(cs).append(time).append(vs);
            else
                b.append(type).append(cs).append(name).append(cs).append(v).append(cs).append(f).append(cs).append(time).append(vs);
        }
    }
}
