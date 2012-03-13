attribute vec2 texcoordIn;

varying vec3 N;
varying vec3 v;
varying vec2 texcoord;
varying float zDepth;

void main()  
{     
   v = vec3(gl_ModelViewMatrix * gl_Vertex);       
   N = normalize(gl_NormalMatrix * gl_Normal);
   
   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
   
   // move from clip space (homogeneous) to zDepth space (goes from 0 to 1)
   zDepth = (gl_Position.z/gl_Position.w)*0.5+0.5;

   texcoord = texcoordIn;
}

