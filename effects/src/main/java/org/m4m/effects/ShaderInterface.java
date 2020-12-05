package org.m4m.effects;

/**
 * Created by eNIX on 26-Aug-17.
 */

public interface ShaderInterface {
    /**
     * Returns Shader code
     *
     *
     *            send this for every shader but this will only be used when the
     *            shader needs it.
     * @return complete shader code in C
     */
    public String getShader();

}