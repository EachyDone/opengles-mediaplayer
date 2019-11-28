package com.learn.opengles;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/**
 * 正方体纹理渲染类
 * */
public class TextResourceReader {

    /**
     * 加载raw文件
     * @param context context
     * @param resId 资源Id
     * @return String
     */
    public static String readTextFileFromResource(Context context, int resId) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(resId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                sb.append(nextLine).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("could not open resource: " + resId, e);
        } catch (Resources.NotFoundException e) {
            throw new RuntimeException("resource no found: " + resId, e);
        }
        return sb.toString();
    }
}
