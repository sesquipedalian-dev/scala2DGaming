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

import org.sesquipedalian_dev.scala2DGaming.entities.{BadGuy, GunTurret, Location, WorldMap}
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.CloseHandler
import org.sesquipedalian_dev.scala2DGaming.ui.FPSCounter

object Main {
//  final val SCREEN_WIDTH: Int = 640
//  final val SCREEN_HEIGHT: Int = 480
//  final val SCREEN_WIDTH: Int = 1920
//  final val SCREEN_HEIGHT: Int = 1080
  final val SCREEN_WIDTH: Int = 1024
  final val SCREEN_HEIGHT: Int = 576
  final val TEXTURE_SIZE: Int = 512

  final val WORLD_WIDTH: Int = 50
  final val WORLD_HEIGHT: Int = 50

  var window: GLFWWindow = null

  def main(args: Array[String]): Unit = {
    println("Hello World")

    window = new GLFWWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "tut")
    window.init()

    // make input handlers
    window.glfwWindow.foreach(w => new CloseHandler(w))

    // make renderables - order matters for initialization
    val renderer = new WorldSpritesRenderer(TEXTURE_SIZE, WORLD_WIDTH, WORLD_HEIGHT)
    renderer.init()
    val uiRenderer = new UIRenderer()
    uiRenderer.init()

    new FPSCounter()

    val world = new WorldMap(Location(WORLD_WIDTH, WORLD_HEIGHT))
    world.initTestData()

    // test bad guy
    new BadGuy(Location(0, 25), Some(Location(1, 0)), Location(WORLD_WIDTH, WORLD_HEIGHT), 50)
    new BadGuy(Location(0, 20), Some(Location(1, 1)), Location(WORLD_WIDTH, WORLD_HEIGHT), 50)
    new BadGuy(Location(0, 40), Some(Location(1, -1)), Location(WORLD_WIDTH, WORLD_HEIGHT), 50)

    new GunTurret(Location(5, 24), 1, 10)
    new GunTurret(Location(5, 27), 1, 10)

    // loop until terminated
    window.mainLoop(HasGameUpdate.update, Renderable.render)

    // clean up
    window.cleanup()
    Renderable.cleanup()
  }
}
