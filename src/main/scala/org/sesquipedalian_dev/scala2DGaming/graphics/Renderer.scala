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

import java.nio.{ByteBuffer, FloatBuffer, IntBuffer}

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30.{GL_TEXTURE_2D_ARRAY, glBindVertexArray, glDeleteVertexArrays, glGenVertexArrays}
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.util.{ThrowsExceptionOnGLError, cleanly}

case class DrawCallInfo(
  elBuffer: IntBuffer,
  vertexBuffer: FloatBuffer,
  var numObjectsThisDraw: Int,
  callback: () => Unit // perform any necessary setup
)

trait Renderer extends Renderable with ThrowsExceptionOnGLError {
  def vertexBufferSize: Int
  def elementBufferSize: Int
  def vertexShaderRscName: String
  def fragmentShaderRscName: String

  // give easy way to reference the draw call info - text index
  var drawCalls: Map[String, DrawCallInfo] = Map()

  var programHandle: Option[Int] = None

  // vertex array object - keep track of memory layout of vertex array buffers
  var vao: Option[Int] = None

  // vertex buffer object - communicate vertices to GFX
  var vbo: Option[Int] = None

  // element array buffer - once vertices are defined in the buffer, elements let us reuse those vertices.
  var ebo: Option[Int] = None

  def init(): Unit = {
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // set up Vertex Array Object - defines memory layout for sending vertex data to GPU
    vao = Some(glGenVertexArrays())
    vao.foreach(glBindVertexArray)

    // NOTE the other array buffers need to be defined REAL CLOSE to the VAO for whatever reason

    vbo = Some(glGenBuffers())
    vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
    glBufferData(GL_ARRAY_BUFFER, vertexBufferSize * java.lang.Float.BYTES, GL_STATIC_DRAW)

    ebo = Some(glGenBuffers())
    ebo.foreach(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _))
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBufferSize * java.lang.Integer.BYTES, GL_STATIC_DRAW)

    // create vertex shader, load up the source for it, and compile
    val vertexShaderHandle: Int = glCreateShader(GL_VERTEX_SHADER)
    val vertexShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream(vertexShaderRscName)
    ).mkString
    glShaderSource(vertexShaderHandle, vertexShaderSource)
    glCompileShader(vertexShaderHandle)

    // create fragment shader, load up the source for it, and compile
    val fragmentShaderHandle: Int = glCreateShader(GL_FRAGMENT_SHADER)
    val fragmentShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream(fragmentShaderRscName)
    ).mkString
    glShaderSource(fragmentShaderHandle, fragmentShaderSource)
    glCompileShader(fragmentShaderHandle)

    // check compile status of the shaders
    cleanly(MemoryStack.stackPush())(stack => {
      val vertexCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(vertexShaderHandle, GL_COMPILE_STATUS, vertexCompiled)

      val fragmentCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(fragmentShaderHandle, GL_COMPILE_STATUS, fragmentCompiled)

      if((vertexCompiled.get(0) <= 0) || (fragmentCompiled.get(0) <= 0)) {
        println(s"error compiling shaders! ${glGetShaderInfoLog(vertexShaderHandle)} ${glGetShaderInfoLog(fragmentShaderHandle)}")
      }
    })

    // create shader program and attach the shaders we created
    programHandle = Some(glCreateProgram())
    programHandle.foreach(glAttachShader(_, vertexShaderHandle))
    programHandle.foreach(glAttachShader(_, fragmentShaderHandle))

    // link the program
    programHandle.foreach(glLinkProgram)
    cleanly(MemoryStack.stackPush())(stack => {
      val linkStatus: IntBuffer = stack.mallocInt(1)
      programHandle.foreach(glGetProgramiv(_, GL_LINK_STATUS, linkStatus))

      if(linkStatus.get(0) <= 0) {
        throw new Exception("error linking shader program")
      }
    })

    glDeleteShader(vertexShaderHandle)
    glDeleteShader(fragmentShaderHandle)
  }

  def render(): Unit = {
    // if we've set up stuff to draw this frame, send it to the GPU
    drawCalls.keys.foreach(flushVertexData)
  }

  def cleanup(): Unit = {
    vao.foreach(glDeleteVertexArrays)
    vbo.foreach(glDeleteBuffers)
    ebo.foreach(glDeleteBuffers)
    programHandle.foreach(glDeleteProgram)
    drawCalls.foreach(drawCall => {
      MemoryUtil.memFree(drawCall._2.vertexBuffer)
      MemoryUtil.memFree(drawCall._2.elBuffer)
    })
  }

  def flushVertexData(key: String): Unit = {
    drawCalls.get(key).foreach(drawCall => {
      val vertexBuffer = drawCall.vertexBuffer
      val elBuffer = drawCall.elBuffer
      val numObjectsThisDraw = drawCall.numObjectsThisDraw

      if(numObjectsThisDraw > 0) {
        vao.flatMap(v => programHandle.map(ph => (v, ph))).foreach(t => {
          val (v, p) = t
          glBindVertexArray(v)
          glUseProgram(p)

          drawCall.callback()

          vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
          vertexBuffer.flip()
          glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)
          checkError()

          ebo.foreach(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _))
          elBuffer.flip()
          glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, elBuffer)
          checkError()

          glDrawElements(GL_TRIANGLES, numObjectsThisDraw, GL_UNSIGNED_INT, 0)
          checkError()

          // clear the vertex data for this pass
          vertexBuffer.clear()
          elBuffer.clear()
          drawCall.numObjectsThisDraw = 0
        })
      }
    })
  }
}
