package org.apache.spark.scheduler

import scala.collection.JavaConverters._
import scala.collection.mutable._

private[spark] class Merger(readers: scala.collection.mutable.MutableList[MergeReader], writer: MergeWriter) {

  def merge(): Array[Long] ={
    var ifDone = readers.zipWithIndex.map(ele => ele._2).toSet
    var lengths = Array[Long]()
    for (r <- readers){
      val rl: Array[java.lang.Long] = r.getLengths.asScala.toArray
      var idx = 0
      for(v  <- rl){
        if(lengths.isEmpty || lengths.size <= idx){
          lengths :+= v.asInstanceOf[Long]
        }else{
          lengths(idx) += v.asInstanceOf[Long]
        }
        idx +=1
      }
    }
    while(ifDone.nonEmpty){
      var done : Set[Int] = Set()
      for (ifDoneidx: Int <- ifDone){
        writer.writeDataFile(readers(ifDoneidx).readDatafile())
        if(readers(ifDoneidx).isReadComplete) {
          done += ifDoneidx
        }
      }
      ifDone = ifDone.diff(done)
    }
   // val collection = asJavaCollection(lengths)
   val javaLengths = new Array[java.lang.Long](lengths.length)
    var i =0;
    for(x <- lengths) {

      javaLengths(i) = x;
      i += 1;
    }
      writer.writeIndexFile(javaLengths);


//    var offset = 0L
//    out.writeLong(offset)
//    for (length <- lengths) {
//      offset += length
//      out.writeLong(offset)
//    }
//    {
//      out.close()
//    }

//    if (indexFile.exists()) {
//      indexFile.delete()
//    }
//    if (dataFile.exists()) {
//      dataFile.delete()
//    }
//    if (!indexTmp.renameTo(indexFile)) {
//      throw new IOException("fail to rename file " + indexTmp + " to " + indexFile)
//    }

    lengths

  }
}