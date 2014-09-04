uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform sampler2D heightmap;

#ifdef VERTEX

in vec4 inPosition;
out vec4 position;
in vec2 inTexCoords;
out vec2 uv;
void main(){
uv = inTexCoords;
position = inPosition;
position.y += (texture(heightmap, uv).r * 320.0);
gl_Position = projectionMatrix * viewMatrix * position;

}

#endif

#ifdef FRAGMENT
in vec2 uv;
in vec4 position;
out vec4 fragColor;
void main(){

float texel = 1.0/1024.0;

vec2 o00 = uv + vec2(-texel, -texel);
vec2 o10 = uv + vec2(0.0, -texel);
vec2 o20 = uv + vec2(texel, -texel);

vec2 o01 = uv + vec2(-texel, 0.0);
vec2 o21 = uv + vec2(texel, 0.0);

vec2 o02 = uv + vec2(-texel, texel);
vec2 o12 = uv + vec2(0.0,  texel);
vec2 o22 = uv + vec2(texel,  texel);


float h00 = texture(heightmap, o00 ).r;
float h10 = texture(heightmap, o10 ).r;
float h20 = texture(heightmap, o20 ).r;

float h01 = texture(heightmap, o01 ).r;
float h21 = texture(heightmap, o21 ).r;

float h02 = texture(heightmap, o02 ).r;
float h12 = texture(heightmap, o12 ).r;
float h22 = texture(heightmap, o22 ).r;

float Gx = h00 - h20 + 2.0 * h01 - 2.0 * h21 + h02 - h22;
float Gy = h00 + 2.0 * h10 + h20 - h02 - 2.0 * h12 - h22;

float Gz = 0.25 * sqrt(1.0 - Gx * Gx - Gy * Gy);

vec3 normal = vec3(normalize(vec3(2.0 * Gx, 2.0 * Gy, Gz)));

float ldn = dot(normalize(vec3(1.0, -2.8, 1.0)), normal);
ldn = max(0.0, ldn);
float slope = length(vec2(normal.x, normal.y));
vec3 color1 = vec3(0.237,0.457,0.137);
//vec3 color2 = vec3(0.733,0.588,0.0627);
vec3 color2 = vec3(0.4802,0.3255,0.07451);

vec3 color = mix( color1, color2, smoothstep(0.09, 0.15, slope) );
fragColor.rgba = vec4(color * ldn * 2.05, 1.0);
}
#endif