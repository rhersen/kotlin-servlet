install:	undeploy pull /opt/apache-tomcat-8.0.24/webapps/kotlin.war

pull:
	git pull

undeploy:
	rm -Rf /opt/apache-tomcat-8.0.24/webapps/kotlin*

build/libs/html5-kotlin-servlet-1.0-SNAPSHOT.war:	$(shell find src/main/kotlin -type f)
	gradle test war

/opt/apache-tomcat-8.0.24/webapps/kotlin.war:	build/libs/html5-kotlin-servlet-1.0-SNAPSHOT.war
	mv build/libs/html5-kotlin-servlet-1.0-SNAPSHOT.war /opt/apache-tomcat-8.0.24/webapps/kotlin.war
