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

class Crystal(val x: Float, val y: Float, val z: Float) extends Entity{
	//val model = new SquareModel(x, y, z) // old color: , List(0, 0, 250))
	var plant:Object3D = null;
	var polymanager:PolygonManager = null;

	override def doInitGL() = {

		/*
		Texture tex=new Texture("textures"+File.separatorChar+"plant.jpg");
		TextureManager.getInstance().addTexture("plant", tex);

		Texture tex2=new Texture("textures"+File.separatorChar+"plant2.jpg");
		TextureManager.getInstance().addTexture("plant2", tex2);
		*/

		var objs:Array[Object3D] = Loader.load3DS("models/plant.3ds",2.5f);
		if (objs.length==1) {
			plant=objs(0)
			plant.setTexture("plant")
			plant.setTransparency(2)
			plant.setCulling(Object3D.CULLING_DISABLED)
			plant.rotateX(-3.14159f/2f)
			plant.rotateMesh()
			plant.setRotationMatrix(new Matrix())
			//plant.setAdditionalColor(new Color(100,100,100))
			plant.build()
			polymanager = plant.getPolygonManager()
		} else {
			println("Error loading crystal.")
			System.exit(-1)
		}
	}

	override def renderGL(shader: Shader) = {
		import GL11._

		glColor3f(1, 0, 0)

		glBegin(GL_TRIANGLES)
			for (poly <- 0 until polymanager.getMaxPolygonID(); vert <- 0 until 3) {
				val sv:SimpleVector = polymanager.getTransformedVertex(poly, vert)
				glVertex3f(sv.x, sv.y, sv.z)
				println(poly + " " + vert)
			}
		glEnd()

	}

	override def traits() = List("render", "update", "crystal")
}