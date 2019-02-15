Validating your virtual machine setup
=====================================

After the `vagrant up` command has completed, you'll have a CentOS
virtual machine with the following installed:

* Hadoop HDFS
* Hadoop YARN
* Hive
* Spark
* Hive on Tez

Let's take a look at each one and validate that it's installed and
setup as expected.

SSH into your virtual machine.

    vagrant ssh

Run an example MapReduce job.

    # create a directory for the input file
    hdfs dfs -mkdir /wordcount-input

    # generate sample input and write it to HDFS
    echo "hello dear world hello" | hdfs dfs -put - /wordcount-input/hello.txt

    # run the MapReduce word count example
    hadoop jar /usr/local/hadoop/share/hadoop/mapreduce/hadoop*example*.jar \
      wordcount /wordcount-input /wordcount-output

    # validate the output of the job - you should see the following in the output:
    #     dear   1
    #     hello  2
    #     world  1
    hdfs dfs -cat /wordcount-output/part*

Launch the Hive shell.

    $ hive

Create a table and run a query over it.

    CREATE EXTERNAL TABLE wordcount (
        word STRING,
        count INT
    )
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
    LOCATION '/user/ubuntu/wordcount-output';

    select * from wordcount order by count;

To run the same query using Tez: -

    set hive.execution.engine=tez;

Next launch the interactive Spark shell.

    spark-shell --master yarn-client

Run word count in Spark.

    // enter paste mode
    :paste
    sc.textFile("hdfs:///wordcount-input/hello.txt")
       .flatMap(line => line.split(" "))
       .map(word => (word, 1))
       .reduceByKey(_ + _)
       .collect.foreach(println)

    <ctrl-D>

    sc.stop
