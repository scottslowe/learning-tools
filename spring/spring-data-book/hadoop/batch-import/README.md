# Building and running

    $ cd hadoop/batch-import
    $ mvn clean package appassembler:assemble

# Start the database (from a separate command window - use 'Ctrl-c' to stop database)

    $ sh ./target/appassembler/bin/start-database

View the products in the PRODUCT table using the web UI

    Driver Class:	org.h2.Driver
    JDBC URL:		jdbc:h2:mem:sbia_ch02
    User Name:		SA
    Password:

# Create the import directory

    $ hadoop fs -mkdir /import/data/products

# Run the import job

    $ sh ./target/appassembler/bin/import

To view the imported product database

    $ hadoop fs -ls /import/data/products
    $ hadoop fs -cat /import/data/products/product-0.txt


