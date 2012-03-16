package edu.stanford.cs248.project.opengl

import org.lwjgl.opengl._
import java.nio._

import GL11._
import GL14._
import GL20._
import GL30._
import EXTFramebufferObject._

trait Fbo {
  import EXTFramebufferObject._ 
  
  def fboId: Int
  def bind()
    
  def newTex(w: Int, h: Int, fmt: Int) = {
    val tex = new BlankTexture(w, h, fmt, GL_UNSIGNED_BYTE)
    tex.init()
    tex
  }
}

object screenFbo extends Fbo {
  def fboId = 0
  
  def bind() = {
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId)
  }
}

// Multiple render targets using GL_FLOAT as data type
class MrtFloatFbo(nTargets: Int, w: Int, h: Int) extends Fbo {  
  var fboId = 0
  
  var depthTex : Texture = null
  var colorTexAry : Array[Texture] = null
  
  val drawBuffers = {
    val res = 
      ByteBuffer.allocateDirect(nTargets*4).order(ByteOrder.nativeOrder())
    (0 until nTargets).map(i =>
      res.putInt(GL_COLOR_ATTACHMENT0_EXT+i))
    res.rewind()
    res.asIntBuffer()
  }
  
  def bind() = {
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId)
    glDrawBuffers(drawBuffers)
  }
  
  def init() = {
    fboId = glGenFramebuffersEXT()
    bind()
      
    depthTex = newTex(w, h, GL_DEPTH_COMPONENT)
    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT,
      GL_TEXTURE_2D, depthTex.id, 0)
    
    colorTexAry = (0 until nTargets).toArray.map { i =>
      val tex = newTex(w, h, GL_RGBA)
      glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT+i, 
        GL_TEXTURE_2D, tex.id, 0)
      tex
    }
    
    if( glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != 
        GL_FRAMEBUFFER_COMPLETE_EXT)
    {
      throw new 
        RuntimeException("FBO with %d targets didn't initialize. Code: %d"
          .format(nTargets, glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT)))
    }
  }
  
}

class SimpleFbo(w: Int, h: Int, fmt: Int) extends Fbo {  
  var fboId = 0
  
  var tex : Texture = null
  
  def bind() = {
    glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId)
  }
  
  def init() = {
    fboId = glGenFramebuffersEXT()
    bind()
    
    tex = {
      val tex = newTex(w, h, fmt)
      glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, 
        GL_TEXTURE_2D, tex.id, 0)
      tex
    }
    
    if( glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != 
        GL_FRAMEBUFFER_COMPLETE_EXT)
    {
      throw new 
        RuntimeException("SimpleFBO didn't initialize. Code: %d"
          .format(glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT)))
    }
  }
  
}
