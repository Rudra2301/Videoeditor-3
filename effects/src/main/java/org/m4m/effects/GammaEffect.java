package org.m4m.effects;

/**
 * Created by eNIX on 26-Aug-17.
 */

import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;


public class GammaEffect extends VideoEffect {
    private float gammaValue;

    public GammaEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        gammaValue = 2.0f;
        setFragmentShader(getFragmentShader());
    }

    protected String getFragmentShader() {
        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"

                + "varying vec2 vTextureCoord;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + "float gamma=" + gammaValue + ";\n"

                + "void main() {\n"

                + "vec4 textureColor = texture2D(sTexture, vTextureCoord);\n"
                + "gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);\n"

                + "}\n";

        return shader;
    }
}
