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
inline float zSample(vec2 texc) {
  return -texture2D(nmlGbuf, texc).w;
}

void main()  
{
  float PI = 3.14159265358979323846264;
  int nAngles = 2;
  float lookupStep = 0.2;
  int nSamples = 4;
  float epsilon = 0.2;
  float maxZdiff = 2.0;
  
  mat4 projMat = gl_TextureMatrix[0];
  
  if(texture2D(nmlGbuf, texcoord).xyz != vec3(0, 0, 0)) {
  
    vec2 originNdcXY = vec2(texcoord)*2.0-1.0;
    
    float originZeye = zSample(texcoord);
    float z_e = originZeye;  
  
    float originXeye = ((-originNdcXY.x*z_e)-projMat[2][0]*z_e)/projMat[0][0];
    float originYeye = ((-originNdcXY.y*z_e)-projMat[2][1]*z_e)/projMat[1][1];
    
    vec3 originEye = vec3(originXeye, originYeye, originZeye); 
    
    // Generate random normalized tangent and bitangent.
    // This allows us to trade low frequency noise for high frequency noise, 
    // which we can blur
    vec3 rVec = vec3( 
      rand(texcoord), rand(texcoord+vec2(1,1)), rand(texcoord+vec2(2,2))
      )*2.0-1.0;
    vec3 normal = texture2D(nmlGbuf, texcoord).xyz*2.0-1.0;
    
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
      float maxHorizAngle = -PI/2.; // actually starts at -pi/2.
      float ambFactor = 0.0;
      
      for(int j=0; j < nSamples; j++) {
        float sampleDist = float(j+1)*lookupStep*cos(tangentAngle);
        
        vec3 lookupPt = originEye + sampleDist*sampleDir;
        
        vec4 lookupClipHomo = gl_TextureMatrix[0]*vec4(lookupPt, 1.0);
        vec3 lookupClip = lookupClipHomo.xyz/lookupClipHomo.w;
        vec2 lookupTexCoord = lookupClip.xy*0.5+0.5;
        
        vec3 lookupNormal = texture2D(nmlGbuf, lookupTexCoord).xyz*2.0-1.0;
        
        // only allow occlusion by surface with normal different from own
        // this prevents self occlusion due to poor z depth
        /*if(distance(lookupNormal, normal) < 0.1) {
          maxHorizAngle = max(maxHorizAngle, tangentAngle);
        } else {*/
        
          float lookupPtActualZ = zSample( lookupTexCoord );
          
          // difference between xy plane of origin and actual z
          float zDiff = lookupPtActualZ - originEye.z;
          
          if(zDiff < maxZdiff) {
              float horizAngle = atan(zDiff, sampleDist);
              
              maxHorizAngle = max(maxHorizAngle, horizAngle);
          
          }
        //}
      }
      
      //ambFactor = 1.0-(sin(maxHorizAngle) - sin(tangentAngle));
      ambFactor = 1.0-((maxHorizAngle - tangentAngle)/(PI/2.));
      
      cumAmbientFactor += (1.0/float(nAngles))*(ambFactor);
    }
    
    // treshold it
    //if(cumAmbientFactor > 0.8) cumAmbientFactor = 1.0;
    
    //gl_FragColor = vec4(vec3(1,1,1)*(-originZeye*0.05), 1); 
    gl_FragColor = vec4(vec3(1,1,1)*(cumAmbientFactor), 1);    
  }
}

