package my.gradle.pluginconfig

import com.bosch.jenkins.ccas.util.*
import com.bosch.jenkins.JenkinsCLIHandler 
import com.bosch.jenkins.JobDslHandler 
import java.awt.TextArea 
import org.junit.Rule 
import org.junit.rules.TemporaryFolder 
import javaposse.jobdsl.dsl.JobParent 
import spock.lang.Shared 
import spock.lang.Specification 
import javaposse.jobdsl.dsl.MemoryJobManagement
import com.bosch.jenkins.ccas.util.*
import java.time.*
import groovy.json.JsonSlurper 
import groovy.json.JsonOutput 
import org.yaml.snakeyaml.Yaml

class CustomTools extends spock.lang.Specification {
	@Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
	def projectRootDir=System.properties['projectRootDir']
	def jenkinsURL=System.properties['jenkinsURL']
	def JenkinsCLI

	def setup() {
		JenkinsCLI=new JenkinsCLIHandler(projectRootDir,jenkinsURL)
	}

	void"verify newtool configurations"()

	{
		given:
		def errorfortool = [:];
		def finalmap = [:];
		String YML = "./CASCTemplate/CASC.yaml";
	        
		when:
		def GroovyData="""
	import jenkins.*;
	import jenkins.model.*;
	import hudson.*;
	import hudson.model.*;
	import com.cloudbees.jenkins.plugins.customtools.*;
	import hudson.tools.ZipExtractionInstaller;
	import hudson.tools.InstallSourceProperty;
	import org.json.*;import groovy.json.JsonOutput

	def instance = Jenkins.getInstance()
	def descriptor = instance.getDescriptor("com.cloudbees.jenkins.plugins.customtools.CustomTool");
	
	// Installation contains list of customtool fetch from descriptor
	
	List<CustomTool> installation = descriptor.getInstallations();
	int toolsize = installation.size();
	
	def jenkinsvalue=[:];
	def jenkinsmap=[:];
	def jenkinserror=[:];
	
    print installation;
	String customtoolname;
	// Fetch all the customtool data
	for(int i =0;i<toolsize;i++)
	{ 
		def property = [:];
		def error ;
		installation.get(i).each{
		def propertiesname = it;
		String Home;
		try{
		  try{
		  // Fetch the name of the tool property and store into customtoolname
	      customtoolname = propertiesname.getName();
	      property.put("name",customtoolname)
	      }catch(Exception e){
	       error = e.getMessage();
	       property.put("name",error);
	       customtoolname == "";
	       }
	       // check the condition if the customtoolname contains value if not store the name of tool in error map
	      if(!customtoolname.isEmpty()){ 
	      try{
	      
		   Home = propertiesname.getHome();
		   property.put("home",Home);
		  }catch(Exception e){
		   error = e.getMessage();
		   property.put("home",error);
		  }
		  
		
		 //Get the properties of the customToolInstallation, which returns an object of the type hudson.tools.InstallSourceProperty  
		    
		                        propertiesname.getProperties().each{
                                obj_ZipExtractionInstaller = it.installers;
                                
                               // Get the AttemptsPerInstaller method from obj_ZipExtractionInstaller
                                 try{
                                   obj_ZipExtractionInstaller.each{
                                   int attempt  = it.getAttemptsPerInstaller();
                                   property.put("attemptsPerInstaller",attempt);}
                                   AuthenticatedZipExtractionInstaller = obj_ZipExtractionInstaller.installers;
                                   AuthenticatedZipExtractionInstaller.each{
                                   obj_ZipExtractionInstaller = it.installers;
                                    }
                                   
                                 }catch(Exception e){
                                  error = e.getMessage();
                                  property.put("attemptsPerInstaller",error);
                                  }
                                  
                                // Find the size of obj_ZipExtractionInstaller for all customtool, size may varies for some customtool name
                                  def sizeforzipExtraction =  obj_ZipExtractionInstaller.size()
                                  
                                // For size variation using loop to find the authenticated value for tool
                                  
                                  obj_ZipExtractionInstaller.each{
                                 def authenticatedvalue = it; 
                                 for(int j=1 ; j <= sizeforzipExtraction;j++){
                                  try{
                                  property.put("credentialsId " +j,authenticatedvalue.getCredentialsId());
                                  }catch(Exception e){
                                  error = e.getMessage();
                                  property.put("credentialsId " +j,error);}
                                  try{
                                  if(authenticatedvalue.getLabel() != null){
                                  property.put("label " +j,authenticatedvalue.getLabel())};
                                  }catch(Exception e){
                                  error = e.getMessage();
                                  property.put("label " +j,error);}
                                  try{
                                  if(authenticatedvalue.getSubdir() != null){
                                  property.put("subdir " +j,authenticatedvalue.getSubdir())};
                                  }catch(Exception e){
                                  error = e.getMessage();
                                  property.put("subdir " +j,error);}
                                  try{
                                  property.put("url " +j,authenticatedvalue.getUrl());
                                  }catch(Exception e){
                                  error = e.getMessage();
                                  property.put("url " +j ,error);}
                                  }
                                  }
                                }  
                               }
                           }catch(Exception e){
                           error = e.getMessage();
                            if(customtoolname == null){
                            jenkinserror.put("Tool name not found",error);}
                            else{
                            jenkinserror.put(customtoolname,error);}
                          }
                        }
                        
                     // property map contains all the property values for tool
                     
                     // Customtoolname contains error make key as tool name not found  and property stored in value for jenkinsvalue map
                    
                     if(customtoolname.isEmpty()){
                            jenkinsvalue.put("Tool name not found",property);}
                            
                     // Jenkinsvalue map store key as customtoolname and value as property for all customtool       
                            else{
                     jenkinsvalue.put(customtoolname,property);}
                     }
	
	// In jenkinsmap add both jenkinsvalue with key as "content" and jenkinserror with key as "error"
	
	jenkinsmap.put("Content",jenkinsvalue);jenkinsmap.put("Error",jenkinserror);
	
	
	// Convert jenkinsmap into JSON
	
	def jsonOutput = JsonOutput.toJson(jenkinsmap);
	println"\\nJSON OUTPUT START\\n"+jsonOutput+"\\nJSON OUTPUT END\\n"

	"""
	ProcessResult Result = JenkinsCLI.RunGroovy(GroovyData)
	String jsonjenkinsmap;
	def processLog = Result.getProcesslog();
	def buildstatus = Result.getExitCode();
	
	
	// Buildstatus for jenkins not equal then it throws some exception 
	if(buildstatus==0)
	{        
	                // processLog contains all data from jenkins to find the jsonoutput using split 
	                
	                def fetchjsonOutputvalue;
		            fetchjsonOutputvalue = processLog.split("JSON OUTPUT START\n")
		           
	                fetchjsonOutputvalue=fetchjsonOutputvalue[1].split("\nJSON OUTPUT END")
	                
	                
	                // After using split jsonjenkinsmap contains jsonoutput value
	                
		            jsonjenkinsmap = fetchjsonOutputvalue[0];
		           
		            
		            
		            // Reading CASC.yaml file in CASCTemplate folder
		
		            //Convert YML into inputstream
		            InputStream inputStream = new FileInputStream(new File(YML));
                    Yaml yaml = new Yaml();
                    
                    // Loading the inputstream into yaml
                    def readingyamlfile =  yaml.load(inputStream);
                    // Contentofyaml contains list of customtool
                    def contentofyaml = readingyamlfile.tool.customTool.installations;
                   
                    int toolsize = contentofyaml.size();
                    
                    def yamlmap = [:];
                    
                    // Fetch all the customtool data
                    
                    for(int i =0 ; i<toolsize ; i++){
         
                    def yamlentry = [:];
                    def customtoolname;
                    readingyamlfile.tool.customTool.installations.get(i).each{
                    def propertyforyaml = it;
                    
                    // Check the key as properties in propertyforyaml if not then add name and home for tool in yamlentry map
                    
                    if(!(propertyforyaml.key).contains("properties")){
                    if(propertyforyaml.key.contains("name")){
                    customtoolname = propertyforyaml.value;}
                    yamlentry.put(propertyforyaml.key,propertyforyaml.value);
                  }
                   // If the key as properties then get the value from propertyforyaml
                  
                  else{
                    def properties = propertyforyaml.value;
                    properties.each{
                    def propertiesdata = it;
                  
                   // Get the installSource from propertiesdata
                    def installSource = propertiesdata.installSource;
                   // Get installsourceinstaller from installSource ,From installsourceinstaller find the size for tool
                    def installsourceinstaller =  installSource.installers;
                    def installerdatasize = installsourceinstaller.size();
                  
                    installsourceinstaller.each{
                    def installerdata = it;
                    
                    // In installerdata check the key as "anyOf" if it find key then takes that value from that value find the key
                    // as "attemptsPerInstaller" if it find key then add the key and value in yamlentry map.
                    installerdata.each{
                    if((it.key).contains("anyOf")){
                    def attemptperinstaller = it.value;
                    attemptperinstaller.each{
                    if((it.key).contains("attemptsPerInstaller")){
                    def attemptproperty = it;
                    yamlentry.put(attemptproperty.key,attemptproperty.value)
                    }
                    
                    // If not find the key as "attemptsPerInstaller" then it takes the values and find the authenticatevalue for tool
                    // All the authenticatedvalue add into yamlentry map
                    
                    else{
                      def installer = it.value;
                    
                      installer.each{
                      def data = it.value;
                      data.each{
                      def authenticate = it
                      authenticate.each{
                      def authenticatedata = it.value;
                      authenticatedata.each{ 
                      def dataforauthenticate = it;
                      if(dataforauthenticate.key == "credentialsId"){ 
                      yamlentry.put("credentialsId 1",dataforauthenticate.value)}
                      if(dataforauthenticate.key ==  "label"){
                      yamlentry.put("label 1",dataforauthenticate.value)}
                      if(dataforauthenticate.key == "subdir"){
                      yamlentry.put("subdir 1",dataforauthenticate.value)}                      
                      if(dataforauthenticate.key == "url"){
                      yamlentry.put("url 1",dataforauthenticate.value)}
                      
                     
                      }
                     
                      }}}}}}
                      
                      // In installerdata not find key as "anyOf" then it find the  authenticatedata
                   
                      // Fetching the autheticatedzip value from yaml file for particular tool and add in yamlentry
                     
                      
                      else{
                      
                      
                      def authenticatedata = it.value;
                      
                     // size variation occur for some tool using loop to find the authenticated value for tool
                    
                      for(int j =1 ; j <= installerdatasize; j++){
                   
                      authenticatedata.each{
                      def authenticateddata =it;
                      if(authenticateddata.key == "credentialsId"){ 
                      yamlentry.put("credentialsId "+j,authenticateddata.value)}
                      if(authenticateddata.key ==  "label"){
                      yamlentry.put("label "+j,authenticateddata.value)}
                      if(authenticateddata.key == "subdir"){
                      yamlentry.put("subdir "+j,authenticateddata.value)}                      
                      if(authenticateddata.key == "url"){
                      yamlentry.put("url "+j,authenticateddata.value)}}
                      }
                      }}}}
                    }}
                    
                    // In yamlmap add the key as customtoolname and value as yamlentry 
                    
                    yamlmap.put(customtoolname,yamlentry); 
                   }
         
          
          // convert yamlmap into JSON 
          
         def jsonyamlmap = JsonOutput.toJson(yamlmap);
        println "\nJSON OUTPUT START\n"+jsonyamlmap+"\nJSON OUTPUT END\n"
		            
		 
		 def jenkinskey = [];
		 def yamlkey = [];          
		 def slurper = new JsonSlurper()
		 
		 // JsonSlurper  parses JSON text content into Groovy data structures (objects) such as maps, lists 
		 
         def jenkinsresult = slurper.parseText(jsonjenkinsmap);
         
         def yamlresult = slurper.parseText(jsonyamlmap);
         
		 
		 // Check the error value in jenkinsresult
		            
		 jenkinsresult.each{
		 def jenkinsresultvalue = it;
		 
		 // "Error" key matches with jenkinsresult fetch the value for error key and value added into errorfortool map
		 
		 if(jenkinsresultvalue.key == "Error"){
		   it.value.each{
		   errorfortool.put(it.key,it.value);}
		  }} 
		  
		  
		  
		  
		  // Check both jenkinsresult and yamlresult key to check the difference
		  
		  
		  // Collect all the key from  jenkinsresult  and store the key in jenkinskey
		  
		  jenkinsresult.each{
		  def checkjenkinsresultkey = it;
		   if(checkjenkinsresultkey.key == "Content"){
		   jenkinskey = checkjenkinsresultkey.value.findAll{it}.collect{it.key};
		   
	       }}
	     
	     // Collect all the key from yamlresult and store the key in yamlkey
	     
		   yamlkey = yamlresult.findAll{it}.collect{it.key};
		   
		
		// Check the difference in both yamlkey and jenkinskey
		
		   def diffinkey = yamlkey - jenkinskey ; 
		   
		   
	    // While substracting the key any difference occur in the key,
	    // For that key find the value and add both key and value in errorfortool map
		   
		   if(diffinkey != []){
		      diffinkey.each{
		      def keydiff = it;
		      def valueforkeydiff =  yamlresult.find{it.key == keydiff}.collect{it.value}
		      errorfortool.put(keydiff,valueforkeydiff);
		      }}
		      
		  
		      
		      
		      
		      // Comparing both jenkinsresult and yamlresult values to check the difference
		    
		      def valuediff;
		      
		      jenkinsresult.each{
		       if(it.key == "Content"){
		        it.value.each{
		        def jenkinsresultkeyvalues = it;
		        def jenkinsmapkey = jenkinsresultkeyvalues.key;
		        
		        // Check both key equals if equals collect the yamlresult value
		        
		        def yamlmapvalue = yamlmap.find{it.key == jenkinsmapkey}.collect{it.value}
		        def jenkinmapvalue = jenkinsresultkeyvalues.value
		        
		        // Check both jenkinsresult and yamlresult values difference
		        
		        valuediff = jenkinmapvalue - yamlmapvalue;
		     
		       
		        // Check the  Values difference if empty it then the key and vaue added into finalmap 
		        // If not empty then the key and value added into errorfortool map 
		       
		        if(valuediff == [:]){
		        finalmap.put(jenkinsmapkey,yamlmapvalue);}
		        else{
		        errorfortool.put(jenkinsmapkey,valuediff);}
		        }}}
		       
		}
		then:
		 
		 // Check exitcode == 0 if not then it fail the execution
		 
		  assert Result.getExitCode() == 0
		  
		 // Check the errorfortool.size == 0 if not then it fail the execution
		  
		  assert errorfortool.size() == 0
		 
		  println "Successfully fetching the customtool data"
		 
       }
      }
