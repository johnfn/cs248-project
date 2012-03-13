attribute vec2 texcoordIn;

varying vec3 N;
varying vec4 v;
varying vec2 texcoord;
varying vec3 ndcVec;

void main()  
{     
   v = gl_ModelViewMatrix * gl_Vertex;       
   N = normalize(gl_NormalMatrix * gl_Normal);
   
   vec4 clipVec = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
   
   gl_Position = clipVec;
   
   ndcVec = clipVec.xyz/clipVec.w;

   texcoord = texcoordIn;
}

