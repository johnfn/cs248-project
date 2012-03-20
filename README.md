# cs248 final project

A game written in Scala.

## Development notes

### Pixel position reconstruction

We originally used the z-buffer built during the g-buffer construction to sample pixel depths. We would then get to the pixel's view space by using the inverse projection matrix. However, we discovered that the z-buffer uses the vast majority of its precision in the area very close to the 'eye'. This leads to pretty poor precision z-buffers.

We ended up making a custom z buffer storage in the "w" channel of the normal g buffer. We set each channel of the g buffers to be 16 bit floats. It's a bit wasteful, and in the future, I will probably try this technique:

http://www.gamerendering.com/2008/09/25/packing-a-float-value-in-rgba/

### SSAO implementation

We used the horizon based ambient occlusion technique detailed here:

http://www.nvidia.com/object/siggraph-2008-HBAO.html

Our results were pretty good, but not perfect. The technique was expensive enough to warrant performing at half resolution and then blurring the result. We also suffered from banding due to an imperfectly accurate z buffer. Overall though, the results were fairly good. I put some screenshots up here:

http://tommycli.com/opengl-is-not-rl 

## Thanks to

* `jPCT` - www.jpct.net
