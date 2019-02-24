# Building and running

    $ cd hadoop/batch-extract
    $ mvn clean package appassembler:assemble

# Start the database (from a separate command window - use 'Ctrl-c' to stop database)

    $ sh ./target/appassembler/bin/start-database

View the products in the PRODUCT table using the web UI, should be empty

    Driver Class:	org.h2.Driver
    JDBC URL:		jdbc:h2:mem:sbia_ch02
    User Name:		SA
    Password:

# To create and view some test data in HDFS (as if it came out of a MR job)

    $ hadoop fs -copyFromLocal src/main/resources/input/products.txt /data/analysis/results/part-0000.txt
    $ hadoop fs -ls /data/analysis/results

# Run the export job

    $ sh ./target/appassembler/bin/export

Now, go back to view the products in the PRODUCT table using the web UI, should have some rows.



