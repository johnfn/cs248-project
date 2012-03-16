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
  
  float sigmaSqValDist = 0.5*0.5;
  
  float myValue = texture2D(texInp, texcoord).x;
  
  for(int i=-2; i <= 2; i++) {
    float texelOffset = float(i);
    float samplePtY = texcoord.y - texelOffset*texelY;
    
    if(samplePtY > 0.0 && samplePtY < 1.0) {
      vec2 samplePt = vec2(texcoord.x, samplePtY);
      float sampleValue = texture2D(texInp, samplePt).x;
      
      float valDist = sampleValue - myValue;
      
      float weight = gaus(valDist,sigmaSqValDist);
        
      accumWeights += weight;
      accumVal += weight*sampleValue;
    }
  }
  
  gl_FragColor = vec4(vec3(1,1,1)*accumVal/accumWeights, texcoord);
}

