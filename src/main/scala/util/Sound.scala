package edu.stanford.cs248.project.util

import javax.sound.sampled._

import scala.concurrent.ops._

object Sound {
  def playSnd(name: String) = spawn {
    try {
      val clip = AudioSystem.getClip()
      
      val inputStream = AudioSystem.getAudioInputStream(
        getClass.getResource("/sounds/%s.wav".format(name)))
      
      clip.open(inputStream)
      clip.start()
    } catch {
      case e : Exception => 
        println("Problem playing sound %s".format(name))
        println(e.getMessage)
    }
  }
}
