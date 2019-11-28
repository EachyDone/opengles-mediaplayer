package com.learn.opengles;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class CubeSurfaceView extends GLSurfaceView {

    private static final String TAG = "CubeSurfaceView";

    private CubeRenderer mCubeRenderer;
    private float mPreviousX;
    private float mPreviousY;
    private float touchConvert;

    public CubeSurfaceView(Context context) {
        this(context, null);
    }

    public CubeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT < 17) {
            display.getSize(point);
        } else {
            display.getRealSize(point);
        }
        touchConvert = (float) point.x / point.y;
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        mCubeRenderer = new CubeRenderer(context);
        setRenderer(mCubeRenderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = x - mPreviousX;
            float dy = y - mPreviousY;
            Log.d(TAG, "onTouchEvent, dx: " + dx + ", dy: " + dy);
            // 防止手指按下是的颤抖
            if (Math.abs(dx) < 4 && Math.abs(dy) < 4) {
                return true;
            }
            // 设置水平滑动时绕Y轴旋转的左右方向
            if (Math.abs(dx) > 4) {
                if (dx > 0) {
                    mCubeRenderer.center.y = 1;
                } else if (dx < 0) {
                    mCubeRenderer.center.y = -1;
                }
            } else {
                mCubeRenderer.center.y = 0;
            }
            // 设置垂直滑动时绕X轴旋转的上下方向
            if (Math.abs(dy) > 4) {
                if (dy > 0) {
                    mCubeRenderer.center.x = 1;
                } else if (dy < 0) {
                    mCubeRenderer.center.x = -1;
                }
            } else {
                mCubeRenderer.center.x = 0;
            }
            // 设置旋转角度
            mCubeRenderer.center.angle += (float) (Math.sqrt(Math.abs(dx) * Math.abs(dx) + Math.abs(dy) * Math.abs(dy))) * touchConvert;
            requestRender();//重绘画面
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    public void onPause() {
        mCubeRenderer.pauseMediaPlayer();
        super.onPause();
    }

    @Override
    public void onResume() {
        mCubeRenderer.startMediaPlayer();
        super.onResume();
    }

    /**
     * 退出时重置MediaPlayer
     */
    public void cleanMediaPlayer() {
        if (mCubeRenderer != null) {
            mCubeRenderer.cleanMediaPlayer();
        }
    }

    /**
     * 设置滤镜效果
     * @param filter NONE:原始，GRAY:黑白，ANTI:反色，DUIBI:增加对比度，LIGHTER:变亮，DARKEN:变暗，VAGUE:模糊
     */
    public void setFilterIndex(Cube.Filter filter) {
        mCubeRenderer.setFilterIndex(filter);
    }
}
