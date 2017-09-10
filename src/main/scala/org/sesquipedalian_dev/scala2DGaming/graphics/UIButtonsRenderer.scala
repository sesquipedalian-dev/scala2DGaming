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
import org.lwjgl.glfw.GLFW.{glfwGetCurrentContext, glfwGetFramebufferSize}
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20.{glGetAttribLocation, glGetUniformLocation, glUniformMatrix4fv, _}
import org.lwjgl.opengl.GL30.{glDeleteVertexArrays, _}
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.util.{ThrowsExceptionOnGLError, cleanly}

class UIButtonsRenderer(
  worldWidth: Int,
  worldHeight: Int
) extends Renderer
  with ThrowsExceptionOnGLError
{
  val textureSize: Int = 32
  final val VERTICES_PER_THING = 4
  final val MAX_THINGS_PER_DRAW = 1024 / (VERTICES_PER_THING + 1);
  final val ELEMENTS_PER_THING = 6

  def vertexBufferSize: Int = MAX_THINGS_PER_DRAW
  def elementBufferSize: Int = MAX_THINGS_PER_DRAW * ELEMENTS_PER_THING
  def vertexShaderRscName: String = "/shaders/textureArray.vert"
  def fragmentShaderRscName: String = "/shaders/textureArray.frag"

  var textureArray: Option[TextureArray] = None

  var camera: Option[UICamera] = None

  override def init(): Unit = {
    super.init()

    programHandle.foreach(glUseProgram)

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val stride = 5 * java.lang.Float.BYTES
    val posAttrib = programHandle.map(glGetAttribLocation(_, "position"))
    posAttrib.foreach(pos => {
      glEnableVertexAttribArray(pos)
      glVertexAttribPointer(pos, 2, GL_FLOAT, false, stride, 0)
    })

    val texAttrib = programHandle.map(glGetAttribLocation(_, "texCoord"))
    texAttrib.foreach(tex => {
      glEnableVertexAttribArray(tex)
      glVertexAttribPointer(tex, 2, GL_FLOAT, false, stride, 2 * java.lang.Float.BYTES)
    })

    val texIAttrib = programHandle.map(glGetAttribLocation(_, "texIndex"))
    texIAttrib.foreach(tex => {
      glEnableVertexAttribArray(tex)
      glVertexAttribPointer(tex, 1, GL_FLOAT, false, stride, 4 * java.lang.Float.BYTES)
    })

    // set the texture image uniform
    val texIUniform = programHandle.map(glGetUniformLocation(_, "texImage"))
    cleanly(MemoryStack.stackPush())(stack => {
      texIUniform.foreach(uniform => {
        glUniform1i(uniform, 0)
      })
    })

    val testTextureNames = List()
    textureArray = Some(new TextureArray(textureSize))
    testTextureNames.foreach(fn => textureArray.foreach(_.addTextureResource(fn)))

    // create our camera
    camera = Some(new UICamera(worldWidth, worldHeight, 1))
    camera.foreach(camera => {
      camera.register()
      programHandle.foreach(camera.init)
    })
    register()
  }

  override def render(): Unit = {
    programHandle.foreach(glUseProgram)
    camera.foreach(_.updateScreenSize("projection"))

    HasUiSpriteRendering.render(this)

    //    // top left
    //    drawAGuy(0, 0, 0)
    //    // top right
    //    drawAGuy((worldWidth - 1) * textureSize, 0, 1)
    //    // bottom right
    //    drawAGuy((worldWidth - 1) * textureSize, (worldHeight - 1) * textureSize, 0)
    //    // bottom left
    //    drawAGuy(0, (worldHeight - 1) * textureSize, 1)
    //
    //    // do all the terra
    //    for {
    //      _x <- 0 until worldWidth
    //      _y <- 0 until worldHeight
    //      (x, y) <- Some(_x, _y) if !List(
    //        (0, 0),
    //        (0, worldHeight - 1),
    //        (worldWidth - 1, 0),
    //        (worldWidth - 1, worldHeight - 1)
    //      ).contains((x, y))
    //    } {
    //      drawAGuyWorld(x, y, 2)
    //    }

    super.render()
  }

  override def flushVertexData(): Unit = {
    textureArray.flatMap(_.textureHandle).foreach(th => {
      glBindTexture(GL_TEXTURE_2D_ARRAY, th)
      super.flushVertexData()
    })
  }

  def drawAButton(x: Float, y: Float, texIndex: Int): Unit = {
    if(vertexBuffer.remaining < (4 * 5) || elBuffer.remaining < (2 * 3)) {
      flushVertexData()
    }

    println(s"UI button print $x $y $texIndex")
    vertexBuffer.put(x).put(y)
      .put(0f).put(1f).put(texIndex.toFloat)
    vertexBuffer.put(x + textureSize).put(y)
      .put(1f).put(1f).put(texIndex.toFloat)
    vertexBuffer.put(x + textureSize).put(y + (textureSize / camera.flatMap(_.aspectRatio).getOrElse(1f)))
      .put(1f).put(0f).put(texIndex.toFloat)
    vertexBuffer.put(x).put(y + (textureSize / camera.flatMap(_.aspectRatio).getOrElse(1f)))
      .put(0f).put(0f).put(texIndex.toFloat)

    val currentVertIndex = numObjectsThisDraw / 6 * VERTICES_PER_THING
    elBuffer.put(currentVertIndex).put(currentVertIndex + 1).put(currentVertIndex + 2)
    elBuffer.put(currentVertIndex + 2).put(currentVertIndex + 3).put(currentVertIndex)

    numObjectsThisDraw += 6
  }

  override def cleanup(): Unit = {
    // free all the memory we used
    super.cleanup()
    textureArray.foreach(_.cleanup)
    camera.foreach(_.cleanup)
  }

  UIButtonsRenderer.singleton = Some(this)
}

object UIButtonsRenderer {
  var singleton: Option[UIButtonsRenderer] = None

  def draw(func: (FloatBuffer, IntBuffer) => Unit): Unit = {
    singleton.foreach(wsr => {
      func(wsr.vertexBuffer, wsr.elBuffer)
      wsr.numObjectsThisDraw += 6
    })
  }

  def drawAGuyWorld(x: Float, y: Float, texIndex: Int): Unit = singleton.foreach(_.drawAButton(x, y, texIndex))
}
