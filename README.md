# Mendix_GitHub_Proj

**Project Description:**
          This application will read the commit history such as owner,commitId,Commit message,Author Mail Id and commit date from the open source projects and store it in a persistance storage. This application will run on a daily basis.
          
**Application tech stack:**


- This application is written in Java with spark for processing. 
- Initial stage the project started with Scala with spark, After analysing the complete requirement the base Github API is developed with Java API and Spring Rest Template. So in order to avoid the syntactical error and time constrain started with the reference of JAVA with Spark 
- Used PostgreSQL for loading the dataframe to DB as a persistant storage.
- Schedule the job through docker Airflow. 
- Planning to create a Scheduler with AWS EMR, Glue and S3 on a daily basis.

**Challenges faced:**
1. Initially developed with Scala due to syntatical errors and performance changed to Java.
2. Tried to store the Dataframe with hive, later faced issues on installing the Hive. Hive works with java 8 and there are more dependencies as to be installed. The Java spark code has been developed with JDK 15 when we downgrade the java to 8, there are lot of errors thrown and application failed to run. There is no backward compatibility in hive
3. Creating the dependencies for the Hadoop and Java is difficult, need to maintain all the versions with same package.
4. Creating a docker airflow pipeline is difficult in installing the image.


