# Building and running

    $ cd hadoop/wordcount
    $ mvn clean package appassembler:assemble
    $ sh ./target/appassembler/bin/wordcount hdfs://localhost:9000/user/gutenberg/input hdfs://localhost:9000/user/gutenberg/output

