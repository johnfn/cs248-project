uniform sampler2D texInp;
uniform float texelX;
uniform float texelY;

varying vec2 texcoord;

float gaus(float x, float sigSq) {
  return exp(-(x*x/(2.0*sigSq)));
}

void main() 
{ 
  float accumWeights = 0.;
  float accumVal = 0.;
  
  float sigmaSqSpatial = 10.0*10.0;
  float sigmaSqValDist = 0.4*0.4;
  
  float myValue = texture2D(texInp, texcoord).x;
  
  for(int i=-5; i <= 5; i++) {
    float texelOffset = float(i);
    float samplePtX = texcoord.x - texelOffset*texelX;
    
    if(samplePtX > 0.0 && samplePtX < 1.0) {
      vec2 samplePt = vec2(samplePtX, texcoord.y);
      float sampleValue = texture2D(texInp, samplePt).x;
      
      float valDist = sampleValue - myValue;
      
      float weight = 
        gaus(texelOffset, sigmaSqSpatial)*gaus(valDist,sigmaSqValDist);
        
      accumWeights += weight;
      accumVal += weight*sampleValue;
    }
  }
  
  gl_FragColor = vec4(vec3(1,1,1)*accumVal/accumWeights, texcoord);
}

