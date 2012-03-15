uniform sampler2D nmlGbuf;
uniform sampler2D difGbuf;
uniform sampler2D spcGbuf;

uniform float farClip;
uniform bool showSSAO;

varying vec2 texcoord;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

float zSample(vec2 texc) {
  float normalizeNegativeZ = texture2D(nmlGbuf, texcoord).w; 
  return -farClip*normalizeNegativeZ;
}

void main()  
{
  float PI = 3.14159265358979323846264;
  int nAngles = 8;
  float lookupStep = 0.25;
  int nSamples = 8;
  float epsilon = 0.01;
  
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
    vec3 rVec = 
      vec3(rand(texcoord), rand(texcoord+vec2(1,1)), rand(texcoord+vec2(2,2)));
    vec3 normal = texture2D(nmlGbuf, texcoord).xyz*2.0-1.0;
    
    float cumAmbientFactor = 0.;
    
    vec3 tangent = normalize(cross(normal, rVec));
    vec3 bitangent = cross(normal, tangent); 
    
    for(int i=0; i < nAngles; i++) {
      float angle = float(i)/float(nAngles)*2.0*PI;
      
      // should be a unit vector in a direction perp. to normal
      vec3 sampleDir = cos(angle)*tangent + sin(angle)*bitangent;
      
      // angle tangent surface makes from z-contours
      float angleFromSampleDir = asin(dot(sampleDir, vec3(0, 0, -1)));
      
      // angle sampled point makes from z-contour 
      float maxAngle = 0.;
      
      for(int j=0; j < nSamples; j++) {
        // this is in view space
        float sampleDist = float(j+1)*lookupStep;
        vec3 lookupPt = originEye + sampleDist*sampleDir; // in view space
        vec4 lookupClipHomo = gl_TextureMatrix[0]*vec4(lookupPt, 1.0);
        vec3 lookupClip = lookupClipHomo.xyz/lookupClipHomo.w;
        
        float lookupPtActualZ = zSample(lookupClip.xy*0.5+0.5);
        
        if(lookupPtActualZ > lookupPt.z + epsilon) {
          float angle = 
            atan(lookupPtActualZ-lookupPt.z, sampleDist) + angleFromSampleDir;
          if(angle > maxAngle) maxAngle = angle;
        }
      }
      
      cumAmbientFactor += 1.0/float(nAngles)*(1.-maxAngle/(PI/2.));
    }
    
    if(showSSAO) {
      gl_FragColor = vec4(tangent*0.5+0.5, 1);
      //gl_FragColor = vec4(normal, 1);
      //gl_FragColor = vec4(vec3(1,1,1)*cumAmbientFactor, 1);
    }
    else {
      gl_FragColor = texture2D(difGbuf, texcoord);
    }
  }
}

