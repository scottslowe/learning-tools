FROM openjdk:jre

ADD ${project.build.directory}/${project.build.finalName}-swarm.jar /greeting.jar
CMD java -jar /greeting.jar

EXPOSE ${greeting.port}
