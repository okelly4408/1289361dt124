uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
attribute vec4 inPosition;
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
varying vec4 c0, c1;
uniform int m_nSamples;
uniform float m_fSamples;
varying vec3 v3Direction;


float scale(float fCos)
{
	float x = 1.0 - fCos;
	return m_fScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void main(void)
{
	// Get the ray from the camera to the vertex, and its length (which is the far point of the ray passing through the atmosphere)
	vec3 v3Pos = vec3(g_WorldMatrix * inPosition);
	vec3 v3Ray = v3Pos - m_v3CameraPos;
	float fFar = length(v3Ray);
	v3Ray /= fFar;

	// Calculate the ray's starting position, then calculate its scattering offset
	vec3 v3Start = m_v3CameraPos;
	float fHeight = length(v3Start);
	float fDepth = exp(m_fScaleOverScaleDepth * (m_fInnerRadius - m_fCameraHeight));
	float fStartAngle = dot(v3Ray, v3Start) / fHeight;
	float fStartOffset = fDepth*scale(fStartAngle);

	// Initialize the scattering loop variables
	//gl_FrontColor = vec4(0.0, 0.0, 0.0, 0.0);
	float fSampleLength = fFar / m_fSamples;
	float fScaledLength = fSampleLength * m_fScale;
	vec3 v3SampleRay = v3Ray * fSampleLength;
	vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;

	// Now loop through the sample rays
	vec3 v3FrontColor = vec3(0.0, 0.0, 0.0);
	for(int i=0; i<m_nSamples; i++)
	{
		float fHeight = length(v3SamplePoint);
		float fDepth = exp(m_fScaleOverScaleDepth * (m_fInnerRadius - fHeight));
		float fLightAngle = dot(m_v3LightPos, v3SamplePoint) / fHeight;
		float fCameraAngle = dot(v3Ray, v3SamplePoint) / fHeight;
		float fScatter = (fStartOffset + fDepth*(scale(fLightAngle) - scale(fCameraAngle)));
		vec3 v3Attenuate = exp(-fScatter * (m_v3InvWavelength * m_fKr4PI + m_fKm4PI));
		v3FrontColor += v3Attenuate * (fDepth * fScaledLength);
		v3SamplePoint += v3SampleRay;
	}

	// Finally, scale the Mie and Rayleigh colors and set up the varying variables for the pixel shader
	c1.xyz = v3FrontColor * m_fKmESun;
	c0.xyz = v3FrontColor * (m_v3InvWavelength * m_fKrESun);
	gl_Position = g_WorldViewProjectionMatrix * inPosition;
	v3Direction = m_v3CameraPos - v3Pos;
}
