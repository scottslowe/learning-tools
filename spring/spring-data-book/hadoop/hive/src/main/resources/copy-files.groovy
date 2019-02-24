//requires three variables, localSourceFile and hdfsInputDir, hdfsOutputDir 

//remoteHdfsDir = "/data/apache/logs/"
//localSourceFile = "./data/apache.log"

// use the shell (made available under variable fsh)
if (!fsh.test(hdfsInputDir)) {
   fsh.mkdir(hdfsInputDir); 
   fsh.copyFromLocal(localSourceFile, hdfsInputDir); 
   fsh.chmod(700, hdfsInputDir)
}
if (fsh.test(hdfsOutputDir)) {
   fsh.rmr(hdfsOutputDir)
}