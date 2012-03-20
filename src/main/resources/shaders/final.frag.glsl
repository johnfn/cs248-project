uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;
uniform sampler2D ssaoBuf;

varying vec2 texcoord;

#define MAX_LIGHTS 1

// Using custom z buffer version
float zSample(vec2 texc) {
  return texture2D(nmlGbuf, texc).w-50.0;
}

void main()
{
  mat4 projMat = gl_TextureMatrix[0];

  vec2 originNdcXY = vec2(texcoord)*2.0-1.0;
  float originZeye = zSample(texcoord);
  float z_e = originZeye;

  float originXeye = ((-originNdcXY.x*z_e)-projMat[2][0]*z_e)/projMat[0][0];
  float originYeye = ((-originNdcXY.y*z_e)-projMat[2][1]*z_e)/projMat[1][1];

  vec3 v = vec3(originXeye, originYeye, originZeye);

  vec3 N = texture2D(nmlGbuf, texcoord).xyz;

  vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

  vec4 difMat = texture2D(difGbuf, texcoord);
  
  if(difMat.xyz != vec3(0., 0., 0.)) {  
    vec4 aoFactor = texture2D(ssaoBuf, texcoord);
  
    for (int i=0;i<MAX_LIGHTS;i++)
    {
      vec3 L = normalize(gl_LightSource[i].position.xyz - v);
      vec3 E = normalize(-v); // we are in Eye Coordinates, so EyePos is (0,0,0)
      vec3 R = normalize(-reflect(L,N));
  
      //calculate Ambient Term:
      vec4 Iamb =
        gl_FrontLightProduct[i].ambient*difMat*aoFactor;
  
      //calculate Diffuse Term:
      vec4 Idiff =
        gl_FrontLightProduct[i].diffuse * max(dot(N,L), 0.0)*difMat;
      Idiff = clamp(Idiff, 0.0, 1.0);
  
      // calculate Specular Term:
      vec4 Ispec = gl_FrontLightProduct[i].specular
             * pow(max(dot(R,E),0.0),0.3*gl_FrontMaterial.shininess);
      Ispec = clamp(Ispec, 0.0, 1.0);
  
      //finalColor += Idiff;
      finalColor += Iamb + Idiff + Ispec;
      //finalColor += vec4(L, 1);
    }
  
    // write Total Color:
    gl_FragColor = clamp(finalColor, 0.0, 1.0);
  }
  else {
    //gl_FragColor = vec4(1., 0., 0., 0.0);
  }
  //gl_FragColor = vec4(texcoord.s, texcoord.t, 0, 1.0);
//gl_FragColor = vec4(texture2D(difTex, texcoord).rgb, 1.0);
}
