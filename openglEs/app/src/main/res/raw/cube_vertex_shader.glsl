attribute vec4 vPosition;
uniform mat4 vMatrix;
varying  vec4 vColor;
attribute vec4 aColor;
attribute vec4 aTexCoord;// S T 纹理坐标
varying vec2 vTexCoord;
uniform mat4 uSTMatrix;
void main() {
    gl_Position = vMatrix * vPosition;
    vColor = aColor;
    vTexCoord = (uSTMatrix * aTexCoord).xy;
}