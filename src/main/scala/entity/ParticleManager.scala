package edu.stanford.cs248.project.entity

import edu.stanford.cs248.project.opengl._
import edu.stanford.cs248.project._
import org.lwjgl.opengl._
import org.lwjgl.input._

class ParticleManager {
  var entities: List[Particle] = List()

  def add(e: Particle) = {
    e.checkInit()
    entities = e :: entities
  }

  def updateAll() = {
    entities.foreach { ent =>
      ent.particle_update()
    }
  }

  def renderAll(cam: Camera, shader: Shader) = {
    import GL11._

    glEnable(GL_BLEND)
    glDepthMask(false)

    entities.foreach { part =>
      glPushMatrix()
      glLoadIdentity()

      glMultMatrix(cam.facingCamMat())

      part.renderGL(shader)
      glPopMatrix()
    }

    glDisable(GL_BLEND)
    glDepthMask(true)
  }
}
