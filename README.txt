--------------------------------------------------------------------------
X M L - R P C
--------------------------------------------------------------------------

The Apache XmlRpc package is an implementation of the XML-RPC
specification (http://www.xml-rpc.com) with optional Servlet
and SSL extensions.

bin/        Temporary directory for building the project.
lib/        Final location of the jar files
build/      Location of Ant build.xml and build.properties files.
examples/   Some examples and instructions on how to run them.
src/        Location of Java sources.
xdocs/      XmlRpc documention in DocBook format.
docs/       The rendered documentation in HTML format.

--------------------------------------------------------------------------
B U I L D I N G
--------------------------------------------------------------------------

You can build the core XmlRpc package with the classes provided
using JDK 1.2+. If you wish to use the Servlet and/or SSL extensions
than you must set the following properties in either your
${user.home}/build.properties file, or the build.properties
file provided in the XmlRpc build/ directory:

jsse.jar
jnet.jar
jcert.jar
servlet.jar

These properties define full paths to JARs files.

--------------------------------------------------------------------------
R U N N I N G
--------------------------------------------------------------------------

The default SAX parser that is used is the MinML parser which is
included in the download. If you want to use an alternative parser
you have to make sure it is included in your CLASSPATH.

