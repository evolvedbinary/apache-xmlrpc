--------------------------------------------------------------------------
X M L - R P C
--------------------------------------------------------------------------

The Apache XmlRpc package is an implementation of the XmlRpc
specification with optional Servlet and SSL extensions.

bin/        Temporary directory for building the project.
build/      Location of Ant build.xml and build.properties files.
src/        Location of Java sources and Torque templates.
xdocs/      XmlRpc documention in DocBook format.

--------------------------------------------------------------------------
B U I L D I N G
--------------------------------------------------------------------------

You can build the core XmlRpc package with the classes provided
by your JDK. If you wish to use the Servlet and/or SSL extensions
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

The default SAX parser that is used is in the XercesJ package so that
should be present in your classpath when trying to use the XmlRpc
package.
