//http://www.opengl.org/sdk/docs/tutorials/ClockworkCoders/lighting.php

uniform sampler2D dumTex;
uniform sampler2D difTex;

varying vec3 N;
varying vec3 v;
varying vec2 texcoord;

#define MAX_LIGHTS 1 

void main() 
{ 
   vec3 N = normalize(N);
   vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
   
   for (int i=0;i<MAX_LIGHTS;i++)
   {
      vec3 L = normalize(gl_LightSource[i].position.xyz - v); 
      vec3 E = normalize(-v); // we are in Eye Coordinates, so EyePos is (0,0,0) 
      vec3 R = normalize(-reflect(L,N)); 
   
      //calculate Ambient Term: 
      vec4 Iamb = gl_FrontLightProduct[i].ambient; 

      //calculate Diffuse Term: 
      vec4 Idiff = gl_FrontLightProduct[i].diffuse * max(dot(N,L), 0.0) * texture2D(difTex, texcoord);
      Idiff = clamp(Idiff, 0.0, 1.0); 
   
      // calculate Specular Term:
      vec4 Ispec = gl_FrontLightProduct[i].specular 
             * pow(max(dot(R,E),0.0),0.3*gl_FrontMaterial.shininess);
      Ispec = clamp(Ispec, 0.0, 1.0); 
   
      finalColor += Iamb + Idiff + Ispec;
   }
   
   // write Total Color:
   gl_FragColor = gl_FrontLightModelProduct.sceneColor + finalColor;
   //gl_FragColor = vec4(texcoord.s, texcoord.t, 0, 1.0);
   //gl_FragColor = vec4(texture2D(difTex, texcoord).rgb, 1.0);
}

