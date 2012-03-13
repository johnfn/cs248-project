#version 120

uniform sampler2D texDif;
uniform sampler2D texSpc;

varying vec3 N;
varying vec3 v;
varying vec2 texcoord;
varying float zDepth;

#define MAX_LIGHTS 1 

void main() 
{ 
   vec3 N = normalize(N);
   
   // Position
   gl_FragData[0] = vec4(v, 1.0);
   
   // Normal and depth
   gl_FragData[1] = vec4(N.xyz, zDepth);
   
   // Diffuse color
   gl_FragData[2] = texture2D(texDif, texcoord);
   
   // Specular color
   gl_FragData[3] = texture2D(texSpc, texcoord);
}

