package scala.spores.serialization.run.capture

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class JavaCollectionsSpec {
  @Test
  def `Capture java hashmap`(): Unit = {
    val javaMap = new java.util.HashMap[String, String]()
    javaMap.put("Hello", "World")
    val s = spore {
      val captured = javaMap
      () => captured.get("Hello")
    }
    assert(s() == "World")
  }

  @Test
  def `Capture java hashset`(): Unit = {
    val javaSet = new java.util.HashSet[String]()
    javaSet.add("Hello, World!")
    val s = spore {
      val captured = javaSet
      () => captured.contains("Hello, World!")
    }
    assert(s())
  }

  @Test
  def `Capture java stack`(): Unit = {
    val javaStack = new java.util.Stack[String]()
    javaStack.add("Hello, World!")
    val s = spore {
      val captured = javaStack
      () => captured.contains("Hello, World!")
    }
    assert(s())
  }

  @Test
  def `Capture java array list`(): Unit = {
    val javaList = new java.util.ArrayList[String]()
    javaList.add("Hello, World!")
    val s = spore {
      val captured = javaList
      () => captured.contains("Hello, World!")
    }
    assert(s())
  }

  @Test
  def `Capture java linked list`(): Unit = {
    val javaList = new java.util.LinkedList[String]()
    javaList.add("Hello, World!")
    val s = spore {
      val captured = javaList
      () => captured.contains("Hello, World!")
    }
    assert(s())
  }
}
