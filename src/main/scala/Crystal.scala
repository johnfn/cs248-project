package edu.stanford.cs248.project

import scala.math._

import org.lwjgl.opengl._
import java.nio.ByteBuffer

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.util._

import org.lwjgl._
import input._
import Keyboard._
import scala.math._

class SquareModel(val x: Float, val y: Float, val z: Float, val color: List[Int]) extends VBOModel {

	val nVerts : Int = 4
	val nIdxs : Int = 4 // number of vertex indices in index buffer
	val name = "cubemodel"

	override def populateVerBuffer(vBuf: ByteBuffer) = {
	    val floorCorners = Array((-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

	    floorCorners.foreach { case(dx, dy) =>
			Vertex(x + dx, y + dy, z,
				  0, 0, 1,
				  color(0), color(1), color(2),
				  dx + 0.5f, dy + 0.5f).insertIntoBuf(vBuf)
			}
	}

	override def populateIdxBuffer(iBuf: ByteBuffer) = {
		(0 until 4).foreach(iBuf.putInt(_))
	}
}

class Crystal(val x: Float, val y: Float, val z: Float) extends VBOModelEntity {
	val model = new SquareModel(x, y, z, List(0, 0, 250))

	override def traits() = List("render", "update", "crystal")
}