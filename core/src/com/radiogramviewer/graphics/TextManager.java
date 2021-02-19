package com.radiogramviewer.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.radiogramviewer.config.Config;
import com.radiogramviewer.relay.P;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Store and draw text in different fonts.
 *
 * IMPORTANT: This shows text based on the dimensions from the config file, not
 * real dimensions. This is because text displays are set manually, not through
 * clicks (which use the real dimensions).
 */
public class TextManager {
    private TreeMap<String,TextNode> text;
    private HashMap<String, FontNode> fonts;
    private Config c;

    public TextManager(Config c){
        this.c=c;
        text= new TreeMap<String,TextNode>();
        fonts = new HashMap<String,FontNode>();
    }

    public TextManager(Config c, TextManager old){
        this.c=c;
        this.text=old.text;
        this.fonts=old.fonts;
        resetDimensions();
    }

    public void draw(SpriteBatch batch){
        for(TextNode node : text.values())
            node.draw(batch);
    }

    public boolean addFont(String fontName, int fontSizePx, String fontColor, float borderWidth, String borderColor, String characters){
        if(fonts.containsKey(fontName)) {
            fonts.get(fontName).set(fontSizePx, borderWidth, fontColor, borderColor, characters);
            return true;
        }

        FontNode f = new FontNode(fontSizePx, borderWidth, fontColor, borderColor, characters);

        if(f.font==null)
            return false;

        fonts.put(fontName, f);
        return true;
    }


    public boolean addText(String textName, String fontName, String msg, int x, int y, boolean leftAlign){
        if(!fonts.containsKey(fontName)){
            P.e("Trying to create text, but don't have font named "+fontName);
            return false;
        }
        if(text.containsKey(textName))
            text.remove(textName);

        text.put(textName, new TextNode(fonts.get(fontName), msg, x, y, leftAlign));
        return true;
    }

    public boolean removeText(String textName){
        if(textName==null){ //remove all text if none is specified
            text=new TreeMap<String,TextNode>();
            return true;
        }

        if(text.containsKey(textName)){
            text.remove(textName);
            return true;
        }
        P.e("Can not remove text, no text called "+textName);
        return false;
    }


    private void resetDimensions(){
        for(TextNode n : text.values())
            n.reset();
        for(FontNode n : fonts.values())
            n.reset();
    }

    public void dispose(){
        for(FontNode font : fonts.values())
            font.dispose();
    }




    private class TextNode{
        Vector2 screenPos, unitPos;
        int align;
        String text;
        FontNode font;

        TextNode(FontNode font, String text, float x, float y, boolean leftAlign){
            this.font=font;
            this.text=text;
            this.unitPos=new Vector2(x/c.window.dimensionWidth, y/c.window.dimensionHeight);
            if(leftAlign)
                this.align= Align.left;
            else
                this.align=Align.right;
            reset();
        }

        void draw(SpriteBatch batch){
            font.font.draw(batch,text,screenPos.x, screenPos.y, 0, align, false);
        }

        void reset(){
            screenPos=new Vector2(unitPos.x*c.window.width, unitPos.y*c.window.height);
        }
    }

    private class FontNode{
        BitmapFont font;
        Color textColor, borderColor;
        String characters;
        float unitBorderWidth;
        int unitHeight;

        FontNode(int pixelHeight, float borderWidth, String textColor, String borderColor, String characters){
            set(pixelHeight,borderWidth,textColor,borderColor,characters);
        }

        void set(int pixelHeight, float borderWidth, String textColor, String borderColor, String characters){
            if(borderWidth > 0){
                this.unitBorderWidth=borderWidth;
                this.borderColor=Config.parseColor(borderColor);
            }
            this.unitHeight=pixelHeight;
            this.textColor=Config.parseColor(textColor);
            this.characters=characters;
            reset();
        }

        void reset(){
            if(font!=null)
                font.dispose();
            float borderWidth=unitBorderWidth*c.global.scale;
            int pixelHeight=(int)Math.ceil(unitHeight*c.global.scale);

            try{
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                if(characters!=null)
                    parameter.characters=characters;
                parameter.size = pixelHeight;
                parameter.color = textColor;
                if(borderWidth > 0){
                    parameter.borderColor = borderColor;
                    parameter.borderWidth = borderWidth;
                }
                font=generator.generateFont(parameter);
                generator.dispose();
            }
            catch(Exception e){
                P.e("Trying to create font, but no font asset could load.");
            }
        }

        void dispose(){
            font.dispose();
        }
    }
}
