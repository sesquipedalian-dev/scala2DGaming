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

import java.nio.FloatBuffer

import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.opengl.GL20.{glGetUniformLocation, glUniformMatrix4fv}
import org.lwjgl.system.MemoryStack
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.input.InputHandler
import org.sesquipedalian_dev.scala2DGaming.util.cleanly
import org.lwjgl.glfw.GLFW._

class Camera2D(
  screenWidth: Int,
  screenHeight: Int,
  worldWidth: Int,
  worldHeight: Int,
  textureSize: Int
) extends Renderable
  with InputHandler
{
  final val MAX_ZOOM: Float = 1f
  final val CAMERA_TRANSLATE_SPEED: Float = textureSize
  final val CAMERA_ZOOM_SPEED: Float = 2f

  var zoom: Float = MAX_ZOOM
  var xTranslate: Float = 0f
  var yTranslate: Float = 0f

  // returns current x translate, y translate, and zoom for camera
  def getCamera(): (Float, Float, Float) = (xTranslate, yTranslate, zoom)

  def setCamera(requestedXTranslate: Float = 0, requestedYTranslate: Float = 0, _zoom: Float = 2f): Unit = {
    val maxZ = MAX_ZOOM
    val minZ = screenWidth.toFloat / (textureSize * worldWidth)
    zoom = math.max(minZ, math.min(_zoom, maxZ))

    // limit translation to not move beyond the bounds of the world space
    val screenScale = (zoom / minZ) // this tells us how much of the world we can see on one screen

    // the idea here is to find how many world pixels one screen takes up - then you can move
    // that many screens in world coordinates (minus 1 for the screen starting at the origin
    val oneScreenX = worldWidth * textureSize / screenScale
    val maxX = (screenScale - 1) * oneScreenX * -1
    val oneScreenY = worldHeight * textureSize / screenScale
    val maxY = (screenScale - 1) * oneScreenY * -1

    xTranslate = math.min(0f, math.max(maxX, requestedXTranslate))
    yTranslate = math.min(0f, math.max(maxY, requestedYTranslate))
  }

  override def init(): Unit = {
    setCamera()
  }

  override def render(): Unit = {
//    println(s"rendering camera $xTranslate $yTranslate $zoom")

    // set world-to-camera transform - also identity
    val uniView = programHandle.map(glGetUniformLocation(_, "view"))
    val viewMatrix = new Matrix4f
    viewMatrix.scale(zoom, zoom, 1f) // zoom
    viewMatrix.translate(xTranslate, yTranslate, 0f) // translate

    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      viewMatrix.get(buf)
      uniView.foreach(glUniformMatrix4fv(_, false, buf))
    })
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
}
