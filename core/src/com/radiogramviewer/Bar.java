package com.radiogramviewer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Simple bar drawer to draw our scroll bar
 * Note that the bar was carried over from a horizontal oriented bar, so
 * width and height are swapped.
 */
public class Bar{

    private Texture bar, outline;

    private int x,y,width, maxWidth, height, border;



    public Bar(Color color,Config c, int width){


        this.x=c.window.width;
        this.y=c.window.height;
        this.width=(int)(width*(1/c.global.scale));
        if(this.width<1)
            this.width=1;
        border=(int)(this.width*.2f);
        if(border<1)
            border=1;

        this.height=y-border*2;
        maxWidth=height;
        this.y-=(height);
        this.x-=(this.width+border*2);

        color.a=.7f;
        bar= DrawShape.rect(color,this.width,height);
        outline=DrawShape.rect(new Color(0,0,0,.5f),this.width+border*2,c.window.height);
    }

    public boolean draw(SpriteBatch batch) {
        batch.draw(outline, x, y);
        batch.draw(bar, x+border, y-border, width, height);
        return true;
    }

    public boolean setHeight(float current, float full){
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
