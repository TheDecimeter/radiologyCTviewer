package com.radiogramviewer.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.P;

import java.nio.IntBuffer;
import java.util.HashMap;

public class Config {

    public final WindowClass window;
    public final DebugClass debug;
    public final ClickClass click;
    public final ControlClass input;
    public final GlobalClass global;
    public final RecordClass record;

    private final float scale;

    public Config(Constants constants){
        HashMap<String,HashMap<String,String>> vals=vals();
        HashMap<String,String> vpWindow=getProperties("window",vals);

        HashMap<String,String> vp=getProperties("debug",vals);
        debug=new DebugClass(getBool("quickKeys",vp),maxSize(getBool("fakeGPU",vp),getInt("fakeTextureMax",vp)));

        vp=getProperties("global",vals);

        int originalWidth=getInt("width",vpWindow);
        boolean fakeDensity=getBool("fakeDensity",vp);
        scale=getScale(constants, originalWidth, fakeDensity);

        global=new GlobalClass(fakeDensity,1/scale, debug.gpuMaxTextureSize, getInt("overscan",vp), getBool("downscaleTexture",vp), getInt("yield",vp));

        window=new WindowClass(originalWidth,getInt("height",vpWindow),
                getInt("barWidth",vpWindow),getInt("barBorder",vpWindow)
                , getColor("barColor",vpWindow), getColor("borderColor",vpWindow));

        vp=getProperties("record",vals);
        record=new RecordClass(getBool("logScrolling",vp));


        vp=getProperties("click",vals);
        click=new ClickClass(getInt("radius",vp),getInt("thickness",vp),getInt("depth",vp),getColor("color",vp),
                getColor("highlightColor",vp),getInt("highlightRadius",vp),getInt("highlightThickness",vp),
                getBool("overwritelastclick",vp));

        vp=getProperties("controls",vals);
        input=new ControlClass(getBool("wheel",vp),getBool("arrows",vp),getBool("wasd",vp),getFloat("holdTime",vp),getBool("drag",vp),getInt("dragDist",vp));

        System.out.println("finished config");
    }

    private float getScale(Constants constants, int originalWindowWidth, boolean fakeDensity){
        float scl=constants.getScale(originalWindowWidth);
        if(scl!=1){
            if(fakeDensity) {
                P.e("Custom width and Density Independant pixels don't work together, Defaulting to Custom width");
            }
            return scl;

        }
        if(fakeDensity)
            return Gdx.graphics.getDensity();
        return 1;
    }


    private HashMap<String,HashMap<String,String>> vals(){
        HashMap<String,HashMap<String,String>>r=new HashMap<String,HashMap<String,String>>();

        FileHandle file= Gdx.files.internal("config.ini");
        String text = file.readString();
        String lines[] = text.split("\\r?\\n");

        int lineNum=0;
        HashMap<String,String> p=null;
        for(String line : lines) {
            if(line.length()==0)
                continue;
            if(line.charAt(0)==';')
                continue;
            if(line.charAt(0)=='['){
                String name=line.trim();
                name=name.replaceAll("\\[|\\]","");
                p= new HashMap<String, String>();
                r.put(c(name),p);
            }
            else{
                String [] s=line.split("=");
                p.put(c(s[0]),c(s[1]));
            }
        }

        return r;
    }

    private HashMap<String,String> getProperties(String section, HashMap<String,HashMap<String,String>> vals){
        String k=section.toLowerCase();
        if(!vals.containsKey(k))
            P.e("could not find section "+k);
        return vals.get(k);
    }

    private int getInt(String s, HashMap<String,String> p){
        int r=Integer.parseInt(getProperty(s,p));
        return r;
    }
    private Color getColor(String s, HashMap<String,String> p){
        Color r=parseColor(getProperty(s,p));
        return r;
    }
    private float getFloat(String s, HashMap<String,String> p){
        float r=Float.parseFloat(getProperty(s,p));
        return r;
    }
    private boolean getBool(String s, HashMap<String,String> p){
        return getBoolean(getProperty(s,p));
    }

    private String getProperty(String s, HashMap<String,String> p){
        String v=p.get(s.toLowerCase());
        if(v==null) {
            String con="";
            for (String k : p.keySet())
                con+="\n   "+k;
            P.e("could not find property " + s+con);
        }
        return v;
    }


    public static boolean getBoolean(String s){
        s=s.trim().toLowerCase();
        if(s.contains("f"))
            return false;
        if(s.contains("t"))
            return true;
        if(s.equals("0"))
            return false;
        if(s.equals("1"))
            return true;

        P.e("Failed to parse boolean value "+s);
        return false;
    }

    public static String fix(String in){
        in=in.replaceAll("\\n","\n");
        in=in.replaceAll("\\t","\t");
        in=in.replaceAll("\\r","\r");
        return in;
    }

    //used for seeing how large a texture can be on a specific platform's GPU
    private int maxSize(boolean fake, int fakeMax){
        if(fake)
            return fakeMax;
        IntBuffer buf = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
        return buf.get(0);
    }

    public class WindowClass{
        public final int height, width, barWidth, barBorder;
        public final Color barColor, borderColor;
        WindowClass(int width, int height, int barWidth, int barBorder, Color barColor, Color borderColor){
            this.height=(int)(height*scale);
            this.width=(int)(width*scale);
            this.barWidth=(int)(barWidth*scale);
            this.barBorder=(int)(barBorder*scale);
            this.barColor=barColor;
            this.borderColor=borderColor;
        }
    }

    public class RecordClass{
        public final boolean logScrolling;
        public RecordClass(boolean logScrolling){
            this.logScrolling=logScrolling;
        }
    }

    public class ClickClass{
        public final boolean overwriteLastClick;
        public final int radius,thickness, depth, highlightRadius,highlightThickness;
        public final Color color,highlightColor;
        ClickClass(int radius,int thickness, int depth, Color color,
                   Color highlightColor, int highlightRadius,int highlightThickness, boolean overwriteLastClick){
            this.radius=(int)(radius*scale);
            this.thickness=(int)(thickness*scale);
            this.depth=depth;
            this.color=color;
            this.highlightColor=highlightColor;
            this.highlightRadius=(int)(highlightRadius*scale);
            this.highlightThickness=(int)(highlightThickness*scale);
            this.overwriteLastClick=overwriteLastClick;
        }
    }

    public class ControlClass{
         public final boolean wheel,arrow,wasd,drag;
         public final float holdTime;
         public final int dragDist;
         ControlClass(boolean wheel,boolean arrow,boolean wasd,float holdTime, boolean drag, int dragDist){
            this.wheel=wheel;
            this.arrow=arrow;
            this.wasd=wasd;
            this.holdTime=holdTime;
            this.drag=drag;
            this.dragDist=(int)(dragDist*scale);
        }
    }

    public class DebugClass{
        public final boolean quickKeys;
        public final int gpuMaxTextureSize;
        DebugClass(boolean quickKeys, int gpuMaxTextureSize){
            this.quickKeys=quickKeys;
            this.gpuMaxTextureSize=gpuMaxTextureSize;
        }
    }

    public class GlobalClass{
        public final boolean densityIndepndant, downscaleTexture;
        public final float scale;
        public final int gpuMaxTextureSize, overscan, yieldMillis;
        GlobalClass(boolean densityIndepndant, float scale, int gpuMaxTextureSize, int overscan, boolean downscaleTexture, int yieldMillis){
            this.scale=scale;
            this.densityIndepndant=densityIndepndant;
            this.gpuMaxTextureSize=gpuMaxTextureSize;
            int tmp=(int)(overscan*Config.this.scale);
            if(tmp>1)
                this.overscan=tmp;
            else
                this.overscan=1;
            this.yieldMillis=yieldMillis;
            this.downscaleTexture=downscaleTexture;
        }
    }

    public Color parseColor(String in){
        String s=c(in);
        float r = 1, g = 1, b = 1, a = 1;
        try {
            if (s.length() >= 6) {
                r = ((float) Integer.parseInt(s.substring(0, 2), 16)) / 255;
                g = ((float) Integer.parseInt(s.substring(2, 4), 16)) / 255;
                b = ((float) Integer.parseInt(s.substring(4, 6), 16)) / 255;
            } else {
                if (s.length() >= 3) {
                    r = ((float) Integer.parseInt(s.substring(0, 1), 16)) / 15;
                    g = ((float) Integer.parseInt(s.substring(1, 2), 16)) / 15;
                    b = ((float) Integer.parseInt(s.substring(2, 3), 16)) / 15;
                }
                if (s.length() == 4) {
                    a = ((float) Integer.parseInt(s.substring(3, 4), 16)) / 15;
                }
            }
            if (s.length() == 8) {
                a = ((float) Integer.parseInt(s.substring(6, 8), 16)) / 255;
            }
        }
        catch(NumberFormatException e){
            P.e("Failed to parse color "+in);
        }
        return new Color(r,g,b,a);
    }

    public String c(String s){
        s=s.replaceAll("\"","");
        s=s.replaceAll("#","");
        s=s.trim();
        return s.toLowerCase();
    }
}
