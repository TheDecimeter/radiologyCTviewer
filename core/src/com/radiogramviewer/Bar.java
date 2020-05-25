package com.radiogramviewer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Simple bar drawer to draw our scroll bar
 * Note that the bar was carried over from a horizontal oriented bar, so
 * width and height are reversed.
 */
public class Bar{

    private Texture bar, outline;

    private boolean vertical=false;

    private int x,y,width, maxWidth, height, border;



    public Bar(int x, int y, Color color, int height){

        this.vertical=true;

        this.x=x;
        width=10;
        border=5;
        this.y=y;

        this.height=height;
        maxWidth=height;
        this.y-=height;

        color.a=.7f;
        bar= DrawShape.rect(color,width,height);
        outline=DrawShape.rect(new Color(0,0,0,.5f),width+border*2,height+border*2);
    }

    public boolean draw(SpriteBatch batch) {
        batch.draw(outline, x, y);
        batch.draw(bar, x+border, y+border, width, height);
        return true;
    }

    public boolean setWidth(float current, float full){
        if(vertical){
            return setHeight(current,full);
        }
        else {
            width = (int) ((current / full) * maxWidth);
            if (width < 0) {
                width = 0;
                return false;
            }
            else if (width > maxWidth) {
                width = maxWidth;
            }
            return true;
        }
    }

    private boolean setHeight(float current, float full){
        height=(int)((current/full)*maxWidth);
        if(height<0) {
            height = 0;
            return false;
        }
        else if(height>maxWidth) {
            height = maxWidth;
        }
        return true;
    }


    public void dispose() {
        bar.dispose();
        outline.dispose();
    }

    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            dispose();
        }
        finally
        {
            super.finalize();
        }
    }
}
