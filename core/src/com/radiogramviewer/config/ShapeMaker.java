package com.radiogramviewer.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.radiogramviewer.graphics.DrawShape;
import com.radiogramviewer.relay.P;

import java.util.ArrayList;
import java.util.TreeMap;

public class ShapeMaker {
    private static final int square=0, circle=1, ring=2,unknown=-1;
    private ArrayList<Shape> shapes;
    private TreeMap<String,Integer> shapeMap;
    public final int depth;

    public ShapeMaker(Config config){
        shapes=new ArrayList<Shape>();
        shapeMap=new TreeMap<String, Integer>();

        depth=fillShapes(config);
    }

    private int fillShapes(Config config){
        int highestDepth=0;
        FileHandle file= Gdx.files.internal("shapes.txt");
        String text = file.readString();
        String lines[] = text.split("\\r?\\n");
        int lineNum=0;
        float scale=1/ config.global.scale;
        for(String line : lines){
            lineNum++;
            if(Config.isComment(line))
                continue;

            String [] c=line.split(",");
            if(c.length!=8)
            {
                P.e("shapes.txt: line "+lineNum+" with "+c.length+" elements. "+line);
                continue;
            }

            try {
                String name=c[0];
                int width=(int)Math.ceil(Integer.parseInt(c[1])*scale);
                int height=(int)Math.ceil(Integer.parseInt(c[2])*scale);
                int depth=(int)Math.ceil(Integer.parseInt(c[3])*scale);
                int weight=(int)Math.ceil(Integer.parseInt(c[4])*scale);
                boolean fade=Config.getBoolean(c[5]);
                int type=typeID(c[6]);
                Color color= Config.parseColor(c[7]);
                if(type==unknown) {
                    P.e("shapes.txt: invalid type for line " + lineNum + " valid types are 'circle', 'square', 'ring)");
                    continue;
                }

                Shape s=makeShape(width,height,depth,weight,fade,type,color);
                if(s==null){
                    P.e("shapes.txt: line "+lineNum+" failed to create shape. "+line);
                    continue;
                }
                shapes.add(s);
                shapeMap.put(name,shapes.size()-1);
                if(depth>highestDepth)
                    highestDepth=depth;
            }
            catch (Exception e){
                P.e("shapes.txt: line "+lineNum+" could not parse "+line);
            }
        }
        return highestDepth;
    }

    private int typeID(String name){
        switch(name.toLowerCase().charAt(0)){
            case 's':
                return square;//square
            case 'c':
                return circle;//circle
            case 'r':
                return ring;//ring
            default:
                return unknown;
        }
    }
    public int get(String name){
        if(shapeMap.containsKey(name)){
            return shapeMap.get(name);
        }
        return -1;
    }


    public Shape get(int i){
        return shapes.get(i);
    }

    private Shape makeShape (int width, int height, int depth, int weight, boolean fade, int type, Color color){
        switch(type){
            case square:
                if(fade)
                    return new SquareFade(width,height,depth,color);
                else
                    return new Square(width,height,depth,color);

            case circle:
                if(fade)
                    return new CircleFade(width,depth,color);
                else
                    return new Circle(width,depth,color);

            case ring:
                if(fade)
                    return new RingFade(width,depth,weight,color);
                else
                    return new Ring(width,depth,weight,color);
            default:
                return null;
        }
    }

    private float fade(float currentFrame, float maxFrame, float val){
        return (currentFrame/maxFrame)*val;
    }


    public void dispose(){
        if(shapes!=null)
            for(Shape s : shapes)
                s.dispose();
    }

    public interface Shape{
//        void draw(SpriteBatch batch, Vector2 at, int fromCenter);
        Texture img(int fromCenter);
        void dispose();
    }


    class Square implements Shape{
        Texture texture;
        int depth;

        Square(int width,int height, int depth, Color color){
            texture= DrawShape.rect(color,width,height);
            this.depth=depth;
        }

        @Override
        public Texture img(int fromCenter){
            if(fromCenter>depth)
                return null;
            return texture;
        }

        @Override
        public void dispose(){
            if(texture!=null)
                texture.dispose();
        }
    }
    class SquareFade implements Shape{
        Texture [] textures;

        SquareFade(int width,int height, int depth, Color color){
            textures=new Texture[depth+1];
            float alpha=color.a;
            for(int i=0; i<=depth; ++i){
                color.a=fade(depth-i,depth,alpha);
                textures[i]=DrawShape.rect(color,width,height);
            }
        }


        @Override
        public Texture img(int fromCenter){
            if(fromCenter>=textures.length)
                return null;
            return textures[fromCenter];
        }

        @Override
        public void dispose(){
            if(textures!=null)
                for(Texture t : textures)
                    t.dispose();
        }
    }

    class Circle implements Shape{
        Texture texture;
        int depth;

        Circle(int width, int depth, Color color){
            width=width/2;
            if(width<=0)
                width=1;
            texture= DrawShape.circle(color,width);
            this.depth=depth;
        }

        @Override
        public Texture img(int fromCenter){
            if(fromCenter>depth)
                return null;
            return texture;
        }

        @Override
        public void dispose(){
            if(texture!=null)
                texture.dispose();
        }
    }
    class CircleFade implements Shape{
        Texture [] textures;

        CircleFade(int width, int depth, Color color){
            textures=new Texture[depth+1];
            width=width/2;
            if(width<=0)
                width=1;

            float alpha=color.a;
            for(int i=0; i<=depth; ++i){
                color.a=fade(depth-i,depth,alpha);
                textures[i]=DrawShape.circle(color,width);
            }
        }


        @Override
        public Texture img(int fromCenter){
            if(fromCenter>=textures.length)
                return null;
            return textures[fromCenter];
        }

        @Override
        public void dispose(){
            if(textures!=null)
                for(Texture t : textures)
                    t.dispose();
        }
    }

    class Ring implements Shape{
        Texture texture;
        int depth;

        Ring(int width, int depth,int weight, Color color){
            width=width/2;
            if(width<=0)
                width=1;
            texture= DrawShape.ring(color,width,weight);
            this.depth=depth;
        }

        @Override
        public Texture img(int fromCenter){
            if(fromCenter>depth)
                return null;
            return texture;
        }

        @Override
        public void dispose(){
            if(texture!=null)
                texture.dispose();
        }
    }
    class RingFade implements Shape{
        Texture [] textures;

        RingFade(int width, int depth, int weight, Color color){
            textures=new Texture[depth+1];
            width=width/2;
            if(width<=0)
                width=1;

            float alpha=color.a;
            for(int i=0; i<=depth; ++i){
                color.a=fade(depth-i,depth,alpha);
                textures[i]=DrawShape.ring(color,width,weight);
            }
        }


        @Override
        public Texture img(int fromCenter){
            if(fromCenter>=textures.length)
                return null;
            return textures[fromCenter];
        }

        @Override
        public void dispose(){
            if(textures!=null)
                for(Texture t : textures)
                    t.dispose();
        }
    }
}
