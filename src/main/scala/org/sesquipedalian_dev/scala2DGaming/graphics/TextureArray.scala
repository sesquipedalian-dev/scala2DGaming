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


import java.io.BufferedInputStream

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL42._
import org.lwjgl.stb.STBImage._
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.util.{ThrowsExceptionOnGLError, cleanly}

import scala.util.{Failure, Success}

// load textures into GPU for use as a GL_TEXTURE_2D_ARRAY.
// assumably the vertex data or a shader uniform will select which index
// in the array is used.
class TextureArray(
  textureSize: Int, // height & width of textures in this array - should be power of 2
  initialCapacity: Int = 1000
) extends ThrowsExceptionOnGLError {
  // we're going to assume one texture unit for now, and it has an array in it
  var textureHandle: Option[Int] = None

  var textureFiles: List[String] = Nil

  // how many textures to put in the array
  var capacity: Int = initialCapacity

  // add a resource file to our set of loaded textures
  def addTextureResource(resourceName: String): Unit = {
    textureHandle.foreach(th => {
      glBindTexture(GL_TEXTURE_2D_ARRAY, th)

      // if we've loaded too many texture, resize and reload the whole thing
      if(textureFiles.lengthCompare(capacity) > 0) {
        capacity *= 2
        resize()
        textureFiles.foreach(addTextureResource)
      }

      val index = textureFiles.size

      // load texture data (pixels)
      val texLoadResult = cleanly(MemoryStack.stackPush())(stack => {
        // load the texture file and get it into a byte buffer that STBI can use
        val stream = new BufferedInputStream(getClass.getResourceAsStream(resourceName))
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
        if (pixels == null) {
          throw new Exception(s"couldn't load bitmap pixels: ${stbi_failure_reason()}")
        }
        MemoryUtil.memFree(byteBuffer)

        // send the pixels to the GPU
        glTexSubImage3D(GL_TEXTURE_2D_ARRAY,
          0, // mipmap id
          0, 0, index, // x, y, layer offsets
          textureSize, textureSize, 1, // x, y, depth sizes
          GL_RGBA,
          GL_UNSIGNED_BYTE,
          pixels
        )
        checkError()

        // free the texel info since we've sent it to the GPU
        MemoryUtil.memFree(pixels)
      })
      texLoadResult match {
        case Success(_) => textureFiles :+= resourceName
        case Failure(e) => println("problemas loading texture"); e.printStackTrace()
      }

      glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
    })
  }

  // resize the bound texture -
  def resize(): Unit = {
    textureHandle.foreach(glDeleteTextures)
    checkError()

    // create texture handle and bind it as the active texture unit
    val texHandle = glGenTextures()
    checkError()
    textureHandle = Some(texHandle)
    glBindTexture(GL_TEXTURE_2D_ARRAY, texHandle)
    checkError()

    // set up storage for all the textures we'll use as a big array. that way each vertex can refer to its
    // index in the array.  The actual texel data will be loaded after
    glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, textureSize, textureSize, capacity)
    checkError()

    // how to unpack the RGBA bytes
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
    checkError()

    // texture parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    checkError()

    // unbind this texture unit
    glBindTexture(GL_TEXTURE_2D_ARRAY, 0)
  }

  def cleanup(): Unit = {
    textureHandle.foreach(glDeleteTextures)
  }

  resize() // initial load
}
