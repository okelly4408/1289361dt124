uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

attribute vec4 inPosition;
attribute vec2 inTexCoord;

varying vec2 uv;
void main(void)
{
vec4 pos = inPosition;

uv = vec2(pos.x/2.0, pos.y/2.0);

pos.x += -1.0;
pos.y += -1.0;

gl_Position = pos;


}
