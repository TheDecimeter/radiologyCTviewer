package com.radiogramviewer.graphics.shaders;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;

public class ShaderManager implements Disposable {
    private static HashMap<String,ShaderProgram> shaders;
    private static SpriteBatch batch;

    public ShaderManager(SpriteBatch batch){
        this.batch=batch;
    }

    public static void addShader(String key, ShaderProgram value){
        if(key.equals("off"))
            return;
        if(shaders==null)
            shaders=new HashMap<String, ShaderProgram>();

        boolean applyShader=false;
        if(shaders.containsKey(key)) { //if there is already a shader by that name, replace it
            if(batch.getShader().equals(shaders.get(key)))
                applyShader=true;  //if it is the active shader, repace it with the new one
            removeShader(key);
        }
        shaders.put(key,value);
        if(applyShader)
            batch.setShader(value);
    }
    public static void removeShader(String key){
        if(shaders==null)
            return;
        if(!shaders.containsKey(key))
            return;
        ShaderProgram s=shaders.get(key);
        if(batch.getShader().equals(s))
            batch.setShader(null);
        s.dispose();
        shaders.remove(key);
    }
    public static void setShader(String key){
        if(key.equals("off")){
            batch.setShader(null);
            return;
        }
        if(shaders==null)
            return;
        if(shaders.containsKey(key))
            batch.setShader(shaders.get(key));
    }

    @Override
    public void dispose() {
        if(shaders!=null) {
            for (ShaderProgram p : shaders.values())
                p.dispose();
            shaders = null;
        }
    }
}
