// http://www.opengl.org/sdk/docs/tutorials/ClockworkCoders/lighting.php

attribute vec2 texcoordIn;

varying vec3 N;
varying vec3 v;
varying vec2 texcoord;

void main()  
{     
   v = vec3(gl_ModelViewMatrix * gl_Vertex);       
   N = normalize(gl_NormalMatrix * gl_Normal);

   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
   texcoord = texcoordIn;
}

