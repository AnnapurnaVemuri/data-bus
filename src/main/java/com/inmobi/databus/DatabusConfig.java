package com.inmobi.databus;

import com.inmobi.databus.utils.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;

import java.io.*;
import java.util.*;

public class DatabusConfig {

  /*
    public static String DATABUS_ROOT_DIR = "/databus/";
    public static String DATABUS_SYSTEM_DIR = DATABUS_ROOT_DIR + "system/";
    public static String TMP = DATABUS_SYSTEM_DIR + "tmp";
    public static String TRASH = DATABUS_SYSTEM_DIR + "trash";
    public static String CONSUMER = DATABUS_SYSTEM_DIR + "consumers";
    public static String DATA_DIR = DATABUS_ROOT_DIR + "data/";
    public static String PUBLISH_DIR = DATABUS_ROOT_DIR + "streams/";
  */
  private final Map<String, Cluster> clusters;
  private final Map<String, Stream> streams;
  private String zkConnectionString;


  public static int globalRetention;

  public DatabusConfig(int globalRetention, String rootDir, String zkConnectionString, Map<String, Stream> streams,
                       Map<String, Cluster> clusterMap) {
    //this.hadoopConf = new Configuration();
    //hadoopConf.set("fs.default.name", destinationCluster.getHdfsUrl());
    this.globalRetention = globalRetention;
    this.zkConnectionString = zkConnectionString;
    this.streams = streams;
    this.clusters = clusterMap;
  }

  public static int getGlobalRetention() {
    return globalRetention;
  }


  public String getZkConnectionString() {
    return zkConnectionString;
  }

  public Map<String, Cluster> getClusters() {
    return clusters;
  }

  public Map<String, Stream> getStreams() {
    return streams;
  }

  public static class Cluster {
    private final String zkConnectionString;
    private final String name;

    private final String rootDir;
    private final String hdfsUrl;
    private final Map<String, ConsumeStream> consumeStreams;
    private final Set<String> sourceStreams;
    private final Configuration hadoopConf;
    // first time starting time
    private long lastCommitTime = System.currentTimeMillis();

    Cluster(String name, String rootDir,
            String hdfsUrl, String jtUrl, Map<String,
            ConsumeStream> consumeStreams, Set<String> sourceStreams, String zkConnectionString) {
      this.name = name;
      this.hdfsUrl = hdfsUrl;
      this.rootDir = rootDir;
      this.hadoopConf = new Configuration();
      this.hadoopConf.set("mapred.job.tracker", jtUrl);
      this.hadoopConf.set("databus.tmp.path", getTmpPath().toString());
      this.consumeStreams = consumeStreams;
      this.sourceStreams = sourceStreams;
      this.hadoopConf.set("fs.default.name", hdfsUrl);
      this.zkConnectionString = zkConnectionString;
    }


    public String getRootDir() {
      return hdfsUrl + File.separator + rootDir + File.separator;
    }

    public String getLocalFinalDestDirRoot() {
      String dest = hdfsUrl + File.separator + rootDir + File.separator + "streams_local"
              + File.separator;
      return dest;
    }

    public String getLocalDestDir(String category, long commitTime)
            throws IOException {
      Date date = new Date(commitTime);
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      String dest = hdfsUrl + File.separator + rootDir + File.separator
              + "streams_local" + File.separator + category + File.separator
              + calendar.get(Calendar.YEAR) + File.separator
              + (calendar.get(Calendar.MONTH) + 1) + File.separator
              + calendar.get(Calendar.DAY_OF_MONTH) + File.separator
              + calendar.get(Calendar.HOUR_OF_DAY) + File.separator
              + calendar.get(Calendar.MINUTE);
      return dest;
    }

    public synchronized long getCommitTime() {
      long current = System.currentTimeMillis();
      if (current - lastCommitTime >= 60000) {
        lastCommitTime = current;
      }
      return lastCommitTime;
    }

    public String getZkConnectionString() {
      return zkConnectionString;
    }

    public String getHdfsUrl() {
      return hdfsUrl;
    }

    public Configuration getHadoopConf() {
      return hadoopConf;
    }

    public String getName() {
      return name;
    }

    public String getUnqaulifiedFinalDestDirRoot() {
      String dest = rootDir + File.separator + "streams" + File.separator;
      return dest;
    }

    public String getFinalDestDirRoot() {
      String dest = hdfsUrl + File.separator + rootDir + File.separator + "streams"
              + File.separator;
      return dest;
    }

    public String getDateTimeDestDir(String category, long commitTime) {
      Date date = new Date(commitTime);
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      String dest = category + File.separator
              + calendar.get(Calendar.YEAR) + File.separator
              + (calendar.get(Calendar.MONTH) + 1) + File.separator
              + calendar.get(Calendar.DAY_OF_MONTH) + File.separator
              + calendar.get(Calendar.HOUR_OF_DAY) + File.separator
              + calendar.get(Calendar.MINUTE);
      return dest;
    }


    public String getFinalDestDir(String category, long commitTime)
            throws IOException {
      Date date = new Date(commitTime);
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      String dest = hdfsUrl + File.separator +
              rootDir + File.separator + "streams"
              + File.separator + category + File.separator
              + calendar.get(Calendar.YEAR) + File.separator
              + (calendar.get(Calendar.MONTH) + 1) + File.separator
              + calendar.get(Calendar.DAY_OF_MONTH) + File.separator
              + calendar.get(Calendar.HOUR_OF_DAY) + File.separator
              + calendar.get(Calendar.MINUTE);
      return dest;
    }

    public String getFinalDestDirTillHour(String category, long commitTime)
            throws IOException {
      Date date = new Date(commitTime);
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      String dest = hdfsUrl + File.separator +
              rootDir + File.separator + "streams"
              + File.separator + category + File.separator
              + calendar.get(Calendar.YEAR) + File.separator
              + (calendar.get(Calendar.MONTH) + 1) + File.separator
              + calendar.get(Calendar.DAY_OF_MONTH) + File.separator
              + calendar.get(Calendar.HOUR_OF_DAY) + File.separator;

      return dest;
    }


    public Map<String, ConsumeStream> getConsumeStreams() {
      return consumeStreams;
    }

    public Set<String> getMirroredStreams() {
      Set<String> mirroredStreams = new HashSet<String>();
      for(ConsumeStream consumeStream : getConsumeStreams().values()) {
        if (!consumeStream.isPrimary())
          mirroredStreams.add(consumeStream.getName());
      }
      return mirroredStreams;
    }

    public Set<String> getPrimaryStreams() {
      Set<String> primaryStreams = new HashSet<String>();
      for(ConsumeStream consumeStream : getConsumeStreams().values()) {
        if (consumeStream.isPrimary())
          primaryStreams.add(consumeStream.getName());
      }
      return primaryStreams;

    }

    public Set<String> getSourceStreams() {
      return sourceStreams;
    }

    public Path getTrashPath() {
      return new Path(getSystemDir() + File.separator +
              "trash");
    }

    public Path getTrashPathWithDate() {
      return new Path(getTrashPath(), CalendarHelper.getCurrentDateAsString());
    }

    public Path getDataDir() {
      return new Path(hdfsUrl + File.separator +
              rootDir + File.separator +
              "data");
    }

    public Path getConsumePath(Cluster consumeCluster) {
      return new Path(getSystemDir()
              + File.separator + "consumers" + File.separator +
              consumeCluster.name);
    }

    public Path getMirrorConsumePath(Cluster consumeCluster) {
      return new Path(getSystemDir()
              + File.separator + "mirrors" + File.separator +
              consumeCluster.name);
    }



    public Path getTmpPath() {
      return new Path(getSystemDir() + File.separator +
              "tmp");
    }

    private String getSystemDir() {
      return hdfsUrl + File.separator +
              rootDir + File.separator +
              "system";
    }
  }

  public static class ConsumeStream {
    private final int retentionInDays;
    private final String name;
    private Boolean isPrimary = false;

    public ConsumeStream(String name, int retentionInDays, Boolean isPrimary) {
      this.name = name;
      this.retentionInDays = retentionInDays;
      this.isPrimary = isPrimary;
    }

    public boolean isPrimary() {
      return isPrimary;
    }

    public void setPrimary(boolean primary) {
      isPrimary = primary;
    }


    public String getName() {
      return name;
    }
    public int getRetentionInDays() {
      if (retentionInDays > 0)
        return retentionInDays;
      else if(getGlobalRetention() > 0) {
        return getGlobalRetention(); //Returns Global retention
      }
      else
        return 2; //default to 2 days

    }
  }

  public static class Stream {
    private final String name;
    //Map of ClusterName, Retention for a stream
    private final Map<String, Integer> sourceClusters;


    public Stream(String name, Map<String, Integer> sourceClusters) {
      super();
      this.name = name;
      this.sourceClusters = sourceClusters;
    }

    public int getRetentionInDays(String clusterName) {
      int clusterRetention = sourceClusters.get(clusterName).intValue();
      if (clusterRetention > 0)
        return clusterRetention;
      else if(getGlobalRetention() > 0)
        return getGlobalRetention();
      else
        return 2;

    }

    public Set<String> getSourceClusters() {
      return sourceClusters.keySet();
    }

    public String getName() {
      return name;
    }


  }

}
