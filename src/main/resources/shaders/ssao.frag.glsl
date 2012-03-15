uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

uniform float farClip;
varying vec2 texcoord;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float zSample(vec2 texc) {
  float normalizeNegativeZ = texture2D(nmlGbuf, texc).w; 
  return (farClip*0.4)*(normalizeNegativeZ-1.0);
}

void main()  
{
  float PI = 3.14159265358979323846264;
  int nAngles = 2;
  float lookupStep = 0.15;
  int nSamples = 4;
  float epsilon = 0.0;
  float maxZdiff = 2.0;
  
  mat4 projMat = gl_TextureMatrix[0];
  
  if(texture2D(nmlGbuf, texcoord).xyz != vec3(0,0,0)) {
  
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
        float sampleDist = float(j+1)*lookupStep;//*cos(tangentAngle);
        
        vec3 lookupPt = originEye + sampleDist*sampleDir;
        
        vec4 lookupClipHomo = gl_TextureMatrix[0]*vec4(lookupPt, 1.0);
        vec3 lookupClip = lookupClipHomo.xyz/lookupClipHomo.w;
        
        float lookupPtActualZ = zSample( lookupClip.xy*0.5+0.5 );
        
        // difference between xy plane of origin and actual z
        float zDiff = lookupPtActualZ - originEye.z;
        
        if(abs(zDiff) > epsilon && abs(zDiff) < maxZdiff) {        
          float horizAngle = atan(zDiff, sampleDist);
          
          if(horizAngle > maxHorizAngle) maxHorizAngle = horizAngle;
        }
      }
      
      ambFactor = 1.0-((maxHorizAngle - tangentAngle)/(PI/2.));
      
      cumAmbientFactor += (1.0/float(nAngles))*(ambFactor);
    }
    
    gl_FragColor = vec4(vec3(1,1,1)*(cumAmbientFactor), 1);    
  }
}

