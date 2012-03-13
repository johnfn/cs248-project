uniform sampler2D posGbuf;
uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

uniform int gBufNumber;
uniform bool viewW;

varying vec2 texcoord;

void main()  
{     
   vec4 sample;
   
   if(gBufNumber == 0) sample = texture2D(posGbuf, texcoord);
   if(gBufNumber == 1) sample = texture2D(nmlGbuf, texcoord);
   if(gBufNumber == 2) sample = texture2D(difGbuf, texcoord);
   if(gBufNumber == 3) sample = texture2D(spcGbuf, texcoord);
   
   if(viewW) {
     gl_FragColor = vec4(vec3(1.0, 1.0, 1.0)*sample.w, 1.0);
   } else {
     gl_FragColor = vec4(sample.rgb*0.5+0.5, 1.0);
   }
}

