package edu.stanford.cs248.project

import java.nio.ByteBuffer
import javax.imageio.ImageIO

import scala.math._

import org.lwjgl.opengl._

import edu.stanford.cs248.project.util._
import edu.stanford.cs248.project.entity._

class LevelModel(val name: String) extends VBOModel {
  val heightMap = new ImageMapGrayscale("/levels/"+name+"_h.png")
  val deltaXMap = heightMap.deltaXMap
  val deltaYMap = heightMap.deltaYMap

  val xSize = heightMap.xSize
  val ySize = heightMap.ySize

  val zNormalQuads = xSize*ySize
  val xNormalQuads = deltaXMap.ary.count(_ != 0) // quads facing the x axis
  val yNormalQuads = deltaYMap.ary.count(_ != 0)
  val nQuads = xNormalQuads + yNormalQuads + zNormalQuads

  val nVerts = nQuads*4
  val nIdxs  = nVerts // just one index per vertex

  val drawMode = GL11.GL_QUADS

  val zScale = 0.03f

  def inBounds(x: Double, y: Double) =
    x > -0.5 && y > -0.5 && x < xSize-0.5 && y < ySize-0.5

  def height(x: Double, y: Double) = {
    val clampedX = round(max(min(xSize-0.5, x), -0.5))
    val clampedY = round(max(min(ySize-0.5, y), -0.5))
    heightMap.valueAt(clampedX.toInt, clampedY.toInt)*zScale
  }

  def populateVerBuffer(vBuf: ByteBuffer) = {
    // insert one vertex per pixel in the heightmap
    // note in this case, the top-left of the image corresponds to (0,0)
    // and x+ and y+ are right and down in the image
    // This is different from opengl's texture coordinate system
    val floorCorners = Array(
      (-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

    for(y <- 0 until ySize; x <- 0 until xSize) {
      val xf = x.asInstanceOf[Float]
      val yf = y.asInstanceOf[Float]
      val zf = heightMap.valueAt(x,y)*zScale
      val dzdx = deltaXMap.valueAt(x,y)*zScale
      val dzdy = deltaYMap.valueAt(x,y)*zScale

      val hc = heightMap.valueAt(x,y).asInstanceOf[Byte]

      // paint floor tiles
      floorCorners.foreach { case(dx, dy) =>
        Vertex(
          xf+dx, yf+dy, zf,
          0, 0, 1,
          50, 50, 50,
          dx+0.5f, dy+0.5f)
          .insertIntoBuf(vBuf)
      }

      // paint x facing walls
      if(dzdx != 0) {
        val nx = if(dzdx > 0) -1.0f else 1.0f
        floorCorners.foreach { case(dy, dz) =>
          Vertex(
            xf+0.5f, yf+dy*nx, zf+(dz+0.5f)*dzdx,
            nx, 0, 0,
            100, 100, 100,
            dy+0.5f, (dz+0.5f)*dzdx) // texture coordinates should tile wall
            .insertIntoBuf(vBuf)
        }
      }

      // paint y facing walls
      if(dzdy != 0) {
        val ny = if(dzdy < 0) -1.0f else 1.0f
        floorCorners.foreach { case(dx, dz) =>
          Vertex(
            xf+dx*ny, yf+0.5f, zf+(dz+0.5f)*dzdy,
            0, ny, 0,
            150, 150, 150,
            dx+0.5f, (dz+0.5f)*dzdy) // texture coordinates should tile wall
            .insertIntoBuf(vBuf)
        }
      }
    }
  }

  def populateIdxBuffer(iBuf: ByteBuffer) =
    (0 until nIdxs).foreach(i => iBuf.putInt(i))
}

class Level(val name: String) extends VBOModelEntity {
  val model = new LevelModel(name)

  // origin of the model in WORLD SPACE
  var x = 0f
  var y = 0f
  var z = 0f

  def height(x: Double, y: Double) = model.height(x, y)
  def inBounds(x: Double, y: Double) = model.inBounds(x, y)
  def zScale = model.zScale

  override def traits() = List("level", "render", "update")
}
