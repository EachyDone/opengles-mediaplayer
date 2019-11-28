package com.learn.opengles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CubeRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CubeRenderer";

    private Cube mCube;
    Center center;

    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];

    CubeRenderer(Context context) {
        mCube = new Cube(context);
        center = new Center();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST | GLES20.GL_DEPTH_BUFFER_BIT);
        mCube.onSurfaceCreated();
        setFilterIndex(Cube.Filter.NONE);
        mCube.onDrawCreatedSet();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged, width: " + width + "height: " + height);
        GLES20.glViewport(0, 0, width, height);
        // 计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 2.0f, 8.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame, center: " + center.x + "," + center.y + "," + center.angle);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float[] scratch = new float[16];
        if (center.angle != 0 && (center.x != 0 || center.y != 0)) {
            // 设置旋转矩阵
            Matrix.setRotateM(mRotationMatrix, 0, center.angle, center.x, center.y, 0.0f);
            // 合入旋转矩阵
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
            mCube.draw(scratch);
        } else {
            mCube.draw(mMVPMatrix);
        }
        mCube.onDrawSet();
    }

    void pauseMediaPlayer() {
        Log.d("ysh", "pauseMediaPlayer() ");
        mCube.pauseMediaPlayer();
    }

    void startMediaPlayer() {
        Log.d("ysh", "startMediaPlayer() ");
        mCube.startMediaPlayer();
    }

    void cleanMediaPlayer() {
        mCube.cleanMediaPlayer();
    }

    void setFilterIndex(Cube.Filter filter) {
        mCube.setFilterIndex(filter);
    }

    class Center {
        float x = 0f;
        float y = 1.0f;
        float angle = 0f;
    }
}
