package com.learn.opengles;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class ShaderHelper {

    private static final String TAG = "com.learn.opengles.ShaderHelper";

    /**
     * 编译vertex shader
     * @param shaderCode shader代码
     * @return shader id
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译fragment shader
     * @param shaderCode shader代码
     * @return shader id
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译shader
     * @param type shader类型（vertex:GL_VERTEX_SHADER、fragment:GL_FRAGMENT_SHADER）
     * @param shaderCode shader代码
     * @return shader id
     */
    private static int compileShader(int type, String shaderCode) {
        // 创建shader对象
        final int shaderObjectId = glCreateShader(type);
        if(shaderObjectId == 0){
            Log.e(TAG, "could not create new shader. ");
            return 0;
        }
        // 上传代码到shader对象
        glShaderSource(shaderObjectId, shaderCode);
        // 编译shader
        glCompileShader(shaderObjectId);
        // 获取编译状态
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e(TAG, "compilation of shader failed:" + "\n" + shaderCode + "\n:"
                    + glGetShaderInfoLog(shaderObjectId));
            glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }

    /**
     * 链接shader
     * @param vertexShaderId 顶点shader
     * @param fragmentShaderId 片段shader
     * @return program id
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 创建program对象
        final int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            Log.e(TAG, "could not create new program.");
            return 0;
        }
        // 将shader附加到program
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);
        // 链接program
        glLinkProgram(programObjectId);
        // 获取链接状态
        int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "linking of program failed:\n" + glGetProgramInfoLog(programObjectId));
            glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    /**
     * 验证program
     * @param programObjectId program id
     * @return true:成功，false:失败
     */
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.e(TAG, "result of validating program: " + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(programObjectId));
        return validateStatus[0] != 0;
    }
}
