uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;
uniform sampler2D ssaoBuf;

varying vec2 texcoord;

void main()  
{
  //gl_FragColor = vec4(vec3(1,1,1), 1);
  gl_FragColor = texture2D(difGbuf, texcoord);
}

