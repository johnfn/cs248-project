package edu.stanford.cs248.project.entity

import java.nio.ByteBuffer

case class Vertex(
  x: Float, y: Float, z: Float,
  nx: Float, ny: Float, nz: Float,
  s: Float, t: Float)
{
  def insertIntoBuf(vBuf: ByteBuffer) = {
    vBuf
      .putFloat(x).putFloat(y).putFloat(z)
      .putFloat(nx).putFloat(ny).putFloat(nz)
      .putFloat(s).putFloat(t)
  }
}

object Vertex {
  val posSize   = java.lang.Float.SIZE/8 * 3
  val norSize   = java.lang.Float.SIZE/8 * 3
  val texSize   = java.lang.Float.SIZE/8 * 2

  val posOffset   = 0
  val norOffset   = posOffset + posSize
  val texOffset   = norOffset + norSize

  val strideSize = texOffset + texSize
}
