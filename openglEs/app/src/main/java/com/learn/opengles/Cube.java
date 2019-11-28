package com.learn.opengles;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube implements MediaPlayer.OnVideoSizeChangedListener, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "Cube";
    private static final int COORDS_PER_VERTEX = 3;// 每个顶点为3个数据
    private static final int COUNT_PER_COLOR = 4;// 每个顶点颜色为4个数据
    private static final int BYTES_OF_FLOAT = 4;// 一个float 4个字节
    private static final int BYTES_OF_SHORT = 4;// 一个short 4个字节

    private MediaPlayer mMediaPlayer;
    private boolean mUpdateSurface;

    private FloatBuffer mCubeVertexBuffer;// 顶点Buffer
    private FloatBuffer mCubeColorBuffer;// 颜色Buffer
    private ShortBuffer mVertexIndexBuffer;// 顶点索引Buffer（除了正面）
    private ShortBuffer mVertexFrontIndexBuffer;// 顶点正面索引Buffer
    private FloatBuffer mTextureVertexBuffer;// 纹理顶点Buffer

    private int mCubeProgramId;

    private int hChangeType;
    private int hChangeColor;
    private Filter filter;

    // 顶点坐标
    private final float mCubeVertex[] = {
            -1.0f, 1.0f, 1.0f,    //正面左上0
            -1.0f, -1.0f, 1.0f,   //正面左下1
            1.0f, -1.0f, 1.0f,    //正面右下2
            1.0f, 1.0f, 1.0f,     //正面右上3
            -1.0f, 1.0f, -1.0f,    //反面左上4
            -1.0f, -1.0f, -1.0f,   //反面左下5
            1.0f, -1.0f, -1.0f,    //反面右下6
            1.0f, 1.0f, -1.0f,     //反面右上7
    };
    // 顶点颜色
    private final float mCubeColor[] = {
            1f, 1f, 1f, 1f,
            1f, 1f, 1f, 1f,
            1f, 1f, 1f, 1f,
            1f, 1f, 1f, 1f,
            0.7f, 0.7f, 0.7f, 1f,
            0.7f, 0.7f, 0.7f, 1f,
            0.7f, 0.7f, 0.7f, 1f,
            0.7f, 0.7f, 0.7f, 1f,
    };
    // 顶点索引
    final short mVertexIndex[]={
            6,7,4,6,4,5,    //后面
            6,3,7,6,2,3,    //右面
            6,5,1,6,1,2,    //下面
            //0,3,2,0,2,1,    //正面
            0,1,5,0,5,4,    //左面
            0,7,3,0,4,7,    //上面
    };
    // 正面顶点索引
    private short mVertexFrontIndex[] = {0, 3, 2, 0, 2, 1};

    // 纹理坐标
    private final float[] mTextureVertex = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private SurfaceTexture mSurfaceTexture;
    private float[] uSTMatrix = new float[16];
    private int mTextureId;
    private Context mContext;

    Cube(Context context) {
        mContext = context;
        synchronized (this) {
            mUpdateSurface = false;
        }
        // 创建并设置MediaPlayer资源，循环播放
        mMediaPlayer = MediaPlayer.create(context, R.raw.test);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        // 立方体顶点Buffer复制
        mCubeVertexBuffer = ByteBuffer.allocateDirect(mCubeVertex.length * BYTES_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mCubeVertex);
        mCubeVertexBuffer.position(0);
        // 立方体顶点颜色Buffer复制
        mCubeColorBuffer = ByteBuffer.allocateDirect(mCubeColor.length * BYTES_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mCubeColor);
        mCubeColorBuffer.position(0);
        // 顶点索引Buffer复制（正面除外）
        mVertexIndexBuffer = ByteBuffer.allocateDirect(mVertexIndex.length * BYTES_OF_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(mVertexIndex);
        mVertexIndexBuffer.position(0);
        // 正面顶点索引Buffer复制
        mVertexFrontIndexBuffer = ByteBuffer.allocateDirect(mVertexFrontIndex.length * BYTES_OF_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(mVertexFrontIndex);
        mVertexFrontIndexBuffer.position(0);
        // 纹理顶点Buffer复制
        mTextureVertexBuffer = ByteBuffer.allocateDirect(mTextureVertex.length * BYTES_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mTextureVertex);
        mTextureVertexBuffer.position(0);
    }


    void onSurfaceCreated() {
        // 加载raw中glsl文件，并编译、链接获得program id
        String cubeVertexShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.cube_vertex_shader);
        String cubeFragmentShaderSource = TextResourceReader.readTextFileFromResource(mContext, R.raw.cube_fragment_shader);
        int cubeVertexShaderId = ShaderHelper.compileVertexShader(cubeVertexShaderSource);
        int cubeFragmentShaderId = ShaderHelper.compileFragmentShader(cubeFragmentShaderSource);
        mCubeProgramId = ShaderHelper.linkProgram(cubeVertexShaderId, cubeFragmentShaderId);

        // 生成纹理
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureId = textures[0];
        // 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);// 监听是否有新的一帧数据到来
        Surface surface = new Surface(mSurfaceTexture);
        mMediaPlayer.setSurface(surface);
        surface.release();
    }

    void draw(float[] mvpMatrix) {
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mCubeProgramId);

        // 获取变换矩阵vMatrix成员句柄
        int vMatrixHandler = GLES20.glGetUniformLocation(mCubeProgramId, "vMatrix");
        // 指定vMatrix的值
        GLES20.glUniformMatrix4fv(vMatrixHandler, 1, false, mvpMatrix, 0);

        // 获取顶点着色器的vPosition成员句柄
        int vPositionHandler = GLES20.glGetAttribLocation(mCubeProgramId, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(vPositionHandler);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(vPositionHandler, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mCubeVertexBuffer);
        // 设置vVideo为0，表示绘制的立方体面不是视频，为1是视频
        int vVideoHandler = GLES20.glGetUniformLocation(mCubeProgramId, "vVideo");
        GLES20.glUniform1i(vVideoHandler, 0);

        // 获取片元着色器的vColor成员的句柄
        int aColorHandler = GLES20.glGetAttribLocation(mCubeProgramId, "aColor");
        // 设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(aColorHandler);
        GLES20.glVertexAttribPointer(aColorHandler, COUNT_PER_COLOR, GLES20.GL_FLOAT, false, 0, mCubeColorBuffer);

        // 索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertexIndex.length, GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
        GLES20.glDisableVertexAttribArray(aColorHandler);

        // 开始渲染立方体正面
        synchronized (this) {
            if (mUpdateSurface) {
                mSurfaceTexture.updateTexImage();// 获取新数据
                mSurfaceTexture.getTransformMatrix(uSTMatrix);// 让新的纹理和纹理坐标系能够正确的对应,mSTMatrix的定义是和projectionMatrix完全一样的。
                mUpdateSurface = false;
            }
        }

        int uSTMMatrixHandler = GLES20.glGetUniformLocation(mCubeProgramId, "uSTMatrix");
        int sTextureHandler = GLES20.glGetUniformLocation(mCubeProgramId, "sTexture");
        int aTextureCoordHandler = GLES20.glGetAttribLocation(mCubeProgramId, "aTexCoord");

        GLES20.glUniformMatrix4fv(vMatrixHandler, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandler, 1, false, uSTMatrix, 0);

        GLES20.glEnableVertexAttribArray(aTextureCoordHandler);
        GLES20.glVertexAttribPointer(aTextureCoordHandler, 2, GLES20.GL_FLOAT, false, 8, mTextureVertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sTextureHandler, 0);

        // 设置vVideo为1绘制正面视频
        GLES20.glUniform1i(vVideoHandler, 1);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mVertexFrontIndex.length, GLES20.GL_UNSIGNED_SHORT, mVertexFrontIndexBuffer);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mUpdateSurface = true;
    }

    void startMediaPlayer() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            Log.d("ysh", "startMediaPlayer in cube");
            mMediaPlayer.start();
        }
    }

    void pauseMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    /**
     * 退出重置MediaPlayer
     */
    void cleanMediaPlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    /**
     * 设置滤镜
     * @param filter NONE:原始，GRAY:黑白，ANTI:反色，DUIBI:增加对比度，LIGHTER:变亮，DARKEN:变暗，VAGUE:模糊
     */
    void setFilterIndex(Filter filter) {
        this.filter = filter;
    }

    /**
     * 获取shader中vChangeType、vChangeColor句柄
     */
    void onDrawCreatedSet() {
        hChangeType = GLES20.glGetUniformLocation(mCubeProgramId, "vChangeType");
        hChangeColor = GLES20.glGetUniformLocation(mCubeProgramId, "vChangeColor");
    }

    /**
     * 设置vChangeType、vChangeColor句柄值
     */
    void onDrawSet() {
        GLES20.glUniform1i(hChangeType, filter.getType());
        GLES20.glUniform3fv(hChangeColor, 1, filter.data(), 0);
    }

    public enum Filter {
        // 原色、黑白、反色、增加对比度、变亮、变暗、模糊
        NONE(0, new float[]{0.0f, 0.0f, 0.0f}),
        GRAY(1, new float[]{0.299f, 0.587f, 0.114f}),
        ANTI(4, new float[]{0.9f, 0.9f, 0.1f}),
        DUIBI(5, new float[]{0.5f, 0.5f, 0.5f}),
        LIGHTER(2, new float[]{0.4f, 0.4f, 0.0f}),
        DARKEN(6, new float[]{0.006f, 0.004f, 0.2f}),
        VAGUE(3, new float[]{0.006f, 0.004f, 0.002f});

        private int vChangeType;
        private float[] data;

        Filter(int vChangeType, float[] data) {
            this.vChangeType = vChangeType;
            this.data = data;
        }

        public int getType() {
            return vChangeType;
        }

        public float[] data() {
            return data;
        }
    }
}
