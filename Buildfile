# To use, first install Buildr:
# $ JAVA_HOME=/Library/Java/Home gem install buildr
# then invoke with:
# $ buildr compile (compiles classes)
# $ buildr package (builds jar)

repositories.remote << 'http://www.ibiblio.org/maven2/'

dc = Layout.new
dc[:source, :main, :java] = 'src/'



define 'PrettyPrinter', :layout => dc do
  manifest['Main-Class'] = 'org.spdx.rdfparser.PrettyPrinter'
  manifest['Class-Path'] = ''
  project.version = '1.0'

  lib_jars = Dir[_('ApachePOI/poi-*.jar'), _('ApachePOI/domj4*.jar'), _('Jenna-2.6.3/lib/*.jar'), _('commons-lang-2.3/*.jar')] 
  compile.with lib_jars
  package(:jar).merge lib_jars
end
