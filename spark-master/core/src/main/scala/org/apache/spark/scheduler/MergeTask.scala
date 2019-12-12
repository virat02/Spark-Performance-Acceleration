package org.apache.spark.scheduler

import java.lang.management.ManagementFactory
import java.nio.ByteBuffer
import java.util.Properties

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, ShuffleDependency, SparkEnv, TaskContext}

import scala.collection.JavaConverters._



private[spark] class MergeTask(
                                     stageId: Int,
                                     mapId : Int,
                                     stageAttemptId: Int,
                                     taskBinary: Broadcast[Array[Byte]],
                                     partition: Partition,
                                     @transient private var locs: Seq[TaskLocation],
                                     localProperties: Properties,
                                     serializedTaskMetrics: Array[Byte],
                                     jobId: Option[Int] = None,
                                     appId: Option[String] = None,
                                     appAttemptId: Option[String] = None,
                                     isBarrier: Boolean = false)
  extends Task[MapStatus](stageId, stageAttemptId, partition.index, localProperties,
    serializedTaskMetrics, jobId, appId, appAttemptId, isBarrier)
    with Logging {
  @transient private val preferredLocs: Seq[TaskLocation] = {
    if (locs == null) Nil else locs.toSet.toSeq
  }

  override def runTask(context: TaskContext): MapStatus ={
    logInfo("Starting with the merge task!")
    val threadMXBean = ManagementFactory.getThreadMXBean
    val deserializeStartTimeNs = System.nanoTime()
    val deserializeStartCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime
    } else 0L
    val ser = SparkEnv.get.closureSerializer.newInstance()
    val rddAndDep = ser.deserialize[(RDD[_], ShuffleDependency[_, _, _])](
      ByteBuffer.wrap(taskBinary.value), Thread.currentThread.getContextClassLoader)
    _executorDeserializeTimeNs = System.nanoTime() - deserializeStartTimeNs
    _executorDeserializeCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
      threadMXBean.getCurrentThreadCpuTime - deserializeStartCpuTime
    } else 0L
    val dep  = rddAndDep._2;
    val capacity = 1024*9000;
    val mergeReader: MergeReader = new MergeReader(dep.shuffleId, context.taskAttemptId()-1, 1024*1000);

    val mergeWriter: MergeWriter = new MergeWriter(dep.shuffleId, context.taskAttemptId());
    while(!mergeReader.isReadComplete)
    {
     mergeWriter.writeDataFile(mergeReader.readDatafile());
    }
    mergeReader.closeChannel();
    mergeReader.closeFileInputStream();
    val lengths = mergeReader.getLengths.asScala
    var l = new Array[Long](lengths.length)
    var i = 0
    for(x <- lengths){
      l(i) = x;
      i += 1;
    }
    val rl: Array[java.lang.Long] = mergeReader.getLengths.asScala.toArray
    mergeWriter.writeIndexFile(rl);
    mergeWriter.closeChannel();
    mergeWriter.closeFileOutputStream()

    logInfo(" Files Written by Merge   " + dep.shuffleId + "  ----  " + context.taskAttemptId())
    MapStatus.apply(SparkEnv.get.blockManager.shuffleServerId, l, context.taskAttemptId())
  }
  override def preferredLocations: Seq[TaskLocation] = preferredLocs

  override def toString: String = "MergeTask(%d, %d)".format(stageId, partitionId)


}


//package org.apache.spark.scheduler
//
//import java.lang.management.ManagementFactory
//import java.nio.ByteBuffer
//import java.util.Properties
//
//import org.apache.spark.broadcast.Broadcast
//import org.apache.spark.internal.Logging
//import org.apache.spark.rdd.RDD
//import org.apache.spark.{Partition, ShuffleDependency, SparkEnv, TaskContext}
//
//
//
//private[spark] class MergeTask(
//                                stageId: Int,
//                                mapId : Int,
//                                stageAttemptId: Int,
//                                taskBinary: Broadcast[Array[Byte]],
//                                partition: Partition,
//                                @transient private var locs: Seq[TaskLocation],
//                                localProperties: Properties,
//                                serializedTaskMetrics: Array[Byte],
//                                jobId: Option[Int] = None,
//                                appId: Option[String] = None,
//                                appAttemptId: Option[String] = None,
//                                isBarrier: Boolean = false)
//  extends Task[MapStatus](stageId, stageAttemptId, partition.index, localProperties,
//    serializedTaskMetrics, jobId, appId, appAttemptId, isBarrier)
//    with Logging {
//  @transient private val preferredLocs: Seq[TaskLocation] = {
//    if (locs == null) Nil else locs.toSet.toSeq
//  }
//
//  override def runTask(context: TaskContext): MapStatus ={
//    logInfo("Starting with the merge task!")
//    val threadMXBean = ManagementFactory.getThreadMXBean
//    val deserializeStartTimeNs = System.nanoTime()
//    val deserializeStartCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
//      threadMXBean.getCurrentThreadCpuTime
//    } else 0L
//    val ser = SparkEnv.get.closureSerializer.newInstance()
//    val rddAndDep = ser.deserialize[(RDD[_], ShuffleDependency[_, _, _])](
//      ByteBuffer.wrap(taskBinary.value), Thread.currentThread.getContextClassLoader)
//    _executorDeserializeTimeNs = System.nanoTime() - deserializeStartTimeNs
//    _executorDeserializeCpuTime = if (threadMXBean.isCurrentThreadCpuTimeSupported) {
//      threadMXBean.getCurrentThreadCpuTime - deserializeStartCpuTime
//    } else 0L
//    val dep  = rddAndDep._2;
//    val capacity = 1024*9000;
//    val n = SparkEnv.get.nValue
//    var seq = new scala.collection.mutable.MutableList[MergeReader]()
//    var u =0
//    for(y <- (1 to n).reverse){
//      seq += new MergeReader(dep.shuffleId, context.taskAttemptId()-y, capacity)
//    }
//    val mergeWriter: MergeWriter= new MergeWriter(dep.shuffleId, context.taskAttemptId())
//    val merger : Merger = new Merger(seq,mergeWriter)
//    val lengths = merger.merge();
//
//    mergeWriter.closeChannel()
//    mergeWriter.closeFileOutputStream()
//
//    logInfo("......................Files..............." + dep.shuffleId + "     ----   " + context.taskAttemptId())
//
//    MapStatus.apply(SparkEnv.get.blockManager.shuffleServerId, lengths, context.taskAttemptId())
//  }
//  override def preferredLocations: Seq[TaskLocation] = preferredLocs
//
//  override def toString: String = "MergeTask(%d, %d)".format(stageId, partitionId)
//
//
//}
