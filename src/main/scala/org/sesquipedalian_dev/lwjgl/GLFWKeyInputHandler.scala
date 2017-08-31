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
package org.sesquipedalian_dev.lwjgl

import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFW._

class GLFWKeyInputHandler(window: Long) extends GLFWKeyCallback {
  final val CAMERA_TRANSLATE_SPEED: Float = 512// always move 1/4th the screen?
  final val CAMERA_ZOOM_SPEED: Float = 2f

  override def invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int): Unit = {
    var xTranslateDir = 0f
    var yTranslateDir = 0f
    var newZoom = (cur: Float) => cur

    // default action: set application to terminate when escape pressed
    if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
      glfwSetWindowShouldClose(window, true)
    } else if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
      xTranslateDir = 1f
    } else if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
      xTranslateDir = -1f
    } else if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
      yTranslateDir = 1f
    } else if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
      yTranslateDir = -1f
    } else if (key == GLFW_KEY_Q && action == GLFW_RELEASE) {
      newZoom = _ * CAMERA_ZOOM_SPEED
    } else if (key == GLFW_KEY_E && action == GLFW_RELEASE) {
      newZoom = _ / CAMERA_ZOOM_SPEED
    }

    val (curX, curY, curZoom) = Main.renderer.getCamera()
    if(xTranslateDir != 0f || yTranslateDir != 0f || newZoom(curZoom) != curZoom) {
      Main.renderer.setCamera(
        curX + (xTranslateDir * CAMERA_TRANSLATE_SPEED),
        curY + (yTranslateDir * CAMERA_TRANSLATE_SPEED),
        newZoom(curZoom)
      )
    }
  }
}
