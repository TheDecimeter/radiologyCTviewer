package com.radiogramviewer.graphics;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.radiogramviewer.config.Config;

/**
 * Simple bar drawer to draw our scroll bar
 * Note that the bar was carried over from a horizontal oriented bar, so
 * width and height are swapped.
 */
public class Bar{

    private Texture bar, outline;

    private int x,y,width, maxHeight, height, border;



    public Bar(Config c){
        this.x=c.window.width;
        this.width=c.window.barWidth;
        border=c.window.barBorder;

        this.height=c.window.height-border*2;
        maxHeight =height;
        this.y=0;
        this.x-=(this.width+border*2);

        bar= DrawShape.rect(c.window.barColor,this.width,height);
        outline=DrawShape.rect(c.window.borderColor,this.width+border*2,c.window.height);
    }

    public boolean draw(SpriteBatch batch) {
        batch.draw(outline, x, y);
        batch.draw(bar, x+border, y+border, width, height);
        return true;
    }

    public boolean setHeight(float current, float full){
        height=(int)((current/full)* maxHeight);
        if(height<0) {
            height = 0;
            return false;
        }
        else if(height> maxHeight) {
            height = maxHeight;
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
