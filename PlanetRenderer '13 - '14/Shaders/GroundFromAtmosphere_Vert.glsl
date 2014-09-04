uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;

uniform vec3 m_v3CameraPos;		// The camera's current position
uniform vec3 m_v3LightPos;		// The direction vector to the light source
uniform vec3 m_v3InvWavelength;	// 1 / pow(wavelength, 4) for the red, green, and blue channels
uniform float m_fCameraHeight;	// The camera's current height
uniform float m_fCameraHeight2;	// fCameraHeight^2
uniform float m_fOuterRadius;		// The outer (atmosphere) radius
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
attribute vec4 inPosition;
attribute vec2 inTexCoord;
varying vec2 uv;
uniform int m_nSamples;
uniform float m_fSamples;
uniform sampler2D m_HeightMap;
uniform vec3 m_meshOffset;
uniform float m_scale;
uniform mat4 m_cubeMatrix;
uniform int m_t;
varying vec3 v3Ray1;
varying vec3 lightDir;
varying vec4 c0, c1;
varying vec4 pos;
float nsize = m_fInnerRadius;
float denom1 = nsize*nsize*2.0;
float denom2 = nsize*nsize*nsize*nsize*3.0;
	 
vec3 spherize( in vec4 v){
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

void main(void)
{
vec4 p = m_cubeMatrix *  vec4(inPosition.xyz * m_scale,1.0);
float yTimes = inPosition.y;
vec3 truePosition = ((vec3(p.xyz)) + m_meshOffset);

uv = inTexCoord;
uv *= (256.0/258.0);
uv += (1.0/258.0);
lightDir = m_v3LightPos;
pos = vec4(spherize(vec4(truePosition.xyz, 1.0)).xyz,1.0);
float n = texture2D(m_HeightMap, (uv)).r*720.0 * yTimes;
if(yTimes < 0.0) n = n - 1.0;
vec3 normalpos = normalize(vec3(pos.xyz));
pos.x = normalpos.x * (n + nsize);
pos.y = normalpos.y * (n + nsize);
pos.z = normalpos.z * (n + nsize);
	// Get the ray from the camera to the vertex, and its length (which is the far point of the ray passing through the atmosphere)
	vec3 v3Pos = vec3(g_WorldMatrix * pos);
	vec3 v3Ray = v3Pos - m_v3CameraPos;
	 v3Ray1 = -normalize(v3Ray);
	float fFar = length(v3Ray);
	v3Ray /= fFar;

	// Calculate the ray's starting position, then calculate its scattering offset
	vec3 v3Start = m_v3CameraPos;
	float fDepth = exp((m_fInnerRadius - m_fCameraHeight) / m_fScaleDepth);
	//float fCameraAngle = dot(-v3Ray, v3Pos) / length(v3Pos);
	float fCameraAngle = 1.0;
	float fLightAngle = dot(m_v3LightPos, v3Pos) / length(v3Pos);
	float fCameraScale = scale(fCameraAngle);
	float fLightScale = scale(fLightAngle);
	float fCameraOffset = fDepth*fCameraScale;
	float fTemp = (fLightScale + fCameraScale);

	// Initialize the scattering loop variables
	float fSampleLength = fFar / m_fSamples;
	float fScaledLength = fSampleLength * m_fScale;
	vec3 v3SampleRay = v3Ray * fSampleLength;
	vec3 v3SamplePoint = v3Start + v3SampleRay * 0.65;

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

	c0.xyz = v3FrontColor * (m_v3InvWavelength * m_fKrESun + m_fKmESun);

	// Calculate the attenuation factor for the ground
	c1.xyz = v3Attenuate;

	gl_Position = g_WorldViewProjectionMatrix * pos;

}
