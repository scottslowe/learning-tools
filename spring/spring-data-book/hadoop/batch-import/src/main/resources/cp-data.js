//
// Simple script that prepares the HDFS env for a Hadoop job:
//
// 1. deletes the input/output paths in HDFS (in case they exist)
// 2. copies a local resource to HDFS
//
// required params are:
// * inputPath
// * outputPath
// * localResource


// 'hack' default permissions to make Hadoop work on Windows
if (java.lang.System.getProperty("os.name").startsWith("Windows")) {
    // 0655 = -rwxr-xr-x
     org.apache.hadoop.mapreduce.JobSubmissionFiles.JOB_DIR_PERMISSION.fromShort(0655)
     org.apache.hadoop.mapreduce.JobSubmissionFiles.JOB_FILE_PERMISSION.fromShort(0655)
}


// delete job paths
if (fsh.test(inputPath)) { fsh.rmr(inputPath) }
if (fsh.test(outputPath)) { fsh.rmr(outputPath) }

// copy local resource using the streams directly (to be portable across envs)
inStream = cl.getResourceAsStream(localResource)
org.apache.hadoop.io.IOUtils.copyBytes(inStream, fs.create(inputPath), cfg)