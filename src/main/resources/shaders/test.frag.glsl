uniform sampler2D texture;

uniform bool showW;

varying vec2 texcoord;

void main()  
{     
   vec4 sample;
   
   sample = texture2D(texture, texcoord);
   
   if(showW) {
     gl_FragColor = vec4(vec3(1,1,1)*sample.w, 1);
   } else {
     gl_FragColor = vec4(sample.xyz, 1);
   }
}

