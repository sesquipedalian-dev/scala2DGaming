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
package org.sesquipedalian_dev.util.registry

import scala.collection.mutable

// object registry
// register game objects by string tags and provides accessor methods for manipulating that collection
// makes it easier to destroy an object by unregistering it with everything at once
class ObjectRegistry {
  val collection: mutable.HashMap[String, List[AnyRef]] = mutable.HashMap()

  def register(x: AnyRef, tag: String): Unit = register(x, tag :: Nil)
  def register(x: AnyRef, tags: List[String]): Unit = {
    collection.synchronized({
      tags.foreach(t => {
        collection += (t -> (collection.getOrElse(t, Nil) :+ x))
      })
    })
  }

  def unregister(x: AnyRef, tag: String): Unit = {
    collection.synchronized(({
      val colCp = collection.toMap
      collection.clear
      collection ++= colCp.map({
        case (thisTag, lst) if thisTag == tag => (thisTag -> lst.filterNot(_ == x))
        case p => p
      })
    }))
  }

  def unregister(x: AnyRef): Unit = {
    collection.synchronized(({
      val colCp = collection.toMap
      collection.clear
      collection ++= colCp.map(p => {
        (p._1 -> p._2.filterNot(_ == x))
      })
    }))
  }

  def objects[A](tag: String)(implicit mf: Manifest[A]): List[A] = {
    collection.synchronized({
      collection.getOrElse(tag, Nil).collect({
        case x: A => x
      })
    })
  }

  def objectsNoMF[A](tag: String): List[A] = {
    collection.synchronized({
      collection.getOrElse(tag, Nil).map(_.asInstanceOf[A])
    })
  }

  def singleton[A](tag: String)(implicit mf: Manifest[A]): Option[A] = {
    collection.synchronized({
      collection.getOrElse(tag, Nil).collect({
        case x: A => x
      }).headOption
    })
  }

  def singletonNoMF[A](tag: String): Option[A] = {
    collection.synchronized({
      collection.getOrElse(tag, Nil).headOption.map(_.asInstanceOf[A])
    })
  }
}
