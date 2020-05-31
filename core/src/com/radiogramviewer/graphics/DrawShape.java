package com.radiogramviewer.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

/**
 * Create textures of simple shapes
 */
public class DrawShape {

    public static Texture rect(Color color, int width, int height){
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(0, 0, width, height);
        Texture r = new Texture(pixmap);
        pixmap.dispose();
        return r;
    }

    public static Texture circle(Color color, int radius){
        if(radius<1)
            radius=1;
        int d=radius*2;
        Pixmap pixmap = new Pixmap(d, d, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);

        drawCircle(pixmap);

        Texture r = new Texture(pixmap);
        pixmap.dispose();
        return r;
    }

    public static Texture ring(Color color, int radius, int thickness){
        if(radius<1)
            radius=1;
        if(thickness<1)
            thickness=1;

        if(radius<=thickness)
            return circle(color,radius);

        int d=radius*2;
        Pixmap pixmap = new Pixmap(d, d, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);

        drawRing(pixmap,thickness);

        Texture r = new Texture(pixmap);
        pixmap.dispose();
        return r;
    }

    private static void drawCircle(Pixmap pixmap)
    {
        int radius=pixmap.getHeight()/2;
        for(int y=0; y<pixmap.getHeight(); ++y){
            for(int x=0; x<pixmap.getHeight(); ++x){
                if(Vector2.dst(radius,radius,x,y)<radius)
                    pixmap.drawPixel(x,y);
            }
        }
    }
    private static void drawRing(Pixmap pixmap, int thickness)
    {
        int radius=pixmap.getHeight()/2;
        int hollow=radius-thickness;
        for(int y=0; y<pixmap.getHeight(); ++y){
            for(int x=0; x<pixmap.getHeight(); ++x){
                int dist=(int)Vector2.dst(radius,radius,x,y);
                if(dist<radius && dist>=hollow)
                        pixmap.drawPixel(x,y);
            }
        }
    }
}
