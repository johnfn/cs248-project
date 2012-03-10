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

class SquareModel(val x: Float, val y: Float, val z: Float) extends VBOModel {

	val name = "cubemodel"

	override def getVertices() = {
	    val floorCorners = Array((-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

	    floorCorners.map { case(dx, dy) =>
			Vertex(dx, dy, z,
				  0, 0, 1,
				  dx + 0.5f, dy + 0.5f)
			}
	}

	override def getIndices() = (0 until 4)
}

class Crystal(val x: Float, val y: Float, val z: Float) extends VBOModelEntity {
	val model = new SquareModel(x, y, z) // old color: , List(0, 0, 250))

	override def traits() = List("render", "update", "crystal")
}