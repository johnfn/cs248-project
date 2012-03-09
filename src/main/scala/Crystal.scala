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

class CubeModel(x: Float, y: Float, z: Float) extends VBOModel {
	override def populateVerBuffer(vBuf: ByteBuffer) = {
	    val floorCorners = Array((-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

	    floorCorners.foreach { case(dx, dy) =>
			Vertex(x + dx, y + dy, z,
				  0, 0, 1,
				  0, 0, 250, //blue
				  dx + 0.5f, dy + 0.5f).insertIntoBuf(vBuf)
			}
	}

	override def populateIdxBuffer(iBuf: ByteBuffer) = {
		(0 until 4).foreach(iBuf.putInt(_))
	}
}

class Crystal() extends VBOModelEntity {
	val x : Float = 25.0f
	val y : Float = 25.0f
	val z : Float = 1.0f
	val model = new CubeModel(x, y, z)

	override def traits() = List("render", "update", "crystal")
}