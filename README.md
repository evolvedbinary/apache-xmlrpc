# Fork of Apache XML-RPC
[![Java 8](https://img.shields.io/badge/java-8+-blue.svg)](https://adoptopenjdk.net/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://opensource.org/licenses/Apache2.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.evolvedbinary.thirdparty.org.apache.xmlrpc/xmlrpc/badge.svg)](https://search.maven.org/search?q=g:com.evolvedbinary.thirdparty.org.apache.xmlrpc)

[Apache XML-RPC](https://ws.apache.org/xmlrpc/) is no longer officially maintained by Apache.
This is a simple fork for the purposes of applying the latest security patches.

* The Apache XML-RPC source code was imported to Git from the archived SVN Apache XML-RPC repository at: https://svn.apache.org/repos/asf/webservices/archive/xmlrpc/
* The security patches were obtained from the Fedora project's package at: https://download-ib01.fedoraproject.org/pub/fedora/linux/releases/34/Everything/source/tree/Packages/x/xmlrpc-3.1.3-28.fc34.src.rpm
    * Whilst 6 patches are available, the 2nd patch for OSGI metadata has not been applied as `PR: XMLRPC-184` (see commit: f0b8977) exists in the main line of this code base and already adds some conflicting OSGI metadata support. 

*NOTE*: This fork was created for our own purposes, and we offer no guarantee that we will maintain it beyond out own requirements.

However, if you want a possibly more secure Apache XML-RPC than the last official version (3.1.3), then this fork's artifacts are available
from Maven Central as:

## XML-RPC Server
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-server</artifactId>
        <version>4.0.0</version>
    </dependency>
```

## XML-RPC Client
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-client</artifactId>
        <version>4.0.0</version>
    </dependency>
```

## XML-RPC Common
```xml
    <dependency>    
        <groupId>com.evolvedbinary.thirdparty.org.apache.xmlrpc</groupId>
        <artifactId>xmlrpc-common</artifactId>
        <version>4.0.0</version>
    </dependency>
```