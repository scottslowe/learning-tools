FROM openjdk:jre

ADD ${project.build.directory}/${project.build.finalName}-swarm.jar /webapp.jar
CMD java -jar /webapp.jar

EXPOSE ${webapp.port}
