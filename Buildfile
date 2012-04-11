# To use, first install Buildr:
# $ JAVA_HOME=/Library/Java/Home gem install buildr
# then invoke with:
# $ buildr compile (compiles classes)
# $ buildr package (builds jar)

repositories.remote << 'http://repo1.maven.org/maven2'

DC = Layout.new
DC[:source, :main, :java] = 'src/'
DC[:source, :test, :java] = 'Test/'
DC[:source, :test, :resources] = 'Test/resources'

# Builds the rake task to build a jar file
def self.command_jar(main_class)
  define main_class[/[^.]+$/], :layout => DC do
    manifest['Main-Class'] = main_class
    manifest['Class-Path'] = ''

    project.version = '1.0.6'
    
    lib_jars = Dir[_('ApachePOI/poi-*.jar'), _( 'ApachePOI/domj4*.jar'), _( 'Jenna-2.6.3/lib/*.jar'), _( 'commons-lang-2.3/*.jar'), _( 'lib/*.jar')] 
    
    compile.with lib_jars
    
    package(:jar).tap do |p| 
      p.include _('src/**/*.properties')
      p.merge lib_jars
    end
  end
end


desc "SPDX Viewer"
command_jar 'org.spdx.tag.SpdxViewer'

desc  "RDF to Spreadsheet"
command_jar 'org.spdx.spdxspreadsheet.RdfToSpreadsheet'


