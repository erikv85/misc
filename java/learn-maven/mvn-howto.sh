# ? I have an idea for a Java project and would like to use Maven to
# ? build and run it.
# ?

# Install Maven, browse web for instructions.

# To create a project you need a "groupId" and an "artifactId". An
# example of groupId would be "com.github.me". The artifactId is the
# name of your project. When you've figured those out, do:

GRP_ID=com.github.me
PROJ_NAME=some-project
mvn archetype:generate -DgroupId="$GRP_ID" -DartifactId="$PROJ_NAME" -DinteractiveMode=false

# Directory contents should now look something like this:
# some-project
# |- pom.xml
# |- src
#    |- main
#    |  |- java
#    |     |- com
#    |        |- github
#    |           |- me
#    |              |- App.java
#    |- test
#       |- ...

# ? Cool, let's build it!
# ?

# It won't build, we need to set Java source and target versions first.
# In some-project, anywhere inside "<project>...</project>", say, before
# dependencies, add properties specifying source and target.
SRC=maven.compiler.source
TGT=maven.compiler.target
sed -i "s/^\(\s*\)\(<dependencies>\)/\\n\
\1<properties>\\n\
\1\1<$SRC>11<\/$SRC>\\n\
\1\1<$TGT>11<\/$TGT>\\n\
\1<\/properties>\\n\
\1\2/g" $PROJ_NAME"/pom.xml"

# Now we can finally build the project:
mvn -f some-project/pom.xml install
# or
#mvn -f some-project/pom.xml compile

# ? Great, so how do you run it?
# ?

# If you "install"-ed, then you can do:
java -cp "$PROJ_NAME"/target/"$PROJ_NAME"-1.0-SNAPSHOT.jar "$GRP_ID".App

# If you just want to run the main class, there's more to do:
sed -i "s/^\(\s*\)\(<\/dependencies>\)/\1\2\\n\
\1<build>\\n\
\1\1<plugins>\\n\
\1\1\1<plugin>\\n\
\1\1\1\1<groupId>org.codehaus.mojo<\/groupId>\\n\
\1\1\1\1<artifactId>exec-maven-plugin<\/artifactId>\\n\
\1\1\1\1<configuration>\\n\
\1\1\1\1\1<mainClass>"$GRP_ID".App<\/mainClass>\\n\
\1\1\1\1<\/configuration>\\n\
\1\1\1<\/plugin>\\n\
\1\1<\/plugins>\\n\
\1<\/build>\\n\
/g" $PROJ_NAME/pom.xml

mvn -f some-project/pom.xml exec:java

# ? But how do I pass arguments?
# ?

# Easy:
find -name "App.java" | xargs sed -i "s/^\(\s*\)\(System.out.*\)/\1\2\\n\
\1java.util.Arrays.stream(args).forEach(System.out::println);\
/g"

mvn -f some-project/pom.xml compile exec:java -Dexec.args="arg1 arg2"
