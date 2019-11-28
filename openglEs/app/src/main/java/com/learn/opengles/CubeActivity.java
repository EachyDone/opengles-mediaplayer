package com.learn.opengles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class CubeActivity extends AppCompatActivity {

    private CubeSurfaceView mCubeSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube);
        init();
    }

    private void init() {
        mCubeSurfaceView = findViewById(R.id.cube_surface_view);
    }

    @Override
    protected void onResume() {
        mCubeSurfaceView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCubeSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mCubeSurfaceView.cleanMediaPlayer();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mDefault:// 原图
                mCubeSurfaceView.setFilterIndex(Cube.Filter.NONE);
                break;
            case R.id.mGray:// 黑白
                mCubeSurfaceView.setFilterIndex(Cube.Filter.GRAY);
                break;
            case R.id.anti_color:// 反色
                mCubeSurfaceView.setFilterIndex(Cube.Filter.ANTI);
                break;
            case R.id.add_contrast_ratio:// 增加对比度
                mCubeSurfaceView.setFilterIndex(Cube.Filter.DUIBI);
                break;
            case R.id.brighten:// 变亮
                mCubeSurfaceView.setFilterIndex(Cube.Filter.LIGHTER);
                break;
            case R.id.darken:// 变黑
                mCubeSurfaceView.setFilterIndex(Cube.Filter.DARKEN);
                break;
            case R.id.vague:// 模糊
                mCubeSurfaceView.setFilterIndex(Cube.Filter.VAGUE);
                break;
        }
        mCubeSurfaceView.requestRender();
        return super.onOptionsItemSelected(item);
    }
}
