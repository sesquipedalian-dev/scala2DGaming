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

import org.sesquipedalian_dev.scala2DGaming.Main.TestTerrain
import org.sesquipedalian_dev.scala2DGaming.entities._
import org.sesquipedalian_dev.scala2DGaming.entities.enemies.BadGuySpawner
import org.sesquipedalian_dev.scala2DGaming.entities.equipment.{Bed, GunTurret, RangeArc}
import org.sesquipedalian_dev.scala2DGaming.entities.soldiers.{GoodGuy, GoodGuyGroup, GoodGuyGroups}
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.Terrain.adapter
import org.sesquipedalian_dev.scala2DGaming.entities.terrain.{Fence, Terrain}
import org.sesquipedalian_dev.scala2DGaming.game.{Commander, HasGameUpdate, TimeOfDay}
import org.sesquipedalian_dev.scala2DGaming.graphics._
import org.sesquipedalian_dev.scala2DGaming.input.{CloseHandler, LoggingMouseCursorHandler}
import org.sesquipedalian_dev.scala2DGaming.ui._
import org.sesquipedalian_dev.scala2DGaming.ui.javafx.{JavaFXManager, ToggleBuildUiButton, ToggleGroupsUiButton, ToggleScheduleUiButton}
import org.sesquipedalian_dev.util.Logging
import org.sesquipedalian_dev.util.pathfinding.{Adapter, Pathfinder}
import org.sesquipedalian_dev.util.pathfinding.Pathfinder.NodeID

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

    // need to refer to object so it links in?
    Bulldozer.name

    // make input handlers
    new CloseHandler()
    new LoggingMouseCursorHandler()

    // make renderables - order matters for initialization
    new WorldSpritesRenderer(TEXTURE_SIZE, WORLD_WIDTH, WORLD_HEIGHT).init()
    new UIButtonsRenderer(UI_WIDTH, UI_HEIGHT).init()
    new RangeOverlay(WORLD_WIDTH, WORLD_HEIGHT, TEXTURE_SIZE).init()
    val bo = new BuildOverlay(WORLD_WIDTH, WORLD_HEIGHT, TEXTURE_SIZE)
    bo.init()
    new UITextRenderer(UI_WIDTH, UI_HEIGHT).init()

    new FPSCounter()
    new TimeOfDay()
    new PauseButton()
    new SlowButton()
    new MediumButton()
    new FastButton()
    new BuyGoodGuyButton()

    WorldMap.initTestData(Location(WORLD_WIDTH, WORLD_HEIGHT))

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

    // fork off JavaFX UI thread
    JavaFXManager.myInit()
    new ToggleGroupsUiButton()
    new ToggleScheduleUiButton()
    new ToggleBuildUiButton()

    // TESTING

    // loop until terminated
    window.mainLoop(HasGameUpdate.update, Renderable.render)

    // clean up
    window.cleanup()
    Renderable.cleanup()
    JavaFXManager.myCleanup()
    Dialog.close()

    info"Main Program End"
  }


  /************************************
   * Pathfinding testing!
   ***************************************/

  def main2(args: Array[String]): Unit = {
    println("Hello World!")

//    val terrain: List[TestTerrain] = List(
//      TestTerrain(TestLocation(0, 0)), TestTerrain(TestLocation(1, 0)), TestTerrain(TestLocation(2, 0)), TestTerrain(TestLocation(3, 0)),
//      TestTerrain(TestLocation(0, 1)), TestTerrain(TestLocation(1, 1)), TestTerrain(TestLocation(2, 1)), TestTerrain(TestLocation(3, 1)),
//      TestTerrain(TestLocation(0, 2)), TestTerrain(TestLocation(1, 2), false), TestTerrain(TestLocation(2, 2), false), TestTerrain(TestLocation(3, 2)),
//      TestTerrain(TestLocation(0, 3)), TestTerrain(TestLocation(1, 3), false), TestTerrain(TestLocation(2, 3), false), TestTerrain(TestLocation(3, 3)),
//      TestTerrain(TestLocation(0, 4)), TestTerrain(TestLocation(1, 4)), TestTerrain(TestLocation(2, 4)), TestTerrain(TestLocation(3, 4)),
//      TestTerrain(TestLocation(0, 5)), TestTerrain(TestLocation(1, 5)), TestTerrain(TestLocation(2, 5)), TestTerrain(TestLocation(3, 5))
//    )

//    val terrain: List[TestTerrain] = List(
//      TestTerrain(TestLocation(0, 0)), TestTerrain(TestLocation(1, 0)), TestTerrain(TestLocation(2, 0), false), TestTerrain(TestLocation(3, 0)),
//      TestTerrain(TestLocation(0, 1)), TestTerrain(TestLocation(1, 1)), TestTerrain(TestLocation(2, 1), false), TestTerrain(TestLocation(3, 1)),
//      TestTerrain(TestLocation(0, 2)), TestTerrain(TestLocation(1, 2)), TestTerrain(TestLocation(2, 2), false), TestTerrain(TestLocation(3, 2)),
//      TestTerrain(TestLocation(0, 3)), TestTerrain(TestLocation(1, 3)), TestTerrain(TestLocation(2, 3), true), TestTerrain(TestLocation(3, 3)),
//      TestTerrain(TestLocation(0, 4)), TestTerrain(TestLocation(1, 4)), TestTerrain(TestLocation(2, 4), false), TestTerrain(TestLocation(3, 4)),
//      TestTerrain(TestLocation(0, 5)), TestTerrain(TestLocation(1, 5)), TestTerrain(TestLocation(2, 5), false), TestTerrain(TestLocation(3, 5)),
//      TestTerrain(TestLocation(0, 6)), TestTerrain(TestLocation(1, 6)), TestTerrain(TestLocation(2, 6), false), TestTerrain(TestLocation(3, 6))
//    )

    val texIndexToTraversable = List(
      false, false, true, true, true, true
    )
    val terrain = WorldMap.locationToTexIndex.zipWithIndex.map(p => {
      val (texIndex, index) = p
      val x = index % 50
      val y = index / 50
      val traversable = texIndexToTraversable(texIndex)
      TestTerrain(TestLocation(x, y), traversable)
    })

    val adapter = new TestPathfindingAdapter(terrain)
    val pathfinder = new Pathfinder(adapter)

//    val start = TestLocation(0, 0)
//    val goal = TestLocation(3, 6)


    def idToLocation(id: NodeID): TestLocation = {
      val loc = adapter.terrain(id).location
      TestLocation(loc.x, loc.y)
    }

    def locationToId(location: TestLocation): NodeID = {
      val intLoc = TestLocation(location.x, location.y)
      Some(terrain.indexWhere(t => {
        t.location == intLoc
      })).filter(_ >= 0).getOrElse(0)
    }

//    val path = pathfinder.search[TestLocation](start, goal, idToLocation _, locationToId _)
//    info"Got test path; from $start to $goal follow these paths $path"
//
//    val losToEdge = adapter.lineOfSight(27, 10)
//    info"testing los path: losToEdge 0,0 to 3,6? $losToEdge"
//
//    val downToUpStart = TestLocation(0, 6)
//    val downToUpGoal = TestLocation(3, 0)
//    val downToUpPath = pathfinder.search[TestLocation](downToUpStart, downToUpGoal, idToLocation _, locationToId _)
//    info"Got test path2; from $downToUpStart to $downToUpGoal follow these paths $downToUpPath"

    val start1 = TestLocation(0, 20)
    val goal1 = TestLocation(49, 10)
    val path1 = pathfinder.search[TestLocation](start1, goal1, idToLocation, locationToId)
    info"Got test path1; from $start1 to $goal1 follow $path1"
  }

  case class TestLocation(x: Int, y: Int)
  case class TestTerrain(location: TestLocation, traversable: Boolean = true)


  class TestPathfindingAdapter(val terrain: List[TestTerrain]) extends Adapter {
    override def getNodeCount() = terrain.size

    override def distance(n1: NodeID, n2: NodeID) = {
      val loc1 = terrain(n1).location
      val loc2 = terrain(n2).location

      Math.sqrt(Math.pow(Math.abs(loc1.x - loc2.x), 2) + Math.pow(Math.abs(loc1.y - loc2.y), 2)).toFloat
    }

    // adapted from https://github.com/lapinozz/Lazy-Theta-with-optimization-any-angle-pathfinding/blob/master/tileadaptor.hpp
    override def lineOfSight(n1: NodeID, n2: NodeID): Boolean = {
      // This line of sight check uses only integer values. First it checks whether the movement along the x or the y axis is longer and moves along the longer
      // one cell by cell. dx and dy specify how many cells to move in each direction. Suppose dx is longer and we are moving along the x axis. For each
      // cell we pass in the x direction, we increase variable f by dy, which is initially 0. When f >= dx, we move along the y axis and set f -= dx. This way,
      // after dx movements along the x axis, we also move dy moves along the y axis.
      var l1: TestLocation = terrain(n1).location
      var l2: TestLocation = terrain(n2).location

      var diff: TestLocation = TestLocation(l2.x - l1.x, l2.y - l1.y)

      var f: Int = 0

      var dir: TestLocation = TestLocation(0, 0) // Direction of movement. Value can be either 1 or -1.

      // The x and y locations correspond to nodes, not cells. We might need to check different surrounding cells depending on the direction we do the
      // line of sight check. The following values are used to determine which cell to check to see if it is unblocked.
      var offset: TestLocation = TestLocation(0, 0)

      if(diff.y < 0) {
        diff = diff.copy(y = -diff.y)
        dir = dir.copy(y = -1)
        offset = offset.copy(y = 0) // Cell is to the North
      } else {
        dir = dir.copy(y = 1)
        offset = offset.copy(y = 1) // Cell is to the South
      }

      if(diff.x < 0) {
        diff = diff.copy(x = -diff.x)
        dir = dir.copy(x = -1)
        offset = offset.copy(x = 0)
      } else {
        dir = dir.copy(x = 1)
        offset = offset.copy(x = 1)
      }


      // Move along the x axis and increment/decrement y when f >= diff.x.
      if(diff.x >= diff.y) {
        while(l1.x != l2.x) {
          f += Math.round(diff.y)
          if(f >= diff.x) {  // We are changing rows, we might need to check two cells this iteration.
            if(!isTraversable(TestLocation(l1.x + offset.x, l1.y + offset.y))) {
              return false
            }

            l1 = l1.copy(y = l1.y + dir.y)
            if(!isTraversable(l1)) {
              return false
            }

            f -= Math.round(diff.x)
          }

          if(f != 0 && !isTraversable(TestLocation(l1.x + offset.x, l1.y + offset.y))) {
            return false
          }

          // If we are moving along a horizontal line, either the north or the south cell should be unblocked.
          if(diff.y == 0 && !isTraversable(TestLocation(l1.x + offset.x, l1.y)) && !isTraversable(TestLocation(l1.x + offset.x, l1.y + 1))) {
            return false
          }

          l1 = l1.copy(x = l1.x + dir.x)
        }
      } else { //if (diff.x < diff.y). Move along the y axis and increment/decrement x when f >= diff.y.
        while(l1.y != l2.y) {
          f += Math.round(diff.x)
          if(f >= diff.y) {
            if(!isTraversable(TestLocation(l1.x + offset.x, l1.y + offset.y))) {
              return false
            }

            l1 = l1.copy(x = l1.x + dir.x)

            if(!isTraversable(l1)) {
              return false
            }

            f -= Math.round(diff.y)
          }

          if(f != 0 && !isTraversable(TestLocation(l1.x + offset.x, l1.y + offset.y))) {
            return false
          }

          if(diff.x == 0 && !isTraversable(l1.copy(y = l1.y + offset.y)) && !isTraversable(l1.copy(x = l1.x + 1, l1.y + offset.y))) {
            return false
          }

          l1 = l1.copy(y = l1.y + dir.y)
        }
      }

      // we made it to the other node!
      true
    }

    override def getNodeNeighbors(n: NodeID) = {
      val loc = terrain(n).location

      var neighborLocs: List[TestLocation] = Nil

      if(loc.x != 0) {
        neighborLocs :+= TestLocation(loc.x - 1, loc.y)
      }

      if(loc.x != Main.WORLD_WIDTH - 1) {
        neighborLocs :+= TestLocation(loc.x + 1, loc.y)
      }

      if(loc.y != 0) {
        neighborLocs :+= TestLocation(loc.x, loc.y - 1)
      }

      if(loc.y != Main.WORLD_HEIGHT - 1) {
        neighborLocs :+= TestLocation(loc.x, loc.y + 1)
      }

      val validLocs = neighborLocs.filter(isTraversable)

      validLocs.map(l => terrain.indexWhere(_.location == l))
        .filter(_ >= 0)
        .map(index => (index -> 1.0f)) // everything only costs 1 to go between since it's a grid
    }

    def isTraversable(location: TestLocation): Boolean = {
      // only the top left corners of non-traversable tiles are non-traversable
      terrain.find(t => t.location == location).map(_.traversable).getOrElse(false)
    }
  }
}

