# Building the examples

    $ cd hadoop/hive
    $ mvn clean package appassembler:assemble

# Run the password analysis example

    $ sh ./target/appassembler/bin/hiveApp

# Run the apache log analysis example

    $ sh ./target/appassembler/bin/hiveAppWithApacheLogs

