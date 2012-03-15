#version 120

uniform float farClip; // to store linear normalized z depths

uniform sampler2D texDif;
uniform sampler2D texSpc;

varying vec3 Ncompressed;
varying vec2 texcoord;

varying vec4 vEyeHomo;

#define MAX_LIGHTS 1 

void main() 
{ 
   // Normal compressed to (0, 1) range in EYE space
   float normalizedNegZeye = -(vEyeHomo.z/vEyeHomo.w)/farClip;
   //gl_FragData[0] = vec4(vec3(1,1,1)*(-vEyeHomo.x/vEyeHomo.w)*0.05,
   //gl_FragData[0] = vec4(vec3(1,1,1)*(-vEyeHomo.z/vEyeHomo.w)*0.05, 
   gl_FragData[0] = vec4(Ncompressed, 
      normalizedNegZeye);
   
   // Diffuse color
   gl_FragData[1] = texture2D(texDif, texcoord);
   
   // Specular color
   gl_FragData[2] = texture2D(texSpc, texcoord);
}

