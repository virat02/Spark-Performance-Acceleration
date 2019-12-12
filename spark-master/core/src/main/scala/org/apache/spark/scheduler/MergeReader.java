package org.apache.spark.scheduler;

import org.apache.spark.SparkEnv;
import org.apache.spark.shuffle.IndexShuffleBlockResolver;
import org.apache.spark.storage.BlockManager;
import org.apache.spark.util.Utils;
import sun.security.util.Length;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Merge reader.
 */
public class MergeReader {
    private int shuffleId;
    private long mapId;
    private File dataFile;
    private File indexFile;
    private ByteBuffer byteBuffer;
    private FileInputStream dataFileInputStream;
    private FileChannel dataFileChannel;
    private FileInputStream indexFileInputStream;
    private FileChannel indexFileChannel;
    private boolean isReadComplete;

    /**
     * Instantiates a new Merge reader.
     *
     * @param shuffleId the shuffle id
     * @param mapId     the map id
     * @param capacity  the capacity
     * @throws FileNotFoundException the file not found exception
     */
    public MergeReader(int shuffleId, long mapId, int capacity) throws FileNotFoundException {
        this.shuffleId= shuffleId;
        this.mapId = mapId;
        BlockManager blockManager = SparkEnv.get().blockManager();
        IndexShuffleBlockResolver blockResolver = new IndexShuffleBlockResolver(SparkEnv.get().conf(), blockManager);
        dataFile = blockResolver.getDataFile(shuffleId, mapId);
        indexFile = blockResolver.getIndexFile(shuffleId, mapId);
        allocateBuffer(capacity);
        dataFileInputStream = openStream(dataFile);
        dataFileChannel = openChannel(dataFileInputStream);
        indexFileInputStream = openStream(indexFile);
        indexFileChannel = openChannel(indexFileInputStream);
    }

    private FileInputStream openStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private FileChannel openChannel(FileInputStream fileInputStream) throws FileNotFoundException {
        return fileInputStream.getChannel();
    }

    /**
     * Allocate buffer.
     *
     * @param capacity the capacity
     */
    public void allocateBuffer(int capacity){
        byteBuffer = ByteBuffer.allocate(capacity);
    }

    /**
     * Read datafile byte buffer.
     *
     * @return the byte buffer
     * @throws IOException the io exception
     */
    public ByteBuffer readDatafile() throws IOException {
        byteBuffer.clear();
        int count = dataFileChannel.read(byteBuffer);
        if((count <= 0)){
            isReadComplete = true;
        }
       // Utils.deserialize(byteBuffer.array());
        return byteBuffer;
    }

    /**
     * Gets index file.
     *
     * @return the index file
     * @throws IOException the io exception
     */
    public ByteBuffer getIndexFile() throws IOException {
        ByteBuffer indexByteBuffer = ByteBuffer.allocate(1024*2000);
        indexFileChannel.read(indexByteBuffer);

        return indexByteBuffer;
    }

    /**
     * Is read complete boolean.
     *
     * @return the boolean
     */
    public boolean isReadComplete(){
        return isReadComplete;
    }

    /**
     * Close channel.
     *
     * @throws IOException the io exception
     */
    public void closeChannel() throws IOException {
        dataFileInputStream.close();
        indexFileInputStream.close();
    }

    /**
     * Close file input stream.
     *
     * @throws IOException the io exception
     */
    public void closeFileInputStream() throws IOException {
        dataFileInputStream.close();
        indexFileChannel.close();
    }

    public List<Long> getLengths() throws IOException {
        indexFileInputStream = new FileInputStream(indexFile);
        DataInputStream in = new DataInputStream(indexFileInputStream);
        List<Long> lengths = new ArrayList<>();
        Long offset = in.readLong();
        while (in.available()>0){
            Long val = in.readLong();
            lengths.add(val-offset);
        }
        in.close();
        indexFileInputStream.close();
        return lengths;
    }

}
