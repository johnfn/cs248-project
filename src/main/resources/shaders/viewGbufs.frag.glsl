uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

uniform int gBufNumber;
uniform bool showW;

varying vec2 texcoord;

void main()  
{     
   vec4 sample;
   
   if(gBufNumber == 0) sample = texture2D(nmlGbuf, texcoord);
   if(gBufNumber == 1) sample = texture2D(difGbuf, texcoord);
   if(gBufNumber == 2) sample = texture2D(spcGbuf, texcoord);
   
   if(showW) {
     gl_FragColor = vec4(vec3(1,1,1)*sample.w, 1);
   } else {
     gl_FragColor = vec4(sample.xyz, 1);
   }
}

