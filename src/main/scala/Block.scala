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
	var selected = false

	def setPosition(m: EntityManager, newx: Float, newy: Float) = {
	    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]

	    if (x != newx || y != newy) {
			x = newx
			y = newy
			z = m.height(x, y, this) + 1.0f
		}
	}

	// TODO: Could emit particles.
	def select(just_selected: Boolean) = {
		selected = just_selected
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
				Vertex(vertex(0) / 2.0f, vertex(1) / 2.0f, vertex(2) / 2.0f - 0.5f,
					   0, 0, 1,
					   0, 0))).flatten(identity)
	}

	override def getIndices() = 0 until 24
}

class Block(block_x: Float, block_y: Float, block_z: Float) extends VBOModelEntity with Moveable {
	val WIDTH = 0.5f

	x = block_x
	y = block_y
	z = block_z + 1.0f

	val model = new SkyModel(x, y, z, 1.0f, "/textures/skybox.jpg")

	override def update(m: EntityManager) = {
		if (!selected) {
			// Fall onto the block directly below this one.
		    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]
			var lowest = lv.height(x, y)
			m.entities.filter(e => e.x == x && e.y == y && e.z < z && e != this).map { e =>
				if (e.z > lowest) {
					lowest = e.z
				}
			}

			lowest += WIDTH * 2

			if (z >= lowest) {
				z -= 0.02f;
				if (z < lowest) {
					z = lowest
				}
			}
		}
	}

	def intersect(px: Float, py: Float, pz: Float) = {
		(px > x - WIDTH) && (px < x + WIDTH) &&
		(py > y - WIDTH) && (py < y + WIDTH) &&
		(pz < z) && (pz > z - 2.0f * WIDTH)
	}

	override def traits() = List("render", "update", "moveable")
}