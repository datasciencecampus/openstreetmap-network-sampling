all: test jar
test: 
	mvn test
jar: 
	mvn -Dmaven.test.skip=true clean package 
