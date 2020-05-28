package com.radiogramviewer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class WindowingShaders {
    private final static String vertexShader = "attribute vec4 a_position;\n" +
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "uniform mat4 u_projTrans;\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main()\n" +
            "{\n" +
            "   v_color = vec4(1, 1, 1, 1);\n" +
            "   v_texCoords = a_texCoord0;\n" +
            "   gl_Position =  u_projTrans * a_position;\n"      +
            "}\n" ;
    private final static String fragmentShader1 =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform mat4 u_projTrans1;\n"+
            "float lo = ";

    private final static String fragmentShader2=
            ";\nfloat hi = ";

    private final static String fragmentShader3hsv=
            ";\n" +
            "vec3 rgb2hsv(vec3 c)\n" +
            "{\n" +
            "    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
            "    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
            "    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
            "\n" +
            "    float d = q.x - min(q.w, q.y);\n" +
            "    float e = 1.0e-10;\n" +
            "    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
            "}\n\n" +
            "vec3 hsv2rgb(vec3 c)\n" +
            "{\n" +
            "    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
            "    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
            "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
            "}\n\n" +
            "void main()\n" +
            "{\n" +
            "  vec3 color = rgb2hsv(texture2D(u_texture, v_texCoords).rgb);\n" +
            "  vec3 finalRGB= hsv2rgb(vec3(color.xy,(color.z-lo)/(";

    private final static String fragmentShader3gray=
            ";\n" +
            "void main()\n" +
            "{\n" +
            "  vec3 color = texture2D(u_texture, v_texCoords).rgb;\n" +
            "  vec3 finalRGB = vec3(((color.r-lo)/(";

    private final static String fragmentShader4=
            ")));\n"+

            "  gl_FragColor = vec4(finalRGB, 1.0);\n"+
            "}";

    /**
     * Create a windowing shader for a grayscale image
     * Note, the image must be grayscale, the shader will only
     *  work with a single color component as if all 3 are equal.
     * @param level the windowing level (between 0 and 1)
     * @param width the window width (between 0 and 1)
     * @return a new shader, or null if it failed to compile (which shouldn't happen)
     */
    public static ShaderProgram windowGray(float level, float width){
        return window(level,width,fragmentShader3gray);
    }
    /**
     * Create a windowing shader for an image for a colored image. This shader only
     * windows the value of the image and leaves its hue and saturation untouched.
     * If you know the image is grayscale use <code>windowGray</code> instead, it's
     * faster (and these two shaders have the same effect for grayscale images).
     * @param level the windowing level (between 0 and 1)
     * @param width the window width (between 0 and 1)
     * @return a new shader, or null if it failed to compile (which shouldn't happen)
     */
    public static ShaderProgram windowValue(float level, float width){
        return window(level,width,fragmentShader3hsv);
    }

    private static ShaderProgram window(float level, float width, String guts){
        width/=2;
        float hi=level+width;
        float lo=level-width;
        float rg=hi-lo;
        String fragment=fragmentShader1+lo+fragmentShader2+hi+guts+rg+fragmentShader4;
        return generateShader(fragment);
    }


    /**
     * Create a custom shader and feed it to the viewer as a set of strings.
     * @param vertex the vertex shader program
     * @param fragment the fragment shader program
     * @return a new shader, or null if if the strings don't form a proper shader
     *  (look in the console for debug info)
     */
    public static ShaderProgram generateShader (String vertex, String fragment){
        ShaderProgram r = new ShaderProgram(vertex,fragment);

        if(r.isCompiled())
            return r;
        MainViewer.println("Failed to generate shader \nVertex:\n"+vertex+"\n\nFragment:\n"+fragment+"\nLog:\n"+r.getLog(),Constants.e);
        return null;
    }

    private static ShaderProgram generateShader (String fragment){
        ShaderProgram r = new ShaderProgram(vertexShader,fragment);

        if(r.isCompiled())
            return r;
        MainViewer.println("Failed to generate shader \nFragment:\n"+fragment+"\nLog:\n"+r.getLog(),Constants.e);
        return null;
    }


}
