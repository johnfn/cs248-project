uniform sampler2D texInp;
uniform float texelX;
uniform float texelY;

varying vec2 texcoord;

void main() 
{ 
  gl_FragColor = texture2D(texInp, texcoord);
}

