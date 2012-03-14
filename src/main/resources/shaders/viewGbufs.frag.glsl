uniform sampler2D posGbuf;
uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

uniform int gBufNumber;

varying vec2 texcoord;

void main()  
{     
   vec4 sample;
   
   if(gBufNumber == 0) sample = texture2D(posGbuf, texcoord);
   if(gBufNumber == 1) sample = texture2D(nmlGbuf, texcoord);
   if(gBufNumber == 2) sample = texture2D(difGbuf, texcoord);
   if(gBufNumber == 3) sample = texture2D(spcGbuf, texcoord);
   
   gl_FragColor = sample;
}

