package com.inmobi.databus;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.TaskAttemptID;

public class DatabusConfig {

  public static final String DATABUS_ROOT_DIR = "/databus/";
  public static final String DATABUS_SYSTEM_DIR = DATABUS_ROOT_DIR + "system/";
  public static final String TMP = DATABUS_SYSTEM_DIR + "tmp";
  public static final String TRASH = DATABUS_SYSTEM_DIR + "trash";
  // public static final String CONSUMER = DATABUS_SYSTEM_DIR + "consumer";
  public static final String DATA_DIR = DATABUS_ROOT_DIR + "data/";
  public static final String PUBLISH_DIR = DATABUS_ROOT_DIR + "streams/";

  private static final String CONFIG = "databus.xml";
  private final Map<String, Cluster> clusters = new HashMap<String, Cluster>();
  private final Map<String, Stream> streams = new HashMap<String, Stream>();
  private final Cluster destinationCluster;
  private final String wfId;
  private final Configuration hadoopConf;
  private final static Path tmpPath = new Path(TMP);

  public DatabusConfig() {
    this("uj1", "1");
  }

  public DatabusConfig(String destCluster, String wfId) {
    // load configuration

    Cluster uj1 = new Cluster("uj1", "hdfs://localhost:54310", new HashSet<ReplicatedStream>());
    clusters.put(uj1.name, uj1);
    
    Stream beacon = new Stream("beacon", new HashSet<String>());
    beacon.sourceClusters.add(uj1.name);
    streams.put(beacon.name, beacon);

    this.destinationCluster = clusters.get("uj1");
    this.hadoopConf = new Configuration();
    this.wfId = wfId;
  }

  public Configuration getHadoopConf() {
    return this.hadoopConf;
  }

  public Cluster getDestinationCluster() {
    return destinationCluster;
  }

  public Map<String, Cluster> getClusters() {
    return clusters;
  }

  public Map<String, Stream> getStreams() {
    return streams;
  }

  public static Path getTmpPath() {
    return tmpPath;
  }

  public Path getTrashPath() {
    return new Path(TRASH);
  }

  public Path getDataDir() {
    return new Path(DATA_DIR);
  }

  public static Path getTaskAttemptTmpDir(TaskAttemptID attemptId) {
    return new Path(getJobTmpDir(attemptId.getJobID()), attemptId.toString());
  }

  public static Path getJobTmpDir(JobID jobId) {
    return new Path(tmpPath, jobId.toString());
  }

  public String getDestDir(String category) throws IOException {
    Date date = new Date(System.currentTimeMillis());
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    String dest = this.destinationCluster.hdfsUrl + File.separator
        + PUBLISH_DIR + File.separator + category + File.separator
        + calendar.get(Calendar.YEAR) + File.separator
        + (calendar.get(Calendar.MONTH) + 1) + File.separator
        + calendar.get(Calendar.DAY_OF_MONTH) + File.separator
        + calendar.get(Calendar.HOUR_OF_DAY) + File.separator
        + calendar.get(Calendar.MINUTE);
    return dest;
  }

  public static class Cluster {
    public final String name;
    public final String hdfsUrl;
    public final Set<ReplicatedStream> replicatedStreams;

    Cluster(String name, String hdfsUrl, Set<ReplicatedStream> replicatedStreams) {
      this.name = name;
      this.hdfsUrl = hdfsUrl;
      this.replicatedStreams = replicatedStreams;
    }
  }

  public static class ReplicatedStream extends Stream {
    public final int retentionHours;
    public final String offset;

    public ReplicatedStream(String name, Set<String> sourceClusters,
        int retentionHours, String offset) {
      super(name, sourceClusters);
      this.retentionHours = retentionHours;
      this.offset = offset;
    }
  }

  public static class Stream {
    public final String name;
    public final Set<String> sourceClusters;

    public Stream(String name, Set<String> sourceClusters) {
      super();
      this.name = name;
      this.sourceClusters = sourceClusters;
    }
  }
}
