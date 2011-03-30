#!/bin/bash
# run Jena tests; try and guess path separator.

PROXY=
# PROXY="-DuseProxy=true -Dhttp.proxyHost=proxy.example.com -Dhttp.proxyPort=8080 -Dhttp.useProxy=true"
JENA=../Jena-2.5.4


S=":"
if [ "$OSTYPE" == "cygwin" ]; then S=";"; fi
LIBS="$(cat<<EOF
grddl.jar
saxon8.jar
nekohtml.jar
BrowserLauncher2-10.jar
EOF
)"

CP=""
for jar in $LIBS
do
  jar="lib/${jar}"
  [ -e "$jar" ] || echo "No such jar: $jar" 1>&2

  if [ "$CP" == "" ]
  then
      CP="${jar}"
  else
      CP="$CP${S}${jar}"
      fi
  done

LIBS="$(cat<<EOF
antlr-2.7.5.jar
arq.jar
arq-extra.jar
commons-logging-1.1.jar
concurrent.jar
icu4j_3_4.jar
jena.jar
json.jar
iri.jar
jenatest.jar
junit.jar
log4j-1.2.12.jar
lucene-core-2.0.0.jar
xercesImpl.jar
xml-apis.jar
stax-api-1.0.jar
wstx-asl-3.0.0.jar
EOF
)"

for jar in $LIBS
do
  jar="${JENA}/lib/${jar}"
  [ -e "$jar" ] || echo "No such jar: $jar" 1>&2
  CP="$CP${S}${jar}"
  done

##echo $CP

#SOCKS=-DsocksProxyHost="<your socks server>"

java -version

java $PROXY -classpath "$CP" $SOCKS junit.textui.TestRunner ${1:-com.hp.hpl.jena.grddl.test.TestPackage}
