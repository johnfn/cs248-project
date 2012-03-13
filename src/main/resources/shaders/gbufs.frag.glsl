#version 120

uniform sampler2D texDif;
uniform sampler2D texSpc;

varying vec3 N;
varying vec4 v;
varying vec2 texcoord;
varying vec3 ndcVec;

#define MAX_LIGHTS 1 

void main() 
{ 
   vec3 N = normalize(N);
   
   // Position in view space. NOT clip or NDC space
   gl_FragData[0] = v;
   
   // Normal and depth
   gl_FragData[1] = vec4(N.xyz, ndcVec.z);
   
   // Diffuse color
   gl_FragData[2] = texture2D(texDif, texcoord);
   
   // Specular color
   gl_FragData[3] = texture2D(texSpc, texcoord);
}

