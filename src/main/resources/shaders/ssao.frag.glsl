uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;
//uniform sampler2D zBuf;

uniform float farClip;
varying vec2 texcoord;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

// Using custom z buffer version
float zSample(vec2 texc) {
  return texture2D(nmlGbuf, texc).w-farClip;
}

void main()
{
  float PI = 3.14159265358979323846264;
  int nAngles = 4;
  float lookupStep = 0.20;
  int nSamples = 3;
  float epsilon = 0.05;
  float maxZdiff = 2.0;

  mat4 projMat = gl_TextureMatrix[0];

  if(texture2D(nmlGbuf, texcoord).xyz != vec3(0, 0, 0)) {

    vec2 originNdcXY = vec2(texcoord)*2.0-1.0;

    float originZeye = zSample(texcoord);
    float z_e = originZeye;

    float originXeye = ((-originNdcXY.x*z_e)-projMat[2][0]*z_e)/projMat[0][0];
    float originYeye = ((-originNdcXY.y*z_e)-projMat[2][1]*z_e)/projMat[1][1];

    vec3 originEye = vec3(originXeye, originYeye, originZeye);
    
    vec3 oW = originEye;

    // Generate random normalized tangent and bitangent.
    // This allows us to trade low frequency noise for high frequency noise,
    // which we can blur
    vec3 rVec = vec3(
      rand(oW.xy), rand(oW.yz), rand(oW.zx)
      )*2.0-1.0;
    vec3 normal = texture2D(nmlGbuf, texcoord).xyz;

    float cumAmbientFactor = 0.;

    vec3 tangent = normalize(cross(normal, rVec));
    vec3 bitangent = cross(normal, tangent);

    for(int i=0; i < nAngles; i++) {
      float angle = float(i)/float(nAngles)*2.0*PI;

      // should be a unit vector in a direction perp. to normal
      vec3 sampleDir = cos(angle)*tangent + sin(angle)*bitangent;

      // angle tangent surface makes from z-contours
      float tangentAngle =
        atan(sampleDir.z/sqrt(dot(sampleDir.xy,sampleDir.xy)));

      // angle sampled point makes from z-contour
      float maxHorizAngle = tangentAngle;
      float ambFactor = 0.0;

      for(float j=1.; j <= nSamples + 0.5; j++) {
        // sampleDist in view space
        float sampleDistXY = j*lookupStep/pow(cos(tangentAngle), 1.0);

        vec3 lookupPt = 
          originEye + sampleDistXY*normalize(vec3(sampleDir.xy, 0));

        vec4 lookupClipHomo = gl_TextureMatrix[0]*vec4(lookupPt, 1.0);
        vec3 lookupClip = lookupClipHomo.xyz/lookupClipHomo.w;
        vec2 lookupTexCoord = lookupClip.xy*0.5+0.5;

        float lookupPtActualZ = 0.;

        lookupPtActualZ = zSample( lookupTexCoord );

        // difference between xy plane of origin and actual z
        float zDiff = lookupPtActualZ - originEye.z;

        if(abs(zDiff) > epsilon && zDiff < maxZdiff) {
          float horizAngle = atan(zDiff, sampleDistXY);
          maxHorizAngle = max(maxHorizAngle, horizAngle);
        }
      }

      //ambFactor = 1.0-((maxHorizAngle - tangentAngle)/(PI/2));
      //ambFactor = (maxHorizAngle-tangentAngle)/PI;
      ambFactor = 1.-(sin(maxHorizAngle)-sin(tangentAngle));

      cumAmbientFactor += (1.0/float(nAngles))*(ambFactor);
    }

    gl_FragColor = vec4(vec3(1,1,1)*(cumAmbientFactor), 1);
  }
}

