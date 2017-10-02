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
package org.sesquipedalian_dev.util.pathfinding

import org.sesquipedalian_dev.util._

import scala.collection.mutable.ListBuffer

// adapted from reference:
// http://aigamedev.com/open/tutorials/theta-star-any-angle-paths/
// http://lapinozz.github.io/learning/2016/06/07/lazy-theta-star-pathfinding.html
// https://github.com/lapinozz/Lazy-Theta-with-optimization-any-angle-pathfinding
// algorithm for 2d pathfinding in a grid while allowing for continuous / any angle movement within
// non-blocked paths
// our version is tweaked to support the way we're rendering relative to the grid; moving units render
// in the +x and +y directions, so we don't want them moving through tiles in a -x / -y direction from a blocked tile.
//

object Pathfinder {
  type NodeID = Int
  type Cost = Float

  // enum values for which list the node is a member of during the algorithm
  type ListType = Int
  final val NO_LIST: ListType = 0
  final val OPEN_LIST: ListType = 1
  final val CLOSED_LIST: ListType = 2

  // tolerance for floating point equality
  final val EPSILON: Float = .00001f
}

// adaptor class to define some functions needed for the algorithm that might vary depending on grid type
// e.g. hex grid, heterogeneous arbitrary polygons, etc
trait Adapter {
  import Pathfinder._ // get shared types / constants

  def getNodeCount(): Int
  def distance(n1: NodeID, n2: NodeID): Cost
  def lineOfSight(n1: NodeID, n2: NodeID): Boolean
  def getNodeNeighbors(n: NodeID): List[(NodeID, Cost)]
}

// a node on the discrete grid that will be searched
case class Node(
  var h: Pathfinder.Cost,                                //
  var neighbors: List[(Pathfinder.NodeID, Pathfinder.Cost)] = Nil,  // generated list of next nodes to expand after this one
  var searchIndex: Int = 0,                   // used to check if node needs to be retested
  var parent: Pathfinder.NodeID = -1,          // the last node on the path; used to generate the final path
  var g: Pathfinder.Cost = Int.MaxValue,                 //
  var list: Pathfinder.ListType = Pathfinder.NO_LIST                // which list the node is in during the search algorithm
)

// Heap element!? looks like this is probably used with std::list in the source, so we probably want to extend
// Ordered or something
case class HeapElement(
  id: Pathfinder.NodeID,
  g: Pathfinder.Cost, // Used for tie-breaking
  f: Pathfinder.Cost // Main key
) extends Ordered[HeapElement] {
  def compare(that: HeapElement): Int = {
    if(Math.abs(f - that.f) < Pathfinder.EPSILON) {
      Math.round(g - that.g)
    } else {
      Math.round(that.f - f)
    }
  }
}

class Pathfinder(adapter: Adapter, weight: Pathfinder.Cost = 1.0f) {
  import Pathfinder._ // get shared types / constants

  val nodes: ListBuffer[Node] = ListBuffer[Node]()
  var openList: ListBuffer[HeapElement] = ListBuffer[HeapElement]()

  var currentSearch: Int = 0

  /*********************
   * Helper functions
   *********************/
  // construct initial graph
  def generateNodes(): Unit =
  {
    nodes.clear()
    val newList = (0 to adapter.getNodeCount()).map(nodeId => {
      Node(h = 0, neighbors = adapter.getNodeNeighbors(nodeId))
    })
    nodes ++= newList
  }

  // make a node be on the 'pending' list
  def addToOpen(id: NodeID): Unit = {
    val node = nodes(id)

    // if already in the list, remove it first
    if(node.list == OPEN_LIST) {
      val possibleRmIndex = openList.indexWhere(p => p.id == id)
      if(possibleRmIndex >= 0) {
        openList.remove(possibleRmIndex)
      }
    }

    // insert into list and sort
    node.list = OPEN_LIST
    openList += HeapElement(id, node.g, node.g + node.h)
    openList = openList.sorted
  }

  // get the next element from our priority queue?
  def getMin(): HeapElement = {
    openList.head
  }

  // remove the next element from our priority queue
  def popMin(): HeapElement = {
    nodes(openList.head.id).list = CLOSED_LIST
    openList.remove(0)
  }

  // prep us to search for a path between s and goal
  def generateState(s: NodeID, goal: NodeID): Unit = {
    if(nodes(s).searchIndex != currentSearch) {
      nodes(s).searchIndex = currentSearch
      nodes(s).h = adapter.distance(s, goal) * weight
      nodes(s).g = Int.MaxValue
      nodes(s).list = NO_LIST
    }
  }

  /*********************
   * Main Algorithm
   *********************/

  // small adapter around main algorithm to convert between some other type and node IDs
  def search[DataType](start: DataType, end: DataType, idToData: (NodeID) => DataType, dataToId: (DataType) => NodeID): List[DataType] = {
    val path = search(dataToId(start), dataToId(end))
    path.map(idToData)
  }

  def search(startId: NodeID, endId: NodeID): List[NodeID] = {
    openList.clear()

    currentSearch += 1

    generateState(startId, endId)
    generateState(endId, endId)

    nodes(startId).g = 0
    nodes(startId).parent = startId

    addToOpen(startId)

    while(openList.nonEmpty && nodes(endId).g > getMin().f + EPSILON) {
      val currId = popMin().id

      // Lazy Theta* assumes that there is always line-of-sight from the parent of an expanded state to a successor state.
      // When expanding a state, check if this is true.
      if(!adapter.lineOfSight(nodes(currId).parent, currId)) {
        // Since the previous parent is invalid, set g-value to infinity.
        nodes(currId).g = Int.MaxValue

        // Go over potential parents and update its parent to the parent that yields the lowest g-value for s.
        nodes(currId).neighbors.foreach(p => {
          val (newParent, nCost) = p

          generateState(newParent, endId)
          if(nodes(newParent).list == CLOSED_LIST) {
            val newG: Cost = nodes(newParent).g + nCost
            if(newG < nodes(currId).g) {
              nodes(currId).g = newG
              nodes(currId).parent = newParent
            }
          }
        })
      }

      nodes(currId).neighbors.foreach(p => {
        val (neighborId, _) = p

        generateState(neighborId, endId)

        val newParent: NodeID = nodes(currId).parent

        if(nodes(neighborId).list != CLOSED_LIST) {
          val newG: Cost = nodes(newParent).g + adapter.distance(newParent, neighborId)

          if(newG + EPSILON < nodes(neighborId).g) {
            nodes(neighborId).g = newG
            nodes(neighborId).parent = newParent
            addToOpen(neighborId)
          }
        }
      })
    }

    if(nodes(endId).g < Int.MaxValue) {
      var lst: List[NodeID] = List(endId)
      var curr: NodeID = endId
      while(curr != startId) {
        lst = curr +: lst
        curr = nodes(curr).parent
      }
      lst
    } else {
      Nil
    }
  }

  /*********************
   * initialize the graph
   *********************/
  generateNodes() // construct graph
}
