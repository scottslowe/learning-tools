# Building the examples

    $ cd hadoop/pig
    $ mvn clean package appassembler:assemble

# Run the password analysis example

    $ sh ./target/appassembler/bin/pigApp

# Run the password analysis repository example

    $ sh ./target/appassembler/bin/pigAppWithRespoitory

# Run the apache log analysis example

    $ sh ./target/appassembler/bin/pigAppWithApacheLogs

