# Building and running

    $ cd hadoop/batch-wordcount
    $ mvn clean package appassembler:assemble
    $ sh ./target/appassembler/bin/batch-wordcount

The file in the local directory, data/nietzsche-chapter-1.txt will be loaded 
into HDFS under the directory /user/gutenberg/input/word

The wordcount MapReduce job will put its results in the directory
/user/gutenberg/output/word

You can view the output by using the hadoop command line utility

    $ hadoop fs -cat /user/gutenberg/output/word/part*



