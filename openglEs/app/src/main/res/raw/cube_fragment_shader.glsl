#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec4 vColor;
varying vec2 vTexCoord;
uniform samplerExternalOES sTexture;
uniform int vVideo;
uniform int vChangeType;
uniform vec3 vChangeColor;

void modifyColor(vec4 color) {
    color.r = max(min(color.r, 1.0), 0.0);
    color.g = max(min(color.g, 1.0), 0.0);
    color.b = max(min(color.b, 1.0), 0.0);
    color.a = max(min(color.a, 1.0), 0.0);
}

void main() {
    // 视频面
    if (vVideo == 1) {
        vec4 nColor = texture2D(sTexture, vTexCoord);
        if (vChangeType == 1) { // 黑白
            float c = nColor.r * vChangeColor.r + nColor.g * vChangeColor.g + nColor.b * vChangeColor.b;
            gl_FragColor = vec4(c, c, c, nColor.a);
        } else if (vChangeType == 2) { // 变亮
            vec4 deltaColor = nColor + vec4(vChangeColor, 0.0);
            modifyColor(deltaColor);
            gl_FragColor = deltaColor;
        } else if (vChangeType == 3) { // 模糊
            float dis = 0.01;// 距离越大越模糊
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y + dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y + dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y + dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y + dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x - dis, vTexCoord.y + dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y - dis));
            nColor += texture2D(sTexture, vec2(vTexCoord.x + dis, vTexCoord.y + dis));
            nColor /= 13.0;// 周边13个颜色相加，然后取平均，作为这个点的颜色
            gl_FragColor = nColor;
        } else if (vChangeType == 4) { // 反色
            gl_FragColor.r = nColor.g * nColor.b;
            gl_FragColor.g = nColor.r * nColor.b;
            gl_FragColor.b = nColor.r * nColor.g;
            gl_FragColor.a = 1.0;
        } else if (vChangeType == 5) { // 增加对比度
            vec4 target = vec4(0.0, 0.0, 0.0, 0.0);
            gl_FragColor = vec4(mix(target, nColor, 0.5));
        } else if (vChangeType == 6) {
            gl_FragColor.r = nColor.r * nColor.r;
            gl_FragColor.g = nColor.g * nColor.g;
            gl_FragColor.b = nColor.b * nColor.b;
            gl_FragColor.a = 1.0;
        } else {
            gl_FragColor = nColor;
        }
    } else { // 立方体白色页面
        gl_FragColor = vColor;
    }
}