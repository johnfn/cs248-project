package edu.stanford.cs248.project.util

import javax.imageio.ImageIO

import scala.math._

trait SpatialMap {
  def xSize: Int
  def idx(x: Int, y: Int) = y*xSize + x
}

trait ClampedSpatialMap extends SpatialMap {
  def ySize: Int
  def clamp(value: Int, valueMax: Int) = min(max(0, value), valueMax)
  override def idx(x: Int, y: Int) = clamp(y, ySize-1)*xSize + clamp(x, xSize-1)
}

class ImageMap(val rcname: String) {
  val img = ImageIO.read(getClass.getResource(rcname))

  val xSize = img.getWidth()
  val ySize = img.getHeight()

  // Get image pixels
  // This just gets the right-most 8 bits, corresponding to the blue channel
  // This is okay, as the image is grayscale.
  def getArgbAry() = img.getRGB(0, 0, xSize, ySize, null, 0, xSize)
}

class ImageMapGrayscale(rcname: String, offs: Int)
  extends ImageMap(rcname) with SpatialMap
{
  // integers varying from 0 to 255
  val grayMap : Array[Int] = getArgbAry().map(_ & 0xff)

  def valueAt(x: Int, y: Int) = grayMap(idx(x, y)) + offs

  // These delta maps have an extra column and row respectively
  // It's expected that the user doesn't access these...
  def deltaXMap() = {
    val ary = new Array[Int](xSize*ySize)
    for(y <- 0 until ySize; x <- 0 until xSize-1)
      ary(idx(x,y)) = valueAt(x+1,y) - valueAt(x,y)
    new ArrayMapGrayscale(ary, xSize)
  }

  def deltaYMap() = {
    val ary = new Array[Int](xSize*ySize)
    for(x <- 0 until xSize; y <- 0 until ySize-1)
      ary(idx(x,y)) = valueAt(x,y+1) - valueAt(x,y)
    new ArrayMapGrayscale(ary, xSize)
  }
}

class ArrayMapGrayscale(val ary: Array[Int], val xSize: Int)
  extends SpatialMap
{
  val ySize = ary.size / xSize
  def valueAt(x: Int, y: Int) = ary(idx(x,y))
}

// interprets pixel value as one of the values a map
// The most byte of colorEnumMap MUST be '00'
class ImageMapObjMap[T](rcname: String, colorObjMap: Map[Int, T])
  extends ImageMap(rcname) with SpatialMap
{
  def getR(p: Int) = (p & 0x00ff0000) >>> 16
  def getG(p: Int) = (p & 0x0000ff00) >>> 8
  def getB(p: Int) = (p & 0x000000ff)

  // returns Manhattan color distance between two colors in [0, 255] space
  def colDist(col1: Int, col2: Int) =
    abs(getR(col1)-getR(col2)) +
    abs(getG(col1)-getG(col2)) +
    abs(getB(col1)-getB(col2))

  val alphaRemovedArgbAry = getArgbAry().map(_ & 0x00ffffff)

  val pixEnumMap = {
    alphaRemovedArgbAry.zipWithIndex.map { case (pix,i) =>
      // fetch obj immediately if exact match, otherwise find closest
      colorObjMap.getOrElse(pix, {
        val closestKey = colorObjMap.keys.minBy(colKey => colDist(pix, colKey))
        println("Inexact color map match: %s - (%d, %d)".format(
          rcname, i%xSize, i/xSize))
        colorObjMap(closestKey)
      })
    }
  }

  def valueAt(x: Int, y: Int) = pixEnumMap(idx(x, y))
}
