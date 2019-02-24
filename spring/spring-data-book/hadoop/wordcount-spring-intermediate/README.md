# Building and running

    $ hadoop dfs -copyFromLocal /tmp/gutenberg/download /user/gutenberg/input
    $ hadoop dfs -rmr /user/gutenberg/qa/output
    $ cd hadoop/wordcount-spring-intermediate
    $ mvn clean package appassembler:assemble
    $ export ENV=qa
    $ sh ./target/appassembler/bin/wordcount

