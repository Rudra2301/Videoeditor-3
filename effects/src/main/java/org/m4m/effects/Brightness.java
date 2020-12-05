package org.m4m.effects;

/**
 * Created by eNIX on 26-Aug-17.
 */

import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;



public class Brightness extends VideoEffect {
    private float brightnessValue;


    public Brightness(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        setFragmentShader(getFragmentShader());
        brightnessValue = 2.0f;
    }

    protected String getFragmentShader() {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "float brightness ;\n" + "varying vec2 vTextureCoord;\n"
                + "void main() {\n" + "  brightness =" + brightnessValue
                + ";\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  gl_FragColor = brightness * color;\n" + "}\n";

        return shader;
    }
}

