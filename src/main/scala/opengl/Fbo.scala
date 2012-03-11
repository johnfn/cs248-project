package edu.stanford.cs248.project.opengl

import org.lwjgl.opengl._

trait Fbo {
  import EXTFramebufferObject._ 
  
  def fboId: Int 
  def use() = {
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId)
  }
}

object screenFbo extends Fbo {
  def fboId = 0
}

// Multiple render targets using GL_FLOAT as data type
class MrtFloatFbo(nTargets: Int, w: Int, h: Int) extends Fbo {
  import GL11._
  import GL14._
  import EXTFramebufferObject._
  
  val fboId = glGenFramebuffersEXT()
  
  var depthTex : Texture = null
  var colorTexAry = new Array[Texture](nTargets)
  
  def init() = {
    use()
    
    def newTex(fmt: Int) = new BlankTexture(w, h, fmt, GL_FLOAT)
    
    depthTex = newTex(GL_DEPTH_COMPONENT24)
    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,
      GL_TEXTURE_2D, depthTex.id, 0)
    
    colorTexAry = (0 to nTargets).toArray.map { i =>
      val tex = newTex(GL_RGBA)
      glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT+i, 
        GL_TEXTURE_2D, tex.id, 0)
      tex
    }
    
    if( glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != 
        GL_FRAMEBUFFER_COMPLETE_EXT)
    {
      throw new RuntimeException("FBO with %i targets didn't initialize."
        .format(nTargets))
    }
  }
  
}
