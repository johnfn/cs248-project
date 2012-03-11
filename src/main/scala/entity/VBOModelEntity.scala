package edu.stanford.cs248.project.entity

import org.lwjgl.opengl._
import edu.stanford.cs248.project.util._

trait VBOModelEntity extends Entity { 
  def model: VBOModel
  
  def x : Float
  def y : Float
  def z : Float
  
  // TODO: add stuff about rotation, shear, etc., etc.
  override def renderGL(shader: Shader) = {
    import GL11._
    
    glPushMatrix()
    glMatrixMode(GL11.GL_MODELVIEW)
    glTranslatef(x, y, z)
    
    model.drawCall(shader)
    
    glPopMatrix()
  }
}
