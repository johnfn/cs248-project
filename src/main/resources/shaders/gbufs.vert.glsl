attribute vec2 texcoordIn;

varying vec3 N;
varying vec3 vCartesian;
varying vec2 texcoord;
varying vec4 clipVec;

void main()  
{     
  vec4 vViewSpace = gl_ModelViewMatrix * gl_Vertex; 
  vCartesian = vViewSpace.xyz/vViewSpace.w;
  
  N = normalize(gl_NormalMatrix * gl_Normal);
   
  gl_Position = gl_ProjectionMatrix * vViewSpace;
   
  texcoord = texcoordIn;
}

