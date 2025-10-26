# Fork of Apache XML-RPC
[![CircleCI](https://circleci.com/gh/evolvedbinary/apache-xmlrpc/tree/main.svg?style=svg)](https://circleci.com/gh/evolvedbinary/apache-xmlrpc/tree/main)
[![Java 8](https://img.shields.io/badge/java-8+-blue.svg)](https://adoptopenjdk.net/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://opensource.org/licenses/Apache2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.evolvedbinary.thirdparty.org.apache.xmlrpc/xmlrpc/badge.svg)](https://search.maven.org/search?q=g:com.evolvedbinary.thirdparty.org.apache.xmlrpc)

[Apache XML-RPC](https://ws.apache.org/xmlrpc/) is no longer officially maintained by Apache.
This is a simple fork for the purposes of:
1. Applying the latest security patches.
2. Providing support for Jakarta EE 5+, i.e.: `jakarta.javax`

* The Apache XML-RPC source code was imported to Git from the archived SVN Apache XML-RPC repository at: https://svn.apache.org/repos/asf/webservices/archive/xmlrpc/
* The security patches were obtained from the Fedora project's package at: https://download-ib01.fedoraproject.org/pub/fedora/linux/releases/34/Everything/source/tree/Packages/x/xmlrpc-3.1.3-28.fc34.src.rpm
    * Whilst 6 patches are available, the 2nd patch for OSGI metadata has not been applied as `PR: XMLRPC-184` (see commit: f0b8977) exists in the main line of this code base and already adds some conflicting OSGI metadata support. 

*NOTE*: This fork was created for our own purposes, and we offer no guarantee that we will maintain it beyond our own requirements.

| Version | Purpose                                                                                                                                                                                                                                                                                                       |
|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 4.0.0   | Contains only the original source code of [Apache XML-RPC 3.1.3](https://github.com/evolvedbinary/apache-xmlrpc/tree/xmlrpc-3.1.3), plus the security updates.                                                                                                                                                | 
| 4.1.0   | 4.0.0 with backports of [f59578a](https://github.com/evolvedbinary/apache-xmlrpc/commit/f59578a95810fac320b99e097fe453d4569cb0c3) and [c7a7091](https://github.com/evolvedbinary/apache-xmlrpc/commit/c7a709199f18f588c5a9b7477e5e6c51515d25aa).                                                              |
| 5.0.0   | Upgraded from `javax.servlet` package namespace to Jakarta EE's `jakarta.servlet`.                                                                                                                                                                                                                            |
| 6.0.0   | Enable the serialization and deserialization of all DOM Node types within function parameter values. [f59578a](https://github.com/evolvedbinary/apache-xmlrpc/commit/f59578a95810fac320b99e097fe453d4569cb0c3)                                                                                                |
| 6.1.0   | Upgraded from `org.apache.ws.commons.util:ws-commons-util:1.0.2` to `com.evolvedbinary.thirdparty.org.apache.ws.commons.util:ws-commons-util:1.1.0` to fix an issue with namespace prefix mappings. [c7a7091](https://github.com/evolvedbinary/apache-xmlrpc/commit/c7a709199f18f588c5a9b7477e5e6c51515d25aa) |


However, if you want a possibly more secure Apache XML-RPC than the last official version (3.1.3), then this fork's artifacts are available
from Maven Central as:

## XML-RPC Server
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-server</artifactId>
        <version>6.1.0</version>
    </dependency>
```

## XML-RPC Client
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-client</artifactId>
        <version>6.1.0</version>
    </dependency>
```

## XML-RPC Common
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-common</artifactId>
        <version>6.1.0</version>
    </dependency>
```

## Perfoming a Release
To release a new version for Evolved Binary to Maven Central, simply run:
``` bash
mvn -Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Darguments="-Dmaven.site.skip=true -Dmaven.site.deploy.skip=true" release:prepare

...

mvn -Dmaven.site.skip=true -Dmaven.site.deploy.skip=true -Darguments="-Dmaven.site.skip=true -Dmaven.site.deploy.skip=true" release:perform
```

Then visit https://oss.sontatype.org and login, and release the staged artifacts to Maven Central
