varying vec3 lightDir;
varying vec3 v3Ray1;
varying vec4 c0, c1;
uniform sampler2D m_HeightMap;
uniform vec3 m_v3CamDir;
varying vec2 uv;
varying vec3 norm;
uniform vec3 m_Color;
uniform float m_intensity;
varying vec4 pos;
varying vec3 varPos;
uniform mat4 m_cubeMatrix;
uniform bool m_debugColor;
uniform bool m_hmView;
uniform float m_fExposure;
uniform float m_gamma;
uniform bool m_mars;
varying vec3 nm;

vec4 HDR(vec4 L) {
    L = L * m_fExposure;
    L.r = L.r < 1.413 ? pow(L.r * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.r);
    L.g = L.g < 1.413 ? pow(L.g * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.g);
    L.b = L.b < 1.413 ? pow(L.b * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.b);
    L.a = L.a < 1.413 ? pow(L.a * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.a);
    return L;
}



void main(void){

vec3 normal; 
vec3 light;

float texel = (1.0/258.0);

vec2 o00 = uv + vec2(-texel, -texel);
vec2 o10 = uv + vec2(0.0, -texel);
vec2 o20 = uv + vec2(texel, -texel);

vec2 o01 = uv + vec2(-texel, 0.0);
vec2 o21 = uv + vec2(texel, 0.0);

vec2 o02 = uv + vec2(-texel, texel);
vec2 o12 = uv + vec2(0.0,  texel);
vec2 o22 = uv + vec2(texel,  texel);


float h00 = texture2D( m_HeightMap, o00 ).r;
float h10 = texture2D( m_HeightMap, o10 ).r;
float h20 = texture2D( m_HeightMap, o20 ).r;

float h01 = texture2D( m_HeightMap, o01 ).r;
float h21 = texture2D( m_HeightMap, o21 ).r;

float h02 = texture2D( m_HeightMap, o02 ).r;
float h12 = texture2D( m_HeightMap, o12 ).r;
float h22 = texture2D( m_HeightMap, o22 ).r;

float Gx = h00 - h20 + 2.0 * h01 - 2.0 * h21 + h02 - h22;
float Gy = h00 + 2.0 * h10 + h20 - h02 - 2.0 * h12 - h22;

float Gz = m_intensity * sqrt(1.0 - Gx * Gx - Gy * Gy);

normal =  vec3(normalize(vec3(2.0 * Gx, 2.0 * Gy, Gz)));

vec3 color;
float slope = length(vec2(normal.x, normal.y));
//martian: vec3 color1 = vec3(0.7647, 0.3843, 0.2549);

vec3 color1 = vec3(0.1 , 0.30 , 0.0);
vec3 color2 = vec3(0.20 , 0.20 , 0.20);

//sample green channel for climate type
//desert = vec3(.941,.902,.549), vec3(0.8666667,0.7607843,0.54509807)
//scrub = vec3(0.5, 0.39, 0.2);
//grass = vec3(0.1 , 0.30 , 0.0)
//ice = (1.0)
vec3 redStone = vec3(0.7647, 0.3843, 0.2549);
vec3 greenGrass = vec3(0.1 , 0.30 , 0.0);
vec3 grayStone = vec3(0.20 , 0.20 , 0.20);
vec3 desertSand = vec3(0.8666667,0.7607843,0.54509807);
vec3 desertSandStone = vec3(.941,.902,.549);
vec3 scrublandOlive = vec3(0.29803923,0.4,0.0);
vec3 scrublandBrown = vec3(0.5,0.39,0.2);
vec3 whiteIce = vec3(1.0);
vec3 permafrostGreen = vec3(0.46666667,0.48235294,0.21568628);
vec3 permafrostBrown = vec3(0.5568628,0.46666667,0.26666668);

float b = texture2D(m_HeightMap, uv).b;

b -= 0.15;
float blendcol1 = smoothstep(0.35,0.5,b);
//float blendcol2 = smoothstep(.9548, .9555, b);
float blender2 = smoothstep(0.30, 0.60, slope);

vec3 dryCol = vec3(mix(desertSand, scrublandBrown, smoothstep(0.0, 0.45, b) ));
vec3 lessDryCol = vec3(mix(dryCol, scrublandOlive, smoothstep(0.45, 0.5, b) ));
vec3 wetCol = vec3(mix(lessDryCol, greenGrass, smoothstep (0.5, 0.85, b) ));
vec3 coldWet = vec3(mix(wetCol, permafrostGreen, smoothstep(0.85, 0.90, b) ));
vec3 colderDry = vec3(mix(coldWet, permafrostBrown, smoothstep(0.90, 0.95, b) ));
vec3 wetterCol = vec3(mix(colderDry, whiteIce, smoothstep(0.95, 0.96, b) ));
vec3 col1 = wetterCol;
vec3 col2 = mix(desertSandStone, grayStone, blendcol1);
col2 = mix(mix(desertSandStone, vec3(0.941,0.702,0.40) * .80, smoothstep(0.45, 0.75, slope)) , mix(mix(vec3(.8235, 0.7059, 0.5490), greenGrass, 0.5)/1.25, col2, blender2), blendcol1);
col2 = mix(col2, permafrostBrown/2.0, smoothstep(0.915, 0.930, b));
col2 = mix(col2, grayStone, smoothstep(0.930, 0.94,b));
col2 = mix(col2, whiteIce, smoothstep(0.99-0.15, 1.3-0.15, b));
//col2 = mix(whiteIce, col2, blendcol2);
color1 = col1;
color2 = col2;
color2 = mix(color2, grayStone, smoothstep(0.75, 0.78, h00));

if(h00 > 0.75)
color1 = whiteIce;

float blender = smoothstep(0.0,0.35,slope);
if(m_mars){
vec3 mainColor = vec3( 0.75 *vec3(0.9647, 0.3843, 0.2549));
vec3 midBand = vec3(0.75686276,0.6039216,0.41960785) * 0.8;
vec3 orangeRust = vec3(195.0, 88.0, 23.0)/255.0;
vec3 midMars = vec3(mix(midBand, orangeRust, smoothstep(0.0, 0.15, b+0.15)));
midMars = vec3(mix(midMars, mainColor, smoothstep(0.45, 0.75, b)));
color1 = mix( midMars, whiteIce, smoothstep(0.80, 0.80, b));
color2 = mix(orangeRust, whiteIce, smoothstep(0.80, 0.82, b));
color2 = mix((mainColor + midBand)/2.0, color2, smoothstep(0.0, 0.15, b+0.15));
}
color = mix(color1, color2, blender);

vec3 is = vec3(0.0);
if(h00 == 0.0){
color = vec3(0.0,0.0,0.1);
if(m_mars){
color = color1;
}else{
normal = normalize(pos.xyz);
vec3 h = normalize(lightDir + v3Ray1);
float ndoth = dot(normal, h);
ndoth = clamp(ndoth, 0.0, 1.0);
is = (vec3(1.0,1.0,.78) * pow(ndoth,10.0)) * 0.5;
}
}else{
 light = normalize(vec3(1.0,-2.8,1.0));
 }
 
float ldn = dot(light, normal);
ldn = max(0.0,ldn);

if(m_debugColor)
color = m_Color;

gl_FragColor = pow(HDR(vec4(c0 +  vec4(is + color * (1.75 * ldn),1.0) * c1)), vec4(m_gamma));
gl_FragColor.a = 1.0;

if(m_hmView)
gl_FragColor = texture2D(m_HeightMap, uv);
}