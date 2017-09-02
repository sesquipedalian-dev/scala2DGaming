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
package org.sesquipedalian_dev.scala2DGaming

import java.io.BufferedInputStream
import java.nio.{ByteBuffer, FloatBuffer, IntBuffer}

import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.{glfwGetCurrentContext, glfwGetFramebufferSize}
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20.{glGetAttribLocation, glGetUniformLocation, glUniformMatrix4fv, _}
import org.lwjgl.opengl.GL30.{glDeleteVertexArrays, _}
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.graphics.Renderable
import org.sesquipedalian_dev.scala2DGaming.util.cleanly
import org.lwjgl.stb.STBImage._

import scala.util.{Failure, Success}

// wrap GL
class TestMesh(textureSize: Int, worldWidth: Int, worldHeight: Int) extends Renderable {
  def checkError(): Unit = {
    val glErrorEnum = glGetError()
    if(glErrorEnum != GL_NO_ERROR) {
      throw new Exception(s"TestMesh got error $glErrorEnum")
    }
  }

  // vertex array object - keep track of memory layout of vertex array buffers
  var vao: Option[Int] = None

  // vertex buffer object - communicate vertices to GFX
  var vbo: Option[Int] = None

  // element array buffer - once vertices are defined in the buffer, elements let us reuse those vertices.
  var ebo: Option[Int] = None

  // texture handle
  var textureHandles: List[Int] = Nil

  var textureBytes: ByteBuffer = MemoryUtil.memAlloc(textureSize * textureSize * 4 /* BPP */)

  val textureFileNames = List("/testTex.bmp", "/testTex2.bmp")

  def init(): Unit = {
    GL.createCapabilities()

    vao = Some(glGenVertexArrays())

    vao.foreach(glBindVertexArray)

    // generate textures
    textureFileNames.foreach(texFile => {
      val texHandle = glGenTextures()
      glBindTexture(GL_TEXTURE_2D, texHandle)
      textureHandles :+= texHandle

      // load texture data (pixels)
      val texLoadResult = cleanly(MemoryStack.stackPush())(stack => {
        // load the texture file and get it into a byte buffer that STBI can use
        val stream = new BufferedInputStream(getClass.getResourceAsStream(texFile))
        val byteArray = Stream.continually(stream.read).takeWhile(-1 != _).map(_.toByte).toArray
        val byteBuffer = MemoryUtil.memAlloc(byteArray.size)
        byteBuffer.put(byteArray)
        byteBuffer.flip()

        // define some buffers for stbi to fill in details of the texture
        val texWidth = stack.mallocInt(1)
        val texHeight = stack.mallocInt(1)
        val channels = stack.mallocInt(1)

        // load image with STBI - since we're flipping our Y axis
        stbi_set_flip_vertically_on_load(true)
        val pixels = stbi_load_from_memory(byteBuffer, texWidth, texHeight, channels, 4)
        if(pixels == null) {
          throw new Exception(s"couldn't load bitmap pixels: ${stbi_failure_reason()}")
        }

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureSize, textureSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels)
        checkError()
      })
      texLoadResult match {
        case Success(_) =>
        case Failure(e) => println("problemas"); e.printStackTrace()
      }

      // how to unpack the RGBA bytes
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

      // texture parameters
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

      glBindTexture(GL_TEXTURE_2D, 0)
    })


    // set up a Vertex Buffer Object with a triangle
    cleanly(MemoryStack.stackPush())(stack => {
      // set up vertex data - x / y / z position and texture coord
      val vertices: FloatBuffer = stack.mallocFloat(4 * 5)
      vertices.put(0f).put(0f).put(0f).put(0f).put(1f)
      vertices.put(textureSize).put(0f).put(0f).put(1f).put(1f)
      vertices.put(textureSize).put(textureSize).put(0f).put(1f).put(0f)
      vertices.put(0f).put(textureSize).put(0f).put(0f).put(0f)
      vertices.flip()

      vbo = Some(glGenBuffers())
      vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
      glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
    })

    cleanly(MemoryStack.stackPush())(stack => {
      val elements: IntBuffer = stack.mallocInt(2 * 3)
      elements.put(0).put(1).put(2)
      elements.put(2).put(3).put(0)
      elements.flip()

      ebo = Some(glGenBuffers())
      ebo.foreach(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _))
      glBufferData(GL_ELEMENT_ARRAY_BUFFER, elements, GL_STATIC_DRAW)
    })

    // create vertex shader, load up the source for it, and compile
    val vertexShaderHandle: Int = glCreateShader(GL_VERTEX_SHADER)
    val vertexShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream("/basic.vert")
    ).mkString
    glShaderSource(vertexShaderHandle, vertexShaderSource)
    glCompileShader(vertexShaderHandle)

    // create fragment shader, load up the source for it, and compile
    val fragmentShaderHandle: Int = glCreateShader(GL_FRAGMENT_SHADER)
    val fragmentShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream("/basic.frag")
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
    Renderable.setProgram(glCreateProgram())
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
    programHandle.foreach(glUseProgram)

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val posAttrib = programHandle.map(glGetAttribLocation(_, "position"))
    posAttrib.foreach(pos => {
      glEnableVertexAttribArray(pos)
      glVertexAttribPointer(pos, 3, GL_FLOAT, false, 5 * java.lang.Float.BYTES, 0)
    })

    val texAttrib = programHandle.map(glGetAttribLocation(_, "texCoord"))
    texAttrib.foreach(tex => {
      glEnableVertexAttribArray(tex)
      glVertexAttribPointer(tex, 2, GL_FLOAT, false, 5 * java.lang.Float.BYTES, 3 * java.lang.Float.BYTES)
    })

    // set the texture image uniform
    val texIUniform = programHandle.map(glGetUniformLocation(_, "texImage"))
    cleanly(MemoryStack.stackPush())(stack => {
      texIUniform.foreach(uniform => {
        glUniform1i(uniform, 0)
      })
    })

    updateScreenSize()
  }

  def updateScreenSize(): Unit = {
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

    // set camera-to-projection transform - ortho
    val uniProjection = programHandle.map(glGetUniformLocation(_, "projection"))
    val projection = new Matrix4f()
    projection.ortho2D(0, width, height, 0)
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      projection.get(buf)
      uniProjection.foreach(glUniformMatrix4fv(_, false, buf))
    })

    // set window viewport so ortho projection lines up
    glViewport(0, 0, width.toInt, height.toInt)

    // after all the ortho tweaks, we've got a 2d coordinate system where 0,0 is top left of the screen,
    // +x = right, +y = down,
    // and world coords map 1-to-1 to screen coords
  }

  def render(): Unit = {
    (vao zip programHandle).foreach(p2 => {
      val (v, p) = p2
      glBindVertexArray(v)
      glUseProgram(p)

      // top left
      drawAGuy(0, 0, 0)
      // top right
      drawAGuy((worldWidth - 1) * textureSize, 0, 1)
      // bottom right
      drawAGuy((worldWidth - 1) * textureSize, (worldHeight - 1) * textureSize, 0)
      // bottom left
      drawAGuy(0, (worldHeight - 1) * textureSize, 1)
    })
  }

  def drawAGuy(x: Float, y: Float, texIndex: Int): Unit = {
    textureHandles.drop(texIndex).headOption.foreach(texture => {
      glBindTexture(GL_TEXTURE_2D, texture)

      // set camera-to-projection transform - ortho
      val uniProjection = programHandle.map(glGetUniformLocation(_, "model"))
      val model = new Matrix4f()
      model.translate(x, y, 0)
      cleanly(MemoryStack.stackPush())(stack => {
        val buf: FloatBuffer = stack.mallocFloat(4 * 4)
        model.get(buf)
        uniProjection.foreach(glUniformMatrix4fv(_, false, buf))
      })

      glDrawElements(GL_TRIANGLES, 6 * 1, GL_UNSIGNED_INT, 0)
    })
  }


  def cleanup(): Unit = {
    vao.foreach(glDeleteVertexArrays)
    vbo.foreach(glDeleteBuffers)
    ebo.foreach(glDeleteBuffers)
    programHandle.foreach(glDeleteProgram)
    textureHandles.foreach(glDeleteTextures)
    MemoryUtil.memFree(textureBytes)
  }
}
