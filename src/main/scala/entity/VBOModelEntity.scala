package edu.stanford.cs248.project.entity

import org.lwjgl.opengl._

trait VBOModelEntity extends Entity { 
  def model: VBOModel
  
  // origin of the model in WORLD SPACE
  def x = 0
  def y = 0
  def z = 0
  
  // TODO: add stuff about rotation, shear, etc., etc.
  override def renderGL() = {
    import GL11._
    
    glPushMatrix()
    glMatrixMode(GL11.GL_MODELVIEW)
    glTranslatef(x, y, z)
    
    model.drawCall()
    
    glPopMatrix()
  }
}
