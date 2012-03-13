attribute vec2 texcoordIn;

varying vec2 texcoord;

void main()  
{
  texcoord = texcoordIn;
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}

