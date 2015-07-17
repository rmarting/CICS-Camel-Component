# Apache Camel CTG/CICS
=======================

This is a small probe of concept about Camel CICS component to connect with mainframe through CICS Gateway Transaction client.
Right now, the response is a byte[] variable, it should be transformed to other structure.

## Features
 * ECI (External call interface) calling a CICS program in a CICS server.
 * Endpoint 
     syntax = "cics:host[:port]/server?options[]"
     example with all options->  
     	cics:serverName1/program1?sslKeyring=sslKeyring1&sslPassword=sslPassword1&userId=userId1&password=password1
 * Exchange
 	Headers:
 		programName: Program to execute in CICS
 		transactionId: Transaction ID to execute the program in CICS
 	Body
 		In: String with the data in the expected pattern by the final program
 		Out: String with the data returned after the program was executed in CICS

## Requirements
 * CICS Transaction Gateway version >= 9.1 must be installed.
 * Fuse >= 6.1 access to the Fuse Maven Repostory

## Instructions 

### 1.- Download CICS Transaction Gateway client
    Version CICS Transaction Gateway client 9.1 or higher

### 2.- Obtain driver OSGI bundle 
    Extract the CICS® TG Client API bundle (com.ibm.ctg.client-1.0.0.jar) from the CICS TG SDK 
    package cicstgsdk/api/java/runtime to a directory on the local file system IBM drivers

### 3.- Install driver in Maven Repository

	In a Local Maven Repository:
       	mvn install:install-file -Dfile=com.ibm.ctg.client-1.0.0.jar -DgroupId=com.ibm.ctg -DartifactId=client -Dversion=1.0.0 -Dpackaging=jar
       
    In a Remote Maven Repository:
    	mvn deploy:deploy-file -DrepositoryId=<repo-id> -Durl=<maven-repo-url> -Dfile=com.ibm.ctg.client-1.0.0.jar -DgroupId=com.ibm.ctg -DartifactId=client -Dversion=1.0.0 -Dpackaging=jar
    	
    	Sample for a Nexus Repository:
    	mvn deploy:deploy-file -DrepositoryId=nexus-server -Durl=http://nexus:8081/nexus/content/repositories/releases -Dfile=com.ibm.ctg.client-1.0.0.jar -DgroupId=com.ibm.ctg -DartifactId=client -Dversion=1.0.0 -Dpackaging=jar	   

### 4.- Build this project use
       mvn install
	
### 5.- Install in a Fuse standalone	
	
		# Install IBM CTG/CICS OSGi Component 
		osgi:install -s mvn:com.ibm.ctg/client/1.0.0
		
		# Install Camel CICS Component
		osgi:install -s mvn:org.apache.camel/camel-cics/1.0.0-SNAPSHOT
	
		# Install 
		osgi:install -s mvn:com.redhat.samples.fuse.cics/camel-cics-blueprint/1.0.2-SNAPSHOT
	
### 6.- Install in a Fuse Fabric (TODO)	
	
		# Install IBM CTG/CICS OSGi Component 
		osgi:install -s mvn:com.ibm.ctg/client/1.0.0
		
		# Install Camel CICS Component Profile
		mvn package fabric8:deploy
		
		osgi:install -s mvn:org.apache.camel/camel-cics/1.0.0-SNAPSHOT	
	
		<!-- Camel CICS Component -->
		<dependency>
			<groupId>org.apache.camel</groupId>
			<artifactId>camel-cics</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
	
		# Install CICS Camel Route Sample 
		mvn package fabric8:deploy
				