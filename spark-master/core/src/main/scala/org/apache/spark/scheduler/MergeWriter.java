package org.apache.spark.scheduler;

import org.apache.spark.SparkEnv;
import org.apache.spark.shuffle.IndexShuffleBlockResolver;
import org.apache.spark.storage.BlockManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * The type Merge writer.
 */
public class MergeWriter {
    private int shuffleId;
    private long mapId;
    private File dataFile;
    private File indexFile;
    private FileOutputStream dataFileOutputStream;
    private FileChannel dataFileChannel;
    private FileOutputStream indexFileOutputStream;
    private FileChannel indexFileChannel;

    /**
     * Instantiates a new Merge writer.
     *
     * @param shuffleId the shuffle id
     * @param mapId     the map id
     * @throws FileNotFoundException the file not found exception
     */
    public MergeWriter(int shuffleId, long mapId) throws FileNotFoundException {
        this.shuffleId = shuffleId;
        this.mapId = mapId;
        BlockManager blockManager = SparkEnv.get().blockManager();
        IndexShuffleBlockResolver blockResolver = new IndexShuffleBlockResolver(SparkEnv.get().conf(), blockManager);
        dataFile = blockResolver.getDataFile(shuffleId, mapId);
        indexFile = blockResolver.getIndexFile(shuffleId, mapId);
        dataFileOutputStream = openStream(dataFile);
        dataFileChannel = openChannel(dataFileOutputStream);
        indexFileOutputStream = openStream(indexFile);
        indexFileChannel = openChannel(indexFileOutputStream);
    }

    private FileOutputStream openStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file, true);
    }

    private FileChannel openChannel(FileOutputStream fileInputStream) {
        return fileInputStream.getChannel();
    }

    /**
     * Write data file.
     *
     * @param dataFileBuffer the data file buffer
     * @throws IOException the io exception
     */
    public void writeDataFile(ByteBuffer dataFileBuffer) throws IOException {
        dataFileBuffer.flip();
        dataFileChannel.write(dataFileBuffer);
    }

//    public void writeIndexFile(ByteBuffer indexFileBuffer) throws IOException {
//        indexFileBuffer.flip();
//        indexFileChannel.write(indexFileBuffer);
//    }
    public void writeIndexFile(Long[] lengths) throws IOException {

        DataOutputStream out = new DataOutputStream(indexFileOutputStream);
        //indexFileBuffer.flip();
                Long offset = 0L;
                out.writeLong(offset);
        for (Long length : lengths) {
            offset += length;
            out.writeLong(offset);
        }
        out.close();
    }


    /**
     * Close channel.
     *
     * @throws IOException the io exception
     */
    public void closeChannel() throws IOException {
        dataFileChannel.close();
        indexFileChannel.close();
    }

    /**
     * Close file output stream.
     *
     * @throws IOException the io exception
     */
    public void closeFileOutputStream() throws IOException {
        dataFileOutputStream.close();
        indexFileOutputStream.close();
    }
}
