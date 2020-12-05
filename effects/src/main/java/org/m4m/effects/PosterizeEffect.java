package org.m4m.effects;

/**
 * Created by eNIX on 26-Aug-17.
 */

import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;

public class PosterizeEffect extends VideoEffect {

       public PosterizeEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        setFragmentShader(getFragmentShader());
    }

    protected String getFragmentShader() {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "varying vec2 vTextureCoord;\n" + "void main() {\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  vec3 pcolor;\n"
                + "  pcolor.r = (color.r >= 0.5) ? 0.75 : 0.25;\n"
                + "  pcolor.g = (color.g >= 0.5) ? 0.75 : 0.25;\n"
                + "  pcolor.b = (color.b >= 0.5) ? 0.75 : 0.25;\n"
                + "  gl_FragColor = vec4(pcolor, color.a);\n" + "}\n";
        return shader;
    }
}
