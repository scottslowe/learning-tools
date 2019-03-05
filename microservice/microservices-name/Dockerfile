FROM openjdk:jre

ADD ${project.build.directory}/${project.build.finalName}-swarm.jar /name.jar
CMD java -jar /name.jar

EXPOSE ${name.port}
