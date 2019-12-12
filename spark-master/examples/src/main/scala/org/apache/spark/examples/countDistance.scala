/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package org.apache.spark.examples

import org.apache.log4j.LogManager
import util.control.Breaks._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object countDistance {

  def getMin(a: Int, b: Int): Int = {
    if (a == -1) return b
    if (b == -1) return a

    if (a < b) a else b
  }

  def main(args: Array[String]) {

    val logger: org.apache.log4j.Logger = LogManager.getRootLogger

//    if (args.length != 4) {
//      logger.
//        error("Usage:\npr.SingleSourceShortestPath <input dir> <output dir> <threshold> <source>")
//      System.exit(1)
//    }

    val conf = new SparkConf().setAppName("SingleSourceShortestPath")
    //    conf.set("spark.eventLog.enabled","true")
    //    conf.set("spark.eventLog.dir","eventlog")
    val spark = new SparkContext(conf)

    // Read the input
    val lines = spark.textFile("./examples/src/main/scala/org/apache/spark/examples/input")

    // Get the MAX threshold
    val MAX = 20

    // Get the source
    val source = 0

    // Create the graph RDD as (node, adjacency list)
    val graph = lines.map { s =>
      val parts = s.split(",")
      (parts(0), parts(1))
    }
      // pre-process the data to remove duplicates
      .distinct()

      // get the adjacency list
      .groupByKey()

      // cache the RDD
      .cache()

    // Get the initial distance RDD
    var distances = graph
      .map( x => if (x._1.toInt == source) (x._1, 0) else (x._1, -1))

    // distances will update after each iteration
    var temp = distances

    breakable {
      while (true) {
        // distances, through different existing paths, for all the nodes reached so far
        temp = graph.join(temp)
          .filter(x => x._2._2 != -1)
          .flatMap(x => x._2._1
            .map(y => (y, x._2._2 + 1)))

        // updated distances for all nodes reached so far
        val distances1 = temp.union(distances).reduceByKey((x, y) => getMin(x, y))

        // check if any distance for any node has changed,
        // as well as all nodes are visited from the given source to all it's targets
        val done = distances.join(distances1)
          .map { case (x, y) => y._1 == y._2 }
          .reduce((x, y) => x && y)

        // Update the distances RDD only if it has changed from it's previous state
        if (!done) distances = distances1 else break
      }
    }

    // save the output
     print(distances.toDebugString);
     distances.collect().foreach(x => println(x))
  }
}

// scalastyle:on println
