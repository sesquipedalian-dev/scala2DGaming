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

import java.util.Date

import org.sesquipedalian_dev.scala2DGaming.entities._
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.{CloseHandler, LoggingMouseCursorHandler}
import org.sesquipedalian_dev.scala2DGaming.ui._

import scala.util.Random

object Main {
  final val UI_WIDTH = 2560
  final val UI_HEIGHT = 1440

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

  var random: Option[Random] = None

  def main(args: Array[String]): Unit = {
    println("Hello World")

    // TODO allow us to initialize random with a specific seed (for replay)
    val seed: Long = new Date().getTime
    random = Some(new Random(new java.util.Random(seed)))

    window = new GLFWWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "tut")
    window.init()

    // make input handlers
    new CloseHandler()
    new LoggingMouseCursorHandler()

    // make renderables - order matters for initialization
    new WorldSpritesRenderer(TEXTURE_SIZE, WORLD_WIDTH, WORLD_HEIGHT).init()
    new UIButtonsRenderer(UI_WIDTH, UI_HEIGHT).init()
    new RangeOverlay(WORLD_WIDTH, WORLD_HEIGHT, TEXTURE_SIZE).init()
    new UITextRenderer(UI_WIDTH, UI_HEIGHT).init()

    new FPSCounter()
    new TimeOfDay()
    new PauseButton()
    new SlowButton()
    new MediumButton()
    new FastButton()

    val world = new WorldMap(Location(WORLD_WIDTH, WORLD_HEIGHT))
    world.initTestData()

    new BadGuySpawner(Location(0, 26), 5f)

    // test guns
//    new GunTurret(Location(5, 24), 1, 10, RangeArc(Math.PI.toFloat, Math.PI.toFloat * 2, 4))
//    new GunTurret(Location(5, 27), 1, 10, RangeArc(0, Math.PI.toFloat, 4))

    val gun1 = new GunTurret(Location(5, 24), 1, 20, RangeArc(Math.PI.toFloat / 2, 3 * Math.PI.toFloat / 2, 4))
    val gun2 = new GunTurret(Location(5, 27), 1, 20, RangeArc(Math.PI.toFloat / 2, 3 * Math.PI.toFloat / 2, 4))

    val gunner1 = new GoodGuy("Washington", Location(6, 24))
    gunner1.use(gun1)
    val gunner2 = new GoodGuy("Jefferson", Location(6, 27))
    gunner2.use(gun2)

    val groups = new GoodGuyGroups()
    groups.groups = Map("Group 1" -> List(gunner1, gunner2))

    // fork off JavaFX UI thread
    JavaFXManager.myInit()
    new ToggleGroupsUiButton()

    // loop until terminated
    window.mainLoop(HasGameUpdate.update, Renderable.render)

    // clean up
    window.cleanup()
    Renderable.cleanup()
//    GroupsUi.close()
    JavaFXManager.myCleanup()
  }
}

