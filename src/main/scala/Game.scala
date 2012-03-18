package edu.stanford.cs248.project

import org.lwjgl.opengl._
import org.lwjgl.input.Keyboard._
import org.lwjgl.input._
import GL11._
import GL20._

import math._
import scala.util.control.Breaks._

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.opengl._

object Main {
  val GAME_TITLE = "My Game"
  val FRAMERATE = 60
  val WIDTH = 1280
  val HEIGHT = 720

  val camera = new Camera()
  val manager = new EntityManager()

  val gbufFbo = new MrtFloatFbo(3, WIDTH, HEIGHT)
  val ssaoFbo = new SimpleFbo(WIDTH/2, HEIGHT/2, GL_RGB, GL_RGB)
  val blurXFbo = new SimpleFbo(WIDTH/2, HEIGHT/2, GL_RGB, GL_RGB)
  val blurYFbo = new SimpleFbo(WIDTH/2, HEIGHT/2, GL_RGB, GL_RGB)
  val finalFbo = new SimpleFbo(WIDTH, HEIGHT, GL_RGBA, GL_RGBA)

  val gbufShader = new Shader("gbufs", "gbufs")
  val testShader = new Shader("minimal", "test")
  val ssaoShader = new Shader("minimal", "ssao")
  val blurXShader = new Shader("minimal", "blurX")
  val blurYShader = new Shader("minimal", "blurY")
  val finalShader = new Shader("minimal", "final")

  var curLevel : Level = null

  def main(args:Array[String]) = {
    var fullscreen = false
    for(arg <- args){
      arg match{
        case "-fullscreen" =>
          fullscreen = true
      }
    }

    init(fullscreen)
    addObjects()
    run()
    gameOver()
  }

  def init(fullscreen:Boolean) = {
    Display.setTitle(GAME_TITLE)
    Display.setFullscreen(fullscreen)
    //Display.setVSyncEnabled(true)
    Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT))
    Display.create()

    GLContext.useContext()
    if(!GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
      println("OpenGL context doesn't support VBOs.")
      System.exit(-1)
    }

    if(!GLContext.getCapabilities().GL_EXT_framebuffer_object) {
      println("OpenGL context doesn't support FBOs.")
      System.exit(-1)
    }

    def loadShader(shader: Shader) = if(!shader.init()) {
      println("""Shader "%s/%s" initialization failed"""
        .format(shader.vertName, shader.fragName))
      System.exit(-1)
    } else {
      shader.use()
    }

    List(
      gbufShader, testShader, ssaoShader, blurXShader, blurYShader, finalShader)
      .foreach(loadShader)

    gbufFbo.init()
    ssaoFbo.init()
    blurXFbo.init()
    blurYFbo.init()
    finalFbo.init()

    glViewport(0, 0, WIDTH, HEIGHT)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
  }

  def addObjects() = {
    val ghost = new Ghost()

    curLevel = new Level("level1")

    manager.add(camera)
    manager.add(curLevel)
    manager.add(new Crystal(3.0f, 3.0f, 0.0f))
    manager.add(ghost)
    manager.add(new Protagonist(ghost))
    manager.add(new Block(8, 8, 0))
  }

  def gameOver() = {
    Display.destroy()
    System.exit(0)
  }

  // Tries to find an object that the user is mousing over.
  def pickAnObject(m: EntityManager) {
    var (pos, dir) = getPickRay()
    val b:Block = m.entities.filter(_.traits.contains("block")).head.asInstanceOf[Block]

    dir.normalise()

    println("pos: " + pos)
    println("dir: " + dir)

    for (x <- 0 until 10000) {
      pos.x += dir.x / 100.0f
      pos.y += dir.y / 100.0f
      pos.z += dir.z / 100.0f

      if (b.intersect(pos.x, pos.y, pos.z)) {
        b.select(true)
        println("You're mousing over the block")
        return
      }
    }
    println("final pos: " + pos)
    b.select(false)
  }

  def updateGame() = {
    ViewMode.update()

    pickAnObject(manager)

    manager.updateAll()
  }

  def renderGame() = {
    // Render G - buffers
    gbufFbo.bind()
    gbufShader.use()

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    camera.loadGLMatrices()
    camera.passInUniforms(gbufShader)

    manager.renderAll(gbufShader)

    // Render SSAO pass
    ssaoFbo.bind()
    ssaoShader.use()

    camera.passInUniforms(ssaoShader)
    camera.putModelViewMatrixIntoTextureMat(0)
    ViewMode.bindGBufs(ssaoShader)

    drawQuad(ssaoShader)
    /* */
    // Render Blur X pass
    blurXFbo.bind()
    blurXShader.use()
    ssaoFbo.tex.bindAndSetShader(0, blurXShader, "texInp");
    ViewMode.bindTexelSizes(blurXShader)
    drawQuad(blurXShader)

    // Render Blur Y pass
    blurYFbo.bind()
    blurYShader.use()
    blurXFbo.tex.bindAndSetShader(0, blurYShader, "texInp");
    ViewMode.bindTexelSizes(blurYShader)
    drawQuad(blurYShader)

    // Render final shader
    finalFbo.bind()
    finalShader.use()
    ViewMode.bindGBufs(finalShader)
    //ssaoFbo.tex.bindAndSetShader(3, finalShader, "ssaoBuf");
    blurYFbo.tex.bindAndSetShader(3, finalShader, "ssaoBuf");

    camera.passInUniforms(finalShader)
    camera.putModelViewMatrixIntoTextureMat(0)

    camera.loadGLMatrices()
    curLevel.setLights()

    drawQuad(finalShader)

    // Render Screen
    screenFbo.bind()
    testShader.use()
    ViewMode.bindForTestShader(testShader)

    drawQuad(testShader)
  }

  def drawQuad(shader: Shader) = {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0, 1, 0, 1, 1, -1)
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()

    val texCoordLoc = glGetAttribLocation(shader.id, "texcoordIn")

    glBegin(GL_QUADS)
      glVertexAttrib2f(texCoordLoc, 0, 0)
      glVertex3f(0, 0, 0)
      glVertexAttrib2f(texCoordLoc, 1, 0)
      glVertex3f(1, 0, 0)
      glVertexAttrib2f(texCoordLoc, 1, 1)
      glVertex3f(1, 1, 0)
      glVertexAttrib2f(texCoordLoc, 0, 1)
      glVertex3f(0, 1, 0)
    glEnd()
  }

  def getActualEyePosition() = {
    import org.lwjgl.util._
    import org.lwjgl.util.vector._

    var rotatedEye : Vector4f = camera.eye()
    rotatedEye.x -= camera.lookAt().x * 2;
    rotatedEye.y -= camera.lookAt().y * 2;
    rotatedEye.z -= camera.lookAt().z * 2;
    //rotatedEye = rotatedEye.rotate(getRotation()); //TODO......
    new Vector3f(-rotatedEye.x, -rotatedEye.y, rotatedEye.z);
  }

  def generateViewMatrix() = {
    import org.lwjgl.util._
    import org.lwjgl.util.vector._
      var viewMatrix: Matrix4f = new Matrix4f()
      var actualEye: Vector3f = getActualEyePosition()
      var side: Vector3f = new Vector3f()
      var forward = new Vector3f( camera.lookAt().x - camera.eye().x
                                , camera.lookAt().y - camera.eye().y
                                , camera.lookAt().z - camera.eye().z)

      println(camera.lookAt())
      println(camera.eye())
      println(forward)

      var up = camera.up()
      forward.normalise();

      /* Side = forward x up */
      Vector3f.cross(forward, up, side);
      println(side)
      side.normalise();

      /* Recompute up as: up = side x forward */
      Vector3f.cross(side, forward, up);

      viewMatrix.m00 = side.x;
      viewMatrix.m10 = side.y;
      viewMatrix.m20 = side.z;
      viewMatrix.m30 = -(Vector3f.dot(side, actualEye));

      viewMatrix.m01 = up.x;
      viewMatrix.m11 = up.y;
      viewMatrix.m21 = up.z;
      viewMatrix.m31 = -(Vector3f.dot(up, actualEye));

      viewMatrix.m02 = -forward.x;
      viewMatrix.m12 = -forward.y;
      viewMatrix.m22 = -forward.z;
      viewMatrix.m32 = -(Vector3f.dot(forward, actualEye));

      viewMatrix
  }

  def getPickRay() = {
    import org.lwjgl.util._
    import org.lwjgl.util.vector._
    val mouseX: Float = Mouse.getX().asInstanceOf[Float]
    val mouseY: Float = Mouse.getY().asInstanceOf[Float]

    val view: Matrix4f = generateViewMatrix()

    val aspectRatio: Double = WIDTH.asInstanceOf[Float] / HEIGHT.asInstanceOf[Float]
    val viewRatio: Double = Math.tan(Math.toRadians(90.0) / 2.0) //90 degrees
    //val viewRatio: Double = Math.tan(scala.Math.Pi / 4.0f / 2.00f)

    //get the mouse position in screenSpace coords
    val screenSpaceX: Double = (mouseX / (WIDTH / 2)  - 1.0f) * aspectRatio * viewRatio
    val screenSpaceY: Double = (mouseY / (HEIGHT / 2) - 1.0f) * viewRatio

    val NearPlane: Double = 0.1;
    val FarPlane: Double = 60.0;

    //Find the far and near camera spaces
    var cameraSpaceNear: Vector4f = new Vector4f( (screenSpaceX * NearPlane).asInstanceOf[Float],  (screenSpaceY * NearPlane).asInstanceOf[Float],  (-NearPlane).asInstanceOf[Float], 1);
    var cameraSpaceFar: Vector4f = new Vector4f( (screenSpaceX * FarPlane).asInstanceOf[Float],  (screenSpaceY * FarPlane).asInstanceOf[Float],  (-FarPlane).asInstanceOf[Float], 1);


    //Unproject the 2D window into 3D to see where in 3D we're actually clicking

    //TODO: If this ends up being right, go correct incorrect SO answer.
    var tmpView: Matrix4f = new Matrix4f(view);
    var invView: Matrix4f = tmpView.invert().asInstanceOf[Matrix4f]
    var worldSpaceNear: Vector4f = new Vector4f();
    Matrix4f.transform(invView, cameraSpaceNear, worldSpaceNear);

    var worldSpaceFar: Vector4f = new Vector4f();

    Matrix4f.transform(invView, cameraSpaceFar, worldSpaceFar);

    //calculate the ray position and direction
    val rayPosition: Vector3f = new Vector3f(worldSpaceNear.x, worldSpaceNear.y, worldSpaceNear.z);
    val rayDirection: Vector3f = new Vector3f(worldSpaceFar.x - worldSpaceNear.x, worldSpaceFar.y - worldSpaceNear.y, worldSpaceFar.z - worldSpaceNear.z);

    rayDirection.normalise();

    rayPosition.z = -rayPosition.z;

    (rayPosition, rayDirection);
  }


  def run() = {
    val fpsPrintInterval = 5000;
    var lastPrintTime = System.nanoTime()/1000000
    var framesDrawnSinceLastPrint = 0

    while(!(isKeyDown(KEY_ESCAPE) || Display.isCloseRequested)) {
      //updateMouseCoords()
      updateGame()
      Display.update()
      renderGame()
      //Display.sync(FRAMERATE)
      framesDrawnSinceLastPrint += 1

      val tNow = System.nanoTime()/1000000
      if(tNow > lastPrintTime+fpsPrintInterval) {
        println("FPS: %f"
          .format(framesDrawnSinceLastPrint.toDouble/(tNow-lastPrintTime)*1000))
        lastPrintTime = tNow
        framesDrawnSinceLastPrint = 0
      }
    }
  }
}

object ViewMode {
  var lastKey = KEY_1

  def associations : List[(Int, Texture, Boolean)] = {
    import Main._
    List(
      (KEY_1, finalFbo.tex, false), // standard view
      (KEY_2, gbufFbo.colorTexAry(0), false), // normal
      (KEY_3, gbufFbo.colorTexAry(0), true),  // zEye buffer
      (KEY_4, gbufFbo.colorTexAry(1), false), // diffuse texture
      (KEY_5, gbufFbo.colorTexAry(2), false), // specular texture
      (KEY_6, ssaoFbo.tex, false),
      (KEY_7, blurXFbo.tex, false),
      (KEY_8, blurYFbo.tex, false)
    )
  }

  def update() = associations.foreach {
    case (key, _, _) => if(isKeyDown(key)) lastKey = key
  }

  def bindForTestShader(shader: Shader) = {
    val (_, tex, showW) = associations.find(_._1 == lastKey).get

    tex.bindAndSetShader(0, shader, "texture")
    glUniform1i(glGetUniformLocation(shader.id, "showW"), if(showW) 1 else 0)
  }

  def bindGBufs(shader: Shader) = {
    Main.gbufFbo.colorTexAry.zipWithIndex.map {
      case (tex, texUnit) => tex.bind(texUnit)
    }

    List("nmlGbuf", "difGbuf", "spcGbuf").zipWithIndex.map {
      case (name, texUnit) =>
        glUniform1i(glGetUniformLocation(shader.id, name), texUnit)
    }

    //Main.gbufFbo.depthTex.bindAndSetShader(3, shader, "zBuf")
  }

  def bindTexelSizes(shader: Shader) = {
    // since we are rendering at half-res
    glUniform1f(glGetUniformLocation(shader.id, "texelX"),
      2.0f/(Main.WIDTH.toFloat))
    glUniform1f(glGetUniformLocation(shader.id, "texelY"),
      2.0f/(Main.HEIGHT.toFloat))
  }
}
