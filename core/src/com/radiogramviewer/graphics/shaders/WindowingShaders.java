package com.radiogramviewer.graphics.shaders;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.radiogramviewer.MainViewer;
import com.radiogramviewer.logging.ShaderLogger;
import com.radiogramviewer.relay.Constants;
import com.radiogramviewer.relay.P;

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
//            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
//            "#endif\n" +
            "varying vec4 v_color;\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture;\n" +
            "uniform mat4 u_projTrans1;\n"+
            "float lo = ";

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
            "  vec4 color = texture2D(u_texture, v_texCoords).rgba;\n" +
            "  vec3 HSV = rgb2hsv(color.rgb);\n" +
            "  vec3 finalRGB= hsv2rgb(vec3(HSV.xy,(HSV.z-lo)/(";


    private final static String fragmentShader3hsvFull=
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
                    "  vec4 color = texture2D(u_texture, v_texCoords).rgba;\n" +
                    "  vec3 HSV = rgb2hsv(color.rgb);\n" +
                    "  vec3 finalRGB= hsv2rgb(vec3(HSV.xy,(HSV.z * 2.048-lo)/(";

    private final static String fragmentShader3gray=
            ";\n" +
                    "void main()\n" +
                    "{\n" +
                    "  vec4 color = texture2D(u_texture, v_texCoords).rgba;\n" +
                    "  vec3 finalRGB = vec3(((color.r-lo)/(";

    private final static String fragmentShader3grayFull=                            //Note that this has a "bias" to offset
            ";\n" +                                                                 // its values so that they are equal to
                    "void main()\n" +                                               // a full window from -1000 HU to 1000 HU
                    "{\n" +                                                         // This is why the pixel color is multiplied
                    "  vec4 color = texture2D(u_texture, v_texCoords).rgba;\n" +    // by 2.0475 (4095/2000). This will allow
                    "  vec3 finalRGB = vec3(((color.r*2.0475-lo)/(";                // the value at 124.54... (1000HU) to become
                                                                                    // 255 (white) when L is .5 and W is 1

    private final static String fragmentShader3gray16=
            ";\n" +
                    "float cbn(vec2 c)\n" +                 //This contains a "bias" to offset its value to appear like
                    "{\n" +                                 // HU of 1000 is white and -1000 is black. So to mimmic that
                    "    return (c.x*256.0+c.y)*0.1275;\n" + // scale the output: multiply by 0.1275 (255/2000).
                    "}\n\n" +
                    "void main()\n" +
                    "{\n" +
                    "  vec4 color = texture2D(u_texture, v_texCoords).rgba;\n" +
                    "  vec3 finalRGB = vec3(((cbn(color.rg)-lo)/(";


    private final static String fragmentShader4=
            ")));\n"+

            "  gl_FragColor = vec4(finalRGB, color.a);\n"+
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

    public static ShaderProgram windowGray16(float level, float width){
        return window(level,width,fragmentShader3gray16);
    }
    public static ShaderProgram windowGrayFull(float level, float width){
        return window(level,width,fragmentShader3grayFull);
    }
    public static ShaderProgram windowValueFull(float level, float width){
        return window(level,width,fragmentShader3hsvFull);
    }

    private static ShaderProgram window(float level, float width, String guts){
//        width/=2;
//        float hi=level+width;
        float lo=level-width/2;
//        float range=hi-lo;
        String fragment=fragmentShader1+f(lo)+guts+f(width)+fragmentShader4;
        return generateShader(fragment);
    }

    private static String f(float v){
        String r=""+v;
        if(r.contains("."))
            return r;
        else
            return r+".0";
    }


    /**
     * Create a custom shader and feed it to the viewer as a set of strings.
     * @param vertex the vertex shader program
     * @param fragment the fragment shader program
     * @return a new shader, or null if if the strings don't form a proper shader
     *  (look in the console for debug info)
     */
    public static ShaderProgram generateShader (String vertex, String fragment){
        if(vertex.length()<8) {
            String s=vertex.toLowerCase();
            if (s.equals("default")||s.equals("d"))
                return generateShader(fragment);
        }
        ShaderProgram r = new ShaderProgram(vertex,fragment);

        if(r.isCompiled())
            return r;
        P.e("Failed to generate shader \nVertex:\n"+vertex+"\n\nFragment:\n"+fragment+"\nLog:\n"+r.getLog());
        return null;
    }

    private static ShaderProgram generateShader (String fragment){
        ShaderProgram r = new ShaderProgram(vertexShader,fragment);

        if(r.isCompiled()) {
//            P.d("shader created "+r.getLog());
            return r;
        }
        P.e("Failed to generate shader with default Vertex and \nFragment:\n"+fragment+"\nLog:\n"+r.getLog());
        return null;
    }


}
