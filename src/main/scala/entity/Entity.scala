package edu.stanford.cs248.project.entity

import edu.stanford.cs248.project.opengl._
import edu.stanford.cs248.project._
import org.lwjgl.opengl._
import org.lwjgl.input._

trait Entity {
  def update(m: EntityManager) = {}

  var initGLDone = false
  var (x, y, z) = (0.0f, 0.0f, 0.0f)
  var visible = true
  var flickerCounter: Int = 0
  var goingToDie: Boolean = false

  def checkInit() = if(!initGLDone) {
    doInitGL()
    initGLDone = true
  }

  def flicker() = {
    flickerCounter = 300
  }

  // This is the 'right' way to kill an in-game object.
  def kill() = {
    flickerCounter = 300
    goingToDie = true
  }

  // This instantly removes the current entity from the game.
  def remove(m: EntityManager) = {
    m.entities = m.entities.remove(_ == this)
  }

  // Generic method called before update() on each entity.

  def pre_update(m: EntityManager) = {
    val FLICKER_SPEED = 10 // bigger == slower

    if (flickerCounter > 0) {
      flickerCounter -= 1

      visible = ((flickerCounter / 10) % 2 == 0)
    } else {
      if (goingToDie) {
        remove(m)
      } else {
        visible = true
      }
    }
  }

  def traits() = List("render", "update")
  def doInitGL() = {}
  def renderGL(shader: Shader) = {}
  def deleteGL() = {}
}

class EntityManager {
  var entities: List[Entity] = List()

  def add(e: Entity) = {
    e.checkInit()
    entities = e :: entities
  }

  /* Returns the ray from the camera's eye coordinate through the position of
     the mouse. Returns a tuple: the origin position of the ray and a normalized
     vector of the direction of the ray. */

  private def getPickRay() = {
    import org.lwjgl.util._
    import org.lwjgl.util.vector._

    val camera:Camera = entities.filter(_.traits.contains("camera")).head.asInstanceOf[Camera]

    val mouseX: Float = Mouse.getX().asInstanceOf[Float]
    val mouseY: Float = Mouse.getY().asInstanceOf[Float]

    val view: Matrix4f = camera.viewMatrix

    val aspectRatio: Double = Main.WIDTH.asInstanceOf[Float] / Main.HEIGHT.asInstanceOf[Float]
    val viewRatio: Double = Math.tan(Math.toRadians(90.0) / 2.0) //90 degrees

    //get the mouse position in screenSpace coords
    val screenSpaceX: Double = (mouseX / (Main.WIDTH  / 2) - 1.0f) * aspectRatio * viewRatio
    val screenSpaceY: Double = (mouseY / (Main.HEIGHT / 2) - 1.0f) * viewRatio

    val NearPlane: Double = camera.nearClip
    val FarPlane: Double = camera.farClip

    //Find the far and near camera spaces
    var cameraSpaceNear: Vector4f = new Vector4f( (screenSpaceX * NearPlane).asInstanceOf[Float],  (screenSpaceY * NearPlane).asInstanceOf[Float],  (-NearPlane).asInstanceOf[Float], 1);
    var cameraSpaceFar: Vector4f = new Vector4f( (screenSpaceX * FarPlane).asInstanceOf[Float],  (screenSpaceY * FarPlane).asInstanceOf[Float],  (-FarPlane).asInstanceOf[Float], 1);

    //Unproject the 2D window into 3D to see where in 3D we're actually clicking

    //TODO: If this ends up being right, go correct incorrect SO answer.
    var invView: Matrix4f = (new Matrix4f(view)).invert().asInstanceOf[Matrix4f]
    var worldSpaceNear: Vector4f = new Vector4f();
    Matrix4f.transform(invView, cameraSpaceNear, worldSpaceNear);

    var worldSpaceFar: Vector4f = new Vector4f();

    Matrix4f.transform(invView, cameraSpaceFar, worldSpaceFar);

    //calculate the ray position and direction
    val rayPosition: Vector3f = new Vector3f(worldSpaceNear.x, worldSpaceNear.y, worldSpaceNear.z);
    val rayDirection: Vector3f = new Vector3f(worldSpaceFar.x - worldSpaceNear.x, worldSpaceFar.y - worldSpaceNear.y, worldSpaceFar.z - worldSpaceNear.z);

    rayDirection.normalise()

    (rayPosition, rayDirection)
  }

  // If coord is true, return the map coordinates the mouse is over or None.
  // If coord is false, return the entity the mouse is over or None.
  private def pick(coord: Boolean): Option[Any] = {
    var (pos, dir) = getPickRay()
    val b:List[Block] = entities.filter(_.traits.contains("moveable")).map{ _.asInstanceOf[Block] }
    val lv:Level = entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]

    // TODO (?) This method for checking ray/box collisions is pretty darn
    // slow. If we are finding that the program is going really slowly, this
    // could be a candidate for easy improvement (just drop a better collision
    // test in here).

    // Step along the ray from the mouse position into the screen, stopping as
    // soon as we find a collision with any in-game object.
    for (x <- 0 until 1000) {
      pos.x += dir.x / 10.0f
      pos.y += dir.y / 10.0f
      pos.z += dir.z / 10.0f

      if (x < 0) return None

      if (coord) {
        val res = lv.intersect(pos.x, pos.y, pos.z)
        if (res.isDefined) return res
      } else {
        b.filter(_.intersect(pos.x, pos.y, pos.z)).foreach { block =>
          return Some(block)
        }
      }
    }

    None
  }

  // Find the highest object at (x, y) that we can stand on, not considering
  // `ignoring`.  We'll need to ignore at least the object that's interested
  // in what this object is, since it can't stand on itself.
  def height(x: Float, y: Float, ignoring: Entity) = {
    val lv: Level = entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]
    var highest = lv.height(x, y)

    entities.filter(ent => ent.x == x && ent.y == y).map {ent =>
      if (ent.z > highest && ignoring != ent) {
        highest = ent.z
      }
    }

    highest
  }

  // Get the coordinate that the mouse is hovering over or None.
  def pickCoordinate(): Option[Tuple2[Int, Int]] = {
    pick(true).asInstanceOf[Option[Tuple2[Int, Int]]]
  }

  // Get the Entity that the mouse is hovering over or None.
  def pickEntity() = {
    pick(false).asInstanceOf[Option[Entity]]
  }

  def updateAll() = {
    entities.filter(e => e.traits.contains("update")).foreach{ent =>
      ent.pre_update(this)
      if (!ent.goingToDie) {
        ent.update(this)
      }
    }
  }

  def renderAll(shader: Shader) = {
    import GL11._

    entities.filter(_.traits.contains("render")).foreach{obj =>
      if (obj.visible) {
        glPushMatrix()
        obj.renderGL(shader)
        glPopMatrix()
      }
    }
  }
}
