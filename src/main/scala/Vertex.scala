package edu.stanford.cs248.project

import java.nio.ByteBuffer

case class Vertex(
  x: Float, y: Float, z: Float,
  nx: Float, ny: Float, nz: Float,
  r: Byte, g: Byte, b: Byte,
  s: Float, t: Float) 
{
  def insertIntoBuf(vBuf: ByteBuffer) = {
    vBuf
      .putFloat(x).putFloat(y).putFloat(z)
      .putFloat(nx).putFloat(ny).putFloat(nz)
      .put(r).put(g).put(b)
      .putFloat(s).putFloat(t)
  }
}

object Vertex {
  val posSize = java.lang.Float.SIZE/8 * 3
  val norSize = java.lang.Float.SIZE/8 * 3
  val colSize = java.lang.Byte.SIZE/8 * 3
  val texSize = java.lang.Float.SIZE/8 * 2
  
  val posOffset = 0
  val norOffset = posSize
  val colOffset = norOffset + norSize
  val texOffset = colOffset + colSize
  
  val strideSize = texOffset + texSize
}
