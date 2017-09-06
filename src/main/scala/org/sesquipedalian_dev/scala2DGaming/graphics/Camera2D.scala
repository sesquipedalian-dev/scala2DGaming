/**
  * Copyright 2017 sesquipedalian.dev@gmail.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.sesquipedalian_dev.scala2DGaming.graphics

import java.nio.{FloatBuffer, IntBuffer}

import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.opengl.GL20.{glGetUniformLocation, glUniformMatrix4fv}
import org.lwjgl.system.MemoryStack
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.input.InputHandler
import org.sesquipedalian_dev.scala2DGaming.util.{ThrowsExceptionOnGLError, cleanly}
import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11.glViewport

class UICamera(
  worldWidth: Int,
  worldHeight: Int,
  worldCameraScale: Int
) extends Camera2D(worldWidth, worldHeight, worldCameraScale) {
  override def setCamera(requestedXTranslate: Float = 0, requestedYTranslate: Float = 0, _zoom: Float = 2f): Unit = {
//     UI camera doesn't allow zoom or translate
  }

  override def handleInput(key: Int): Boolean = {
//     UI camera doesn't allow zoom or translate
    false
  }

  // UI camera doesn't use aspect ratio for scale
  override def updateScreenSize(projectionUniformName: String): Unit = {
    // get screen height / width for ortho projection
    var width: Float = 0f
    var height: Float = 0f
    cleanly(MemoryStack.stackPush())(stack => {
      val wh = glfwGetCurrentContext()
      val w: IntBuffer = stack.mallocInt(1)
      val h: IntBuffer = stack.mallocInt(1)
      glfwGetFramebufferSize(wh, w, h)
      width = w.get()
      height = h.get()
    })

    val uniProjection = programHandle.map(glGetUniformLocation(_, projectionUniformName))
    checkError()
    val projection = new Matrix4f()
    val cameraXScale = 2f / worldWidth.toFloat / worldCameraScale / zoom
    val cameraYScale = 2f / worldHeight.toFloat / worldCameraScale / zoom
    //    println(s"updating screen size $aspectRatio $cameraXScale, $cameraYScale")
    projection.scale(cameraXScale, -cameraYScale, 1f)
    projection.translate(
      -worldWidth.toFloat * worldCameraScale / 2 + xTranslate,
      -worldHeight.toFloat * worldCameraScale / 2 + yTranslate,
      0f
    )
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      projection.get(buf)
      uniProjection.foreach(glUniformMatrix4fv(_, false, buf))
      checkError()
    })

    // after all the ortho tweaks, we've got a 2d coordinate system where 0,0 is top left of the screen,
    // +x = right, +y = down,
    // and world coords map 1-to-1 to screen coords
  }
}

class Camera2D(
  worldWidth: Int,
  worldHeight: Int,
  worldCameraScale: Int
) extends Renderable
  with InputHandler
  with ThrowsExceptionOnGLError
{
  final val MAX_ZOOM: Float = 1f
  final val CAMERA_TRANSLATE_SPEED: Float = worldCameraScale
  final val CAMERA_ZOOM_SPEED: Float = 2f

  var zoom: Float = MAX_ZOOM
  var xTranslate: Float = 0f
  var yTranslate: Float = 0f

  var programHandle: Option[Int] = None

  // returns current x translate, y translate, and zoom for camera
  def getCamera(): (Float, Float, Float) = (xTranslate, yTranslate, zoom)

  def setCamera(requestedXTranslate: Float = 0, requestedYTranslate: Float = 0, _zoom: Float = 2f): Unit = {
    // get screen height / width for ortho projection
    var width: Float = 0f
    var height: Float = 0f
    cleanly(MemoryStack.stackPush())(stack => {
      val wh = glfwGetCurrentContext()
      val w: IntBuffer = stack.mallocInt(1)
      val h: IntBuffer = stack.mallocInt(1)
      glfwGetFramebufferSize(wh, w, h)
      width = w.get()
      height = h.get()
    })

    // set up zoom range
    val maxZ = MAX_ZOOM
    val minZ = width / (worldCameraScale * worldWidth)
    zoom = math.max(minZ, math.min(_zoom, maxZ))

    // the idea here is to find how many world pixels one screen takes up - then you can move
    // that many screens in world coordinates (minus 1 for the screen starting at the origin
    val numScreensX = 1 / zoom
    val oneScreenX = zoom * worldWidth * worldCameraScale
    val minX = (numScreensX - 1) * oneScreenX / 2
    val maxX = (numScreensX - 1) * oneScreenX / -2

    val aspectRatio = width / height
    val oneScreenY = worldHeight * worldCameraScale * zoom
    val numScreensY = aspectRatio / zoom
    val minY = (numScreensY - 1) * oneScreenY / 2 / aspectRatio
    val maxY = (numScreensY - 1) * oneScreenY / -2 / aspectRatio

//    println(s"SetCamera: zoom=$zoom, min/max X: $minX/$maxX currentX=$xTranslate desiredX=$requestedXTranslate")
//    println(s"SetCamera: zoom=$zoom, min/max Y: $minY/$maxY currentY=$yTranslate desiredY=$requestedYTranslate oneScreen=$oneScreenY numScreens=$numScreensY")

    xTranslate = math.min(minX, math.max(maxX, requestedXTranslate))
    yTranslate = math.min(minY, math.max(maxY, requestedYTranslate))

//    xTranslate = requestedXTranslate
//    yTranslate = requestedYTranslate
  }

  def init(progHandle: Int): Unit = {
    programHandle = Some(progHandle)
  }

  override def render(): Unit = {
  }

  override def handleInput(key: Int): Boolean = {
    var xTranslateDir = 0f
    var yTranslateDir = 0f
    var newZoom = (cur: Float) => cur

    // default action: set application to terminate when escape pressed
    if (key == GLFW_KEY_A) {
      xTranslateDir = 1f
    } else if (key == GLFW_KEY_D) {
      xTranslateDir = -1f
    } else if (key == GLFW_KEY_W) {
      yTranslateDir = 1f
    } else if (key == GLFW_KEY_S) {
      yTranslateDir = -1f
    } else if (key == GLFW_KEY_Q) {
      newZoom = _ * CAMERA_ZOOM_SPEED
    } else if (key == GLFW_KEY_E) {
      newZoom = _ / CAMERA_ZOOM_SPEED
    }

    if(xTranslateDir != 0f || yTranslateDir != 0f || newZoom(zoom) != zoom) {
      setCamera(
        xTranslate + (xTranslateDir * CAMERA_TRANSLATE_SPEED),
        yTranslate + (yTranslateDir * CAMERA_TRANSLATE_SPEED),
        newZoom(zoom)
      )
      true
    } else {
      false
    }
  }

  override def cleanup(): Unit = {
    // Nop
  }

  def updateScreenSize(projectionUniformName: String): Unit = {
    // get screen height / width for ortho projection
    var width: Float = 0f
    var height: Float = 0f
    cleanly(MemoryStack.stackPush())(stack => {
      val wh = glfwGetCurrentContext()
      val w: IntBuffer = stack.mallocInt(1)
      val h: IntBuffer = stack.mallocInt(1)
      glfwGetFramebufferSize(wh, w, h)
      width = w.get()
      height = h.get()
    })

    val uniProjection = programHandle.map(glGetUniformLocation(_, projectionUniformName))
    checkError()
    val projection = new Matrix4f()
    val cameraXScale = 2f / worldWidth.toFloat / worldCameraScale / zoom
    val aspectRatio = width / height
    val cameraYScale = 2f / worldHeight.toFloat / worldCameraScale / zoom * aspectRatio
//    println(s"updating screen size $aspectRatio $cameraXScale, $cameraYScale")
    projection.scale(cameraXScale, -cameraYScale, 1f)
    projection.translate(
      -worldWidth.toFloat * worldCameraScale / 2 + xTranslate,
      -worldHeight.toFloat * worldCameraScale / 2 + yTranslate,
      0f
    )
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      projection.get(buf)
      uniProjection.foreach(glUniformMatrix4fv(_, false, buf))
      checkError()
    })

    // after all the ortho tweaks, we've got a 2d coordinate system where 0,0 is top left of the screen,
    // +x = right, +y = down,
    // and world coords map 1-to-1 to screen coords
  }
}
