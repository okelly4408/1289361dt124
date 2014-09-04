attribute vec4 inPosition;

varying vec3 varPos;

void main(void){
vec4 pos = inPosition;
varPos = vec3(inPosition.xyz);

pos.x += -1.0;
pos.y += -1.0;

gl_Position = pos;

}