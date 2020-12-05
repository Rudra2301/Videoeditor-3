package org.m4m.effects;

import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;

/**
 * Created by eNIX on 26-Aug-17.
 */

public class VignetteEffect  extends VideoEffect  {

    public VignetteEffect(int angle, IEglUtil eglUtil) {

        super(angle, eglUtil);
        setFragmentShader(getShader());
    }

    private String getShader() {

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " float range;\n"
                + " float inv_max_dist;\n"
                + " float shade;\n"
                + " vec2 scale;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n"
                // Parameters that were created above
                + "  const float slope = 20.0;\n"
                + "  vec2 coord = vTextureCoord - vec2(0.5, 0.5);\n"
                + "  float dist = length(coord * scale);\n"
                + "  float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);\n"
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  gl_FragColor = vec4(color.rgb * lumen, color.a);\n"
                + "}\n";

        return shader;
    }


}
