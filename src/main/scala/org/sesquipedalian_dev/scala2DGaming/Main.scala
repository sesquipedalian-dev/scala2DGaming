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
import org.sesquipedalian_dev.scala2DGaming.ui.javafx.{JavaFXManager, ToggleGroupsUiButton, ToggleScheduleUiButton}
import org.sesquipedalian_dev.scala2DGaming.util.Logging

import scala.util.Random

object Main extends Logging {
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
    info"Main Program Start"

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
    new BuyGoodGuyButton()

    val world = new WorldMap(Location(WORLD_WIDTH, WORLD_HEIGHT))
    world.initTestData()

    new BadGuySpawner(Location(0, 26), 5f)

    new GunTurret(Location(5, 24), 1, 20, RangeArc(Math.PI.toFloat / 2, 3 * Math.PI.toFloat / 2, 5))
    new GunTurret(Location(5, 27), 1, 20, RangeArc(Math.PI.toFloat / 2, 3 * Math.PI.toFloat / 2, 5))

    val gunner1 = new GoodGuy("Washington", Location(15, 24))
    val gunner2 = new GoodGuy("Jefferson", Location(15, 27))

    val groups = new GoodGuyGroups()
    val group1 = new GoodGuyGroup("Group 1", List(gunner1, gunner2))
    groups.groups = Map(group1.name -> group1)

    new Bed(Location(40, 24))
    new Bed(Location(40, 27))

    new Commander(100)

//    Dialog.open(
//      """
//        |There are many variations of passages of Lorem Ipsum available,
//        |but the majority have suffered alteration in some form, by injected humour,
//        |or randomised words which don't look even slightly believable.
//        |If you are going to use a passage of Lorem Ipsum, you need
//        |to be sure there isn't anything embarrassing hidden in the
//        |middle of text. All the Lorem Ipsum generators on the Internet t
//        |end to repeat predefined chunks as necessary, making this the
//        |first true generator on the Internet. It uses a dictionary of over 2
//        |00 Latin words, combined with a handful of model sentence structures, to generate
//        |Lorem Ipsum which looks reasonable. The generated Lorem Ipsum is
//        |therefore always free from repetition, injected humour, or
//        |non-characteristic words etc.
//      """.stripMargin, "/textures/ui/sarge_dialog.bmp")

    // fork off JavaFX UI thread
    JavaFXManager.myInit()
    new ToggleGroupsUiButton()
    new ToggleScheduleUiButton()

    // loop until terminated
    window.mainLoop(HasGameUpdate.update, Renderable.render)

    // clean up
    window.cleanup()
    Renderable.cleanup()
    JavaFXManager.myCleanup()
    Dialog.close()

    info"Main Program End"
  }
}

