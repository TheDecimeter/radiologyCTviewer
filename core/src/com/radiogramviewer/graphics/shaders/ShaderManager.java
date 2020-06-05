package com.radiogramviewer.graphics.shaders;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.logging.ShaderLogger;
import com.radiogramviewer.logging.Timing;

import java.util.HashMap;

public class ShaderManager{
    private static final String off="off";

    public static ShaderLogger logger;

    private static HashMap<String,ShaderProgram> shaders;
    private static SpriteBatch batch;
    private static String last;
    private static ShaderProgram shader;

    public static void init(SpriteBatch batch){
        ShaderManager.batch=batch;
        if(logger==null) {
            last = off;
            shader = null;
            logger = new ShaderLogger();
        }
    }

    /**
     * Add a shader (this doesn't apply it unless it is replacing the currently
     * active shader.
     * Important: unlike other methods, this will not log the shader information so that
     * should be done by the method calling this one (because it will have more information
     * on how to log (ex, whether it's a gray window, custom shader, etc)
     * @param key the name of the shader
     * @param value the shader
     */
    public static void addShader(String key, ShaderProgram value){
        if(key.equals(off))
            return;
        if(shaders==null)
            shaders=new HashMap<String, ShaderProgram>();

        if(shaders.containsKey(key)) {
            shaders.get(key).dispose();
            shaders.remove(key);
        }
        shaders.put(key,value);
        if(last.equals(key))
            applyShader(value);
    }

    public static void removeShader(String key){
        if(shaders==null)
            return;
        if(!shaders.containsKey(key))
            return;

        logger.remove(key);
        shaders.get(key).dispose();
        shaders.remove(key);
        if(last.equals(key))
        {
            last=off;
            applyShader(null);
        }
    }
    public static void setShader(String key){
        if(key.equals(last))
            return;
        if(shaders==null)
            return;

        if(key.equals(off)){
            last=key;
            logger.invoke(key);
            applyShader(null);
            return;
        }
        if(shaders.containsKey(key)) {
            last=key;
            logger.invoke(key);
            applyShader(shaders.get(key));
        }
    }


    public static void dispose() {
        if(shaders!=null) {
            for (ShaderProgram p : shaders.values())
                p.dispose();
            shaders = null;
        }
        logger.reset();
    }

    private static void applyShader(ShaderProgram shader){
        ShaderManager.shader=shader;
    }

    public static void apply(SpriteBatch batch){
        batch.setShader(shader);
    }
    public static void remove(SpriteBatch batch){
        batch.setShader(null);
    }
}
