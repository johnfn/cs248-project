#version 120

uniform sampler2D texDif;
uniform sampler2D texSpc;

varying vec3 N;
varying vec3 vCartesian;
varying vec2 texcoord;

#define MAX_LIGHTS 1 

void main() 
{ 
   //float zDepth = ndcVec.z;
   
   // Position in view space. NOT clip or NDC space
   gl_FragData[0] = vec4(vCartesian, 1.0);
   
   // Normal
   gl_FragData[1] = vec4(N, 1.0);
   
   // Diffuse color
   gl_FragData[2] = texture2D(texDif, texcoord);
   
   // Specular color
   gl_FragData[3] = texture2D(texSpc, texcoord);
}

