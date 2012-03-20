package edu.stanford.cs248.project.util

import javax.sound.sampled._

import scala.concurrent.ops._

object Sound {
  def playSnd(name: String) = spawn {
    try {
      val inputStream = AudioSystem.getAudioInputStream(
        getClass.getResource("/sounds/%s.wav".format(name)))
      
      val format = inputStream.getFormat()
      val info = new DataLine.Info(classOf[Clip], format)
      
      val clip = AudioSystem.getLine(info).asInstanceOf[Clip]
      
      clip.open(inputStream)
      clip.start()
      clip.close()
    } catch {
      case e : Exception => 
        println("Problem playing sound %s".format(name))
        println(e.getMessage)
    }
  }
}
