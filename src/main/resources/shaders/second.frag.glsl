uniform sampler2D posGbuf;
uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

varying vec2 texcoord;

void main()  
{     
  gl_FragColor = texture2D(difGbuf, texcoord);
}

