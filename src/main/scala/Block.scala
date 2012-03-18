package edu.stanford.cs248.project

import scala.math._

import org.lwjgl.opengl._
import java.nio.ByteBuffer

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.opengl._

import org.lwjgl._
import input._
import Keyboard._
import scala.math._
import com.threed.jpct._
import com.threed.jpct.util._
import math._

/* Moveable by the gravity gun. */
trait Moveable extends Entity {
	def setPosition(m: EntityManager, newx: Float, newy: Float) = {
	    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]

		x = newx
		y = newy
		z = lv.height(x, y)
	}
}

class CubeModel(val x: Float, val y: Float, val z: Float) extends VBOModel {
	val name = "cubemodel"

	override def getVertices() = {
		var vertices = List(
		    // Front face
	    	List(
			    List(-1.0f, -1.0f,  1.0f),
			    List( 1.0f, -1.0f,  1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List(-1.0f,  1.0f,  1.0f)
	    	),

		    // Back face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List(-1.0f,  1.0f, -1.0f),
			    List( 1.0f,  1.0f, -1.0f),
			    List( 1.0f, -1.0f, -1.0f)
		    ),

		    // Top face
		    List(
			    List(-1.0f,  1.0f, -1.0f),
			    List(-1.0f,  1.0f,  1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List( 1.0f,  1.0f, -1.0f)
		    ),

		    // Bottom face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List( 1.0f, -1.0f, -1.0f),
			    List( 1.0f, -1.0f,  1.0f),
			    List(-1.0f, -1.0f,  1.0f)
		    ),

		    // Right face
		    List(
			    List( 1.0f, -1.0f, -1.0f),
			    List( 1.0f,  1.0f, -1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List( 1.0f, -1.0f,  1.0f)
		    ),

		    // Left face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List(-1.0f, -1.0f,  1.0f),
			    List(-1.0f,  1.0f,  1.0f),
			    List(-1.0f,  1.0f, -1.0f)
		    )
		  );

		vertices.map(face =>
			face.map(vertex =>
				Vertex(vertex(0) / 2.0f, vertex(1) / 2.0f, vertex(2) / 2.0f + 0.5f,
					   0, 0, 1,
					   0, 0))).flatten(identity)
	}

	override def getIndices() = 0 until 24
}

class Block(block_x: Float, block_y: Float, block_z: Float) extends VBOModelEntity with Moveable {
	x = block_x
	y = block_y
	z = block_z

	val model = new CubeModel(x, y, z)
	val width = 0.5f

	// TODO: Could emit particles.
	def select(selected: Boolean) = {
		if (selected) {
			z = 2.0f
		} else {
			z = 0.0f
		}
	}

	def intersect(px: Float, py: Float, pz: Float) = {
		(px > x - width) && (px < x + width) &&
		(py > y - width) && (py < y + width) &&
		(pz > z) && (pz < z + 2.0f * width) //TODO - actual z value
	}

	override def traits() = List("render", "update", "moveable")
}