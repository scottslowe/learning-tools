# Building and running

    $ hadoop fs -rmr /user/gutenberg/output
    $ hadoop fs -rmr /user/gutenberg/input
    $ cd hadoop/wordcount-hdfs-copy
    $ mvn clean package appassembler:assemble
    $ sh ./target/appassembler/bin/wordcount

You should see the input file copied into the HDFS directory
  /user/gutenberg/input/word

$ hadoop fs -ls /user/gutenberg/input/word

Found 1 items
-rw-r--r--   3 mpollack supergroup      51384 2013-02-26 00:26 /user/gutenberg/input/word/nietzsche-chapter-1.txt


and the output file from the MapReduce WordCount job in the HDFS directory
  /user/gutenberg/input/work

$ hadoop fs -ls /user/gutenberg/output/word
Warning: $HADOOP_HOME is deprecated.

Found 2 items
-rw-r--r--   3 mpollack supergroup          0 2013-02-26 00:26 /user/gutenberg/output/word/_SUCCESS
-rw-r--r--   3 mpollack supergroup      31752 2013-02-26 00:26 /user/gutenberg/output/word/part-r-00000


you can 'cat' the part-r-00000 file to see the results

