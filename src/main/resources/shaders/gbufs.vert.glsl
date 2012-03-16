uniform float farClip; // to store linear normalized z depths

attribute vec2 texcoordIn;

varying vec3 N;

varying vec2 texcoord;

varying vec4 vEyeHomo;

void main()  
{     
  vEyeHomo = gl_ModelViewMatrix*gl_Vertex;
  //vEye = vec3(gl_ModelViewMatrix[1][2], gl_ModelViewMatrix[2][2], gl_ModelViewMatrix[3][2]);
  //vEye = vEyeHomo.xyz/vEyeHomo.w;
  vec4 clipVec = gl_ModelViewProjectionMatrix * gl_Vertex;
  
  N = normalize(gl_NormalMatrix * gl_Normal);
  
  texcoord = texcoordIn;
  
  gl_Position = clipVec;
}

