common-java
===========

This project contains some common Java classes that are used by other
jeb.ca projects.

This project can also be used independently, as it has some useful utility classes.

To use this in your project, add the following to your Maven `pom.xml` file:

1. You will need to add our Maven repository:

```xml
<repositories>
  <repository>
       <id>jeb-common-mvn-repo</id>
       <url>https://raw.github.com/ebourgeois/jeb-common/mvn-repo/</url>
       <snapshots>
           <enabled>true</enabled>
           <updatePolicy>always</updatePolicy>
       </snapshots>
   </repository>
</repositories>
```

2. Now add our artifact as a dependency:

```xml
<dependency>
  <groupId>ca.jeb</groupId>
  <artifactId>jeb-common</artifactId>
  <version>2014.09.24</version>
</dependency>
```

You can view all the available versions here: https://github.com/ebourgeois/jeb-common/tree/mvn-repo/ca/jeb/jeb-common
