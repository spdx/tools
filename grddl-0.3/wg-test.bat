@echo off

set PROXY=
rem set PROXY=-DuseProxy=true -Dhttp.proxyHost=proxy.example.com -Dhttp.proxyPort=8080 -Dhttp.useProxy=true
set J=../Jena-2.5.4
set CP=lib/grddl.jar;lib/saxon8.jar;lib/BrowserLauncher2-10.jar;lib/nekohtml.jar;%J%/lib/antlr-2.7.5.jar;%J%/lib/arq.jar;%J%/lib/arq-extra.jar;%J%/lib/concurrent.jar;%J%/lib/iri.jar;%J%/lib/icu4j_3_4.jar;%J%/lib/jena.jar;%J%/lib/jenatest.jar;%J%/lib/json.jar;%J%/lib/junit.jar;%J%/lib/log4j-1.2.12.jar;%J%/lib/lucene-core-2.0.0.jar;%J%/lib/commons-logging-1.1.jar;%J%/lib/xercesImpl.jar;%J%/lib/xml-apis.jar;%J%/lib/wstx-asl-3.0.0.jar;%J%/lib/stax-api-1.0.jar

java -version

java %PROXY% -classpath %CP%  com.hp.hpl.jena.grddl.test.WDTests
