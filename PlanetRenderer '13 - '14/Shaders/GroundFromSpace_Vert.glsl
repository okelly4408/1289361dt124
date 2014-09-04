uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat4 g_WorldViewMatrix;
uniform vec3 m_v3CameraPos;		// The camera's current position
uniform vec3 m_v3LightPos;		// The direction vector to the light source
uniform vec3 m_v3InvWavelength;	// 1 / pow(wavelength, 4) for the red, green, and blue channels
uniform float m_fCameraHeight;	// The camera's current height
uniform float m_fCameraHeight2;	// fCameraHeight^2
uniform float m_fOuterRadius;   // The outer (atmosphere) radius
uniform float m_fOuterRadius2;	// fOuterRadius^2
uniform float m_fInnerRadius;		// The inner (planetary) radius
uniform float m_fInnerRadius2;	// fInnerRadius^2
uniform float m_fKrESun;			// Kr * ESun
uniform float m_fKmESun;			// Km * ESun
uniform float m_fKr4PI;			// Kr * 4 * PI
uniform float m_fKm4PI;			// Km * 4 * PI
uniform float m_fScale;			// 1 / (fOuterRadius - fInnerRadius)
uniform float m_fScaleDepth;		// The scale depth (i.e. the altitude at which the atmosphere's average density is found)
uniform float m_fScaleOverScaleDepth;	// fScale / fScaleDepth
attribute vec3 inPosition;
attribute vec2 inTexCoord;
uniform int m_nSamples;
uniform float m_fSamples;
varying vec3 nm;
varying vec4 c1;
varying vec4 c0;
varying vec2 uv;
varying vec3 lightDir;
uniform sampler2D m_HeightMap;
uniform vec3 m_meshOffset;
uniform mat4 m_cubeMatrix;
uniform float m_scale;
uniform float m_size;
varying vec4 pos;
varying vec3 v3Ray1;
varying vec3 norm;
float nsize = m_fInnerRadius;
float denom1 = nsize*nsize*2.0;
float denom2 = nsize*nsize*nsize*nsize*3.0;

vec3 spherize( in vec3 v){

float x = (v.x * (sqrt(1.0-((v.y*v.y)/denom1)-((v.z*v.z)/denom1)+(((v.y*v.y)*(v.z*v.z))/denom2))));
float y = (v.y * (sqrt(1.0-((v.x*v.x)/denom1)-((v.z*v.z)/denom1)+(((v.x*v.x)*(v.z*v.z))/denom2))));
float z = (v.z * (sqrt(1.0-((v.y*v.y)/denom1)-((v.x*v.x)/denom1)+(((v.y*v.y)*(v.x*v.x))/denom2))));
return vec3(x,y,z);
}

float scale(float fCos)
{
	float x = 1.0 - fCos;
	return m_fScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

vec3 map(in vec3 p, vec2 uv){
    vec4 v = m_cubeMatrix * vec4(p * m_scale, 1.0);
    float yTimes = p.y;
    vec3 pos = v.xyz + m_meshOffset;
    pos = spherize(pos);
    float n = texture2D(m_HeightMap, uv).r * 720.0 * yTimes;
    if(yTimes < 0.0) n = n - 1.0;
    vec3 normalpos = normalize(vec3(pos.xyz));
    pos.x = normalpos.x * (n + nsize);
    pos.y = normalpos.y * (n + nsize);
    pos.z = normalpos.z * (n + nsize);
    return pos;
}
vec3 calcNormal( in vec3 pos, in vec2 uv )
{
    float e = 1.0/258.0;
	vec3 eps = vec3( e, 0.0, 0.0 );
	vec3 nor = vec3(
	    map(pos+eps.xyy, uv).x - map(pos-eps.xyy, uv).x,
	    map(pos+eps.yxy, uv).x - map(pos-eps.yxy, uv).x,
	    map(pos+eps.yyx, uv).x - map(pos-eps.yyx, uv).x );
	return normalize(nor);
}

void main(void)
{

uv = inTexCoord;
uv *= (256.0/258.0);
uv += (1.0/258.0);
vec4 p = m_cubeMatrix *  vec4(vec3(inPosition.xyz) * m_scale, 1.0);

float yTimes = inPosition.y;
vec3 truePosition = ((vec3(p.xyz)) + m_meshOffset);

pos = vec4( spherize(vec3(truePosition)).xyz,1.0);
float n = texture2D(m_HeightMap, (uv)).r * 720.0 * yTimes; 

vec3 normalpos = normalize(vec3(pos.xyz));
pos.x = normalpos.x * (n + nsize);
pos.y = normalpos.y * (n + nsize);
pos.z = normalpos.z * (n + nsize);

lightDir = normalize(m_v3LightPos);
	// Get the ray from the camera to the vertex and its length (which is the far point of the ray passing through the atmosphere)
	vec3 v3Pos = vec3(g_WorldMatrix * pos);
	
	vec3 v3Ray = v3Pos - m_v3CameraPos;
	 v3Ray1 = -normalize(v3Ray);
	float fFar = length(v3Ray);
	v3Ray /= fFar;

	// Calculate the closest intersection of the ray with the outer atmosphere (which is the near point of the ray passing through the atmosphere)
	float B = 2.0 * dot(m_v3CameraPos, v3Ray);
	float C = m_fCameraHeight2 - m_fOuterRadius2;
	float fDet = max(0.0, B*B - 4.0 * C);
	float fNear = 0.5 * (-B - sqrt(fDet));

	// Calculate the ray's starting position, then calculate its scattering offset
	vec3 v3Start = m_v3CameraPos + v3Ray * fNear;
	fFar -= fNear;
	float fDepth = exp((m_fInnerRadius - m_fOuterRadius) / m_fScaleDepth);
	float fCameraAngle = dot(-v3Ray, v3Pos) / length(v3Pos);
	float fLightAngle = dot(m_v3LightPos, v3Pos) / length(v3Pos);
	float fCameraScale = scale(fCameraAngle);
	float fLightScale = scale(fLightAngle);
	float fCameraOffset = fDepth*fCameraScale;
	float fTemp = (fLightScale + fCameraScale);

	// Initialize the scattering loop variables
	float fSampleLength = fFar / m_fSamples;
	float fScaledLength = fSampleLength * m_fScale;
	vec3 v3SampleRay = v3Ray * fSampleLength;
	vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;

	// Now loop through the sample rays
	vec3 v3FrontColor = vec3(0.0, 0.0, 0.0);
	vec3 v3Attenuate;
	for(int i=0; i<m_nSamples; i++)
	{
		float fHeight = length(v3SamplePoint);
		float fDepth = exp(m_fScaleOverScaleDepth * (m_fInnerRadius - fHeight));
		float fScatter = fDepth*fTemp - fCameraOffset;
		v3Attenuate = exp(-fScatter * (m_v3InvWavelength * m_fKr4PI + m_fKm4PI));
		v3FrontColor += v3Attenuate * (fDepth * fScaledLength);
		v3SamplePoint += v3SampleRay;
	}

	c0 = vec4(v3FrontColor * (m_v3InvWavelength * m_fKrESun + m_fKmESun),1.0);
	c1 = vec4(v3Attenuate,1.0);
	
	gl_Position = (g_WorldViewProjectionMatrix * pos);
}
