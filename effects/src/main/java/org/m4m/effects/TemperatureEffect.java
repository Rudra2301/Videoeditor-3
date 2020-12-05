/*
 * Copyright 2014-2016 Media for Mobile
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.m4m.effects;

import org.m4m.android.graphics.VideoEffect;
import org.m4m.domain.graphics.IEglUtil;

public class TemperatureEffect extends VideoEffect {
    private float scale;

    public TemperatureEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
        scale = 1.0f;
        setFragmentShader(getFragmentShader());
    }

    protected String getFragmentShader() {
        String scaleString = "scale = " + (2.0f * scale - 1.0f) + ";\n";

        String shader = "#extension GL_OES_EGL_image_external : require\n"
                + "precision mediump float;\n"
                + "uniform samplerExternalOES sTexture;\n"
                + " float scale;\n"
                + "varying vec2 vTextureCoord;\n"
                + "void main() {\n" // Parameters that were created above
                + scaleString
                + "  vec4 color = texture2D(sTexture, vTextureCoord);\n"
                + "  vec3 new_color = color.rgb;\n"
                + "  new_color.r = color.r + color.r * ( 1.0 - color.r) * scale;\n"
                + "  new_color.b = color.b - color.b * ( 1.0 - color.b) * scale;\n"
                + "  if (scale > 0.0) { \n"
                + "    new_color.g = color.g + color.g * ( 1.0 - color.g) * scale * 0.25;\n"
                + "  }\n"
                + "  float max_value = max(new_color.r, max(new_color.g, new_color.b));\n"
                + "  if (max_value > 1.0) { \n"
                + "     new_color /= max_value;\n" + "  } \n"
                + "  gl_FragColor = vec4(new_color, color.a);\n" + "}\n";

        return shader;
    }


}
