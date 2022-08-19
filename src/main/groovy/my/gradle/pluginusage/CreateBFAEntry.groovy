package my.gradle.pluginusage

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import groovy.json.JsonSlurper

/**
 * <h1>This class is to create and verify a jenkins job for the new BFA entries.<h1><br>
 * @author FAF2KOR
 *
 */
class CreateBFAEntry extends spock.lang.Specification {

	static final String JSON_BEGIN = "JsonBegin"
	static final String JSON_END = "JsonEnd"

	static final String SUCCESS = "SUCCESS"
	static final String UNSATBLE = "UNSTABLE"
	@Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
	def projectRootDir=System.properties['projectRootDir']
	def jenkinsURL=System.properties['jenkinsURL']
	def JenkinsCLI
	def jobdslHandle
	File JobDSLFile

	def setup() {
		println "Project Dir: ${projectRootDir}"
		println "Jenkins Url: ${jenkinsURL}"
		JenkinsCLI=new JenkinsCLIHandler(projectRootDir,jenkinsURL)
		jobdslHandle=new JobDslHandler()
		JobDSLFile=testProjectDir.newFile('JobDSLFile.groovy')
	}

	/**
	 * <h1>Test case creates a job to create a new BFA entry and verify that exist<h1>
	 */
	void "create a job to create a new BFA entry and verify that exist"() {
		given:'''pipeline text script for a job: Step 1: Construct 2 new BFA entries object, one with BuildLogIndication and another one is of MultilineBuildLogIndication.
                                                 Step 2: Check BFA entries from knowledge base, if exist remove it.
                                                 Step 3: Iterate over new BFA entries and add it into knowledge base.
                                                 Step 4: Verify that newly added BFA entries exist in knowledge base data.'''
		String jobName = 'createNewBFAEntryAndVerify'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import com.sonyericsson.jenkins.plugins.bfa.*
import com.sonyericsson.jenkins.plugins.bfa.model.*
import com.sonyericsson.jenkins.plugins.bfa.model.indication.*
import groovy.json.*
def jobsOutput = [:]
def message = ""
def error = ""
node('master')
{
    try{
            deleteDir()
            // construct 2 new indicaton entries
            newBFAEntry =  [
                             "causes":  [
                                            [
                                                "id": "",
                                                "name": "Issue with BuildLogIndication",
                                                "description": "Description: Create and verify new BFA entry of BuildLogIndication.",
                                                "comment": "Comment: Create a new BFA entry of BuildLogIndication to verify the given pattern.",
                                                "categories": [
                                                    "test_new_bfa"
                                                ],
                                                "indications": [
                                                    [
                                                        "pattern": ".*Error during verifying BuildLogIndication test usecase.*",
                                                        "className": "class com.sonyericsson.jenkins.plugins.bfa.model.indication.BuildLogIndication."
                                                    ]
                                                ]
                                            ],
                                            [
                                                "id": "",
                                                "name": "Issue with MultilineBuildLogIndication",
                                                "description": "Description: Create and verify new BFA entry for MultilineBuildLogIndication.",
                                                "comment": "Comment: Create a new BFA entry of MultilineBuildLogIndication to verify the given pattern.",
                                                "categories": [
                                                    "test_new_bfa"
                                                ],
                                                "indications": [
                                                    [
                                                        "pattern": "Error occurred:(.*)Error during verifying MultilineBuildLogIndication test usecase",
                                                        "className": "class com.sonyericsson.jenkins.plugins.bfa.model.indication.MultilineBuildLogIndication"
                                                    ]
                                                ]
                                            ]
                                        ]    
                            ]    
                            
            //get the knowledge base instance where we need to add the new entries                
            def pluginInst = PluginImpl.getInstance()
            def bfaKnowledgeBase = pluginInst.getKnowledgeBase()
            
            //verify that newly BFA entry is already exist, if yes then remove
            def localbfaData = []
            //get all available the BFA entries from knowledge base
            localbfaData = getBFAData()
            
             def removeEntries = []
            //iterate over new BFA entries and add the matching BFA entries into the list
            for (int i = 0; i < newBFAEntry.causes.size(); i++) {
                removeEntries.addAll(localbfaData.findAll { it.name.contains(newBFAEntry.causes.name[i]) })
            }
            //remove entries
            if(removeEntries.size()>0){
                for(delEntry in removeEntries){
                      bfaKnowledgeBase.removeCause(delEntry.id)
                    }
            }
            
            //iterate over new BFA entries and add it into knowledge base
            newBFAEntry.causes.each {
                dbentry ->  def indicationsToAdd = []
                            println "adding:" + dbentry.name
                            dbentry.indications.each {
                              def errorFound = false
                              if (it.className.contains("com.sonyericsson.jenkins.plugins.bfa.model.indication.BuildLogIndication")) 
                              {
                                  def buildLogIndication = new BuildLogIndication(it.pattern.toString())
                                  indicationsToAdd.add(buildLogIndication)
                              } 
                              else if (it.className.contains("com.sonyericsson.jenkins.plugins.bfa.model.indication.MultilineBuildLogIndication")) 
                              {
                                  def multilineBuildLogIndication = new MultilineBuildLogIndication(it.pattern.toString())
                                  indicationsToAdd.add(multilineBuildLogIndication)
                              } 
                              
                              if (indicationsToAdd != null && indicationsToAdd.size() > 0) 
                              {
                                  println "No of Indications to add: "+ indicationsToAdd.size()
                                      List<String> categoryList = new ArrayList<String> ();
                                      for (int i = 0; i<dbentry.categories.size(); i++) {
                                          categoryList.add(dbentry.categories[i].toString());
                                      }
                                      def id = dbentry.id==null ? "" : dbentry.id.toString()
                                      println "=============="+ id
                                      def newFailureCause = new FailureCause(id, dbentry.name.toString(), dbentry.description.toString(), dbentry.comment.toString(), null, categoryList, indicationsToAdd, null)
                                      bfaKnowledgeBase.saveCause(newFailureCause)
                                      message = message ==""? """Added :  ${dbentry.name}""" : """${message}
                                                                                                  Added : ${dbentry.name}""" 
                              }
                            }
                            
            }
             pluginInst.save()
             jenkins.model.Jenkins.instance.reload()
            
            //verify that newly added indications is exist or not?
            localbfaData = []
            //get all available the BFA entries from knowledge base
            localbfaData = getBFAData()
            def matchedEntries = []
            //iterate over new BFA entries and add the matching bfa entries into the list
            for (int i = 0; i < newBFAEntry.causes.size(); i++) {
                matchedEntries.addAll(localbfaData.findAll { it.name.contains(newBFAEntry.causes.name[i]) })
            }
            
            //matched BFA entry list size should equals to 2(i.e size of new bfa entries)
            println "No of new BFA entry created: " + matchedEntries.size()
            //verify that new BFA entries is same we get it in the matched list
            if ( matchedEntries.size() == 2) {
                    if (!matchedEntries.name[0].trim().equals("Issue with BuildLogIndication")) {
                         error = """New BFA entry 'Issue with BuildLogIndication' not created"""
                    }
                    if (!matchedEntries.name[1].trim().equals("Issue with MultilineBuildLogIndication")) {
                         error """${error}
                                    New BFA entry 'Issue with MultilineBuildLogIndication' not created"""
                    }
            } 
            else{
                currentBuild.result = "FAILURE"
            }
    }
	catch (Exception e) {
    	error = e.getMessage()
		currentBuild.result = "FAILURE"
    }
    finally{
      script {
        echo "RESULT: BuildResult"
        jobsOutput = [
                        Status: "${currentBuild.currentResult}",
                        LogMessage:   "${message}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
        echo "JsonBegin $json JsonEnd"
      }
    }
}

  @NonCPS
  def getBFAData() 
  {
      def failureCauses = []
      for (cause in PluginImpl.getInstance().getKnowledgeBase().getCauses()) 
      {
          def indications = []
          for (indication in cause.getIndications())
           {
              def indicationToAdd = [:]
              indicationToAdd["pattern"] = indication.toString()
              indicationToAdd["className"] = indication.class.toString()
              indications.add(indicationToAdd)
          }
          failureCauses.add([id: cause.getId(), name: cause.getDisplayName(), description: cause.description, comment: cause.comment, categories: cause.getCategories(), indications: indications])
      }
      if (failureCauses.size() != 0) 
      {
          return failureCauses
      }
}
        '''
		def jobXmlFile = jobdslHandle.GetJobXml(jobName, pipelinetext, testProjectDir.getRoot().getAbsolutePath())

		when:"Create and run a job and get the json console output"
		def Result=JenkinsCLI.CreateJob(jobName, jobXmlFile)
		println ScriptUtil.sciptApprovalData()
		JenkinsCLI.RunGroovy(ScriptUtil.sciptApprovalData())
		Result=JenkinsCLI.BuildJob(jobName)
		def buildStatus =Result.getExitCode()
		def consoleOutput = Result.getProcesslog()
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)

		then:"Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * Test case creates a job to verify file copy operation with file overwrite
	 */
	void "create a job to test new bfa"() {
		given:'''pipeline text script for a job: Step 1: Test new BFA entries for usecase BuildLogIndication and MultilineBuildLogIndication
                                                 Step 2: Check that newly added BFA entries is trigging for matched patterns.
                                                 Step 3: Verify that the correct indications are identified.'''
		String jobName = 'verifyAndTestNewBFA'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import com.sonyericsson.jenkins.plugins.bfa.*
import com.sonyericsson.jenkins.plugins.bfa.model.*
import com.sonyericsson.jenkins.plugins.bfa.model.indication.*
import groovy.json.*
def jobsOutput = [:]
def message = ""
def error = ""
node('master')
{
    try{
         deleteDir()
        //BuildLogIndication
        println "Error during verifying BuildLogIndication test usecase"
        
        //MultilineBuildLogIndication
        println "Error occurred:"
        println "Error during verifying MultilineBuildLogIndication test usecase"
        
        currentBuild.result = "UNSTABLE"
    }
    catch (Exception e) {
    	error = e.getMessage()
		currentBuild.result = "FAILURE"
    }
    finally{
        script{
                def buildFailure = tm('${BUILD_FAILURE_ANALYZER}') //initialize build failure
                // println buildFailure
                
                def build = currentBuild.rawBuild
                println "Build: ${build} | Status: ${currentBuild.currentResult}"
               
                def buildAction = build.getAction(FailureCauseBuildAction.class);
                def failureCauseDisplayData  = buildAction.getFailureCauseDisplayData();
                def failureCauses = failureCauseDisplayData.getFoundFailureCauses();

                message =  """Found ${failureCauses.size()} indications."""
                // println failureCauses
                for(failureCause in failureCauses){
                    message = """${message} 
                                    name: ${failureCause.getName()}, description: ${failureCause.getDescription()}"""
                    for (indication  in failureCause.getIndications()) {
                        // println "matching String" +indication.getMatchingString()
                        // println "first matching String" +indication.getFirstMatchingLine()
                        
                        message = """${message}
                                  Matching Pattern: ${indication.getPattern()}
                                  Matching String: ${indication.getFirstMatchingLine()}"""
                    }
                }
              
                echo "RESULT: BuildResult"
                jobsOutput = [
                                Status: "${currentBuild.currentResult}",
                                LogMessage:   "${message}",
                                ErrorMessage: "${error}"
                             ]
                def json = JsonOutput.toJson(jobsOutput)
                //if you need pretty print (multiline) json
                json = JsonOutput.prettyPrint(json)
                echo "JsonBegin $json JsonEnd"
           }
        }
}
        '''
		def jobXmlFile = jobdslHandle.GetJobXml(jobName, pipelinetext, testProjectDir.getRoot().getAbsolutePath())

		when:"Create and run a job and get the json console output"
		def Result=JenkinsCLI.CreateJob(jobName, jobXmlFile)
		JenkinsCLI.RunGroovy(ScriptUtil.sciptApprovalData())
		Result=JenkinsCLI.BuildJob(jobName)
		def buildStatus =Result.getExitCode()
		def consoleOutput = Result.getProcesslog()
		println consoleOutput
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)

		then:"Console output should contains UNSATBLE string and detailed text of 'Identified problems'"
		jobXmlFile.text!=null
		buildResult.Status.equals(UNSATBLE)
		buildResult.LogMessage.contains("Found 2 indications.")
		buildResult.LogMessage.contains("name: Issue with BuildLogIndication, description: Description: Create and verify new BFA entry of BuildLogIndication.")
		buildResult.LogMessage.contains("Matching Pattern: .*Error during verifying BuildLogIndication test usecase.*")
		buildResult.LogMessage.contains("Matching String: Error during verifying BuildLogIndication test usecase")

		buildResult.LogMessage.contains("name: Issue with MultilineBuildLogIndication, description: Description: Create and verify new BFA entry for MultilineBuildLogIndication.")
		buildResult.LogMessage.contains("Error occurred:(.*)Error during verifying MultilineBuildLogIndication test usecase")
		buildResult.LogMessage.contains("Matching String: Error occurred:")
	}

	/**
	 * Test case creates a job and tests the copying of file from one folder to another folder and
	 *files are copied directly to the target location without preserving source file sub-directory structure. 
	 */
	void "create a job to retrieve the bfa findings(i.e indication in html view) and match"() {
		given:'''pipeline text script for a job: Step 1: Test new BFA entries for usecase BuildLogIndication.
                                                 Step 2: Get the current build url from the console output.
                                                 Step 3: Verify that build page added with all identified problems texts.'''
		String jobName = 'retrieveAndFindIndicationInBuildPage'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import com.sonyericsson.jenkins.plugins.bfa.*
import com.sonyericsson.jenkins.plugins.bfa.model.*
import com.sonyericsson.jenkins.plugins.bfa.model.indication.*
import groovy.json.*
def jobsOutput = [:]
def message = ""
def error = ""
node('master')
{
    try{
         deleteDir()
        //BuildLogIndication
        println "Error during verifying BuildLogIndication test usecase"
        
        currentBuild.result = "UNSTABLE"
    }
    catch (Exception e) {
    	error = e.getMessage()
		currentBuild.result = "FAILURE"
    }
    finally{
        script{
                def data = new URL(BUILD_URL).getText()
                message = BUILD_URL
               
                echo "RESULT: BuildResult"
                jobsOutput = [
                                Status: "${currentBuild.currentResult}",
                                LogMessage:   "${message}",
                                ErrorMessage: "${error}"
                             ]
                def json = JsonOutput.toJson(jobsOutput)
                //if you need pretty print (multiline) json
                json = JsonOutput.prettyPrint(json)
                echo "JsonBegin $json JsonEnd"
           }
                
        }
        //  println buildFailure
}
        '''
		def jobXmlFile = jobdslHandle.GetJobXml(jobName, pipelinetext, testProjectDir.getRoot().getAbsolutePath())

		when:"Create and run a job and get the json console output"
		def Result=JenkinsCLI.CreateJob(jobName, jobXmlFile)
		JenkinsCLI.RunGroovy(ScriptUtil.sciptApprovalData())
		Result=JenkinsCLI.BuildJob(jobName)
		def buildStatus =Result.getExitCode()
		def consoleOutput = Result.getProcesslog()
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)
		def buildUrl = buildResult.LogMessage.trim()
		buildUrl = buildUrl.substring(0, buildUrl.length() -1);
		println "build url" + buildUrl
		def doc = Jsoup.parse(new URL(buildUrl), 3000);
		def h2 = doc.select("h2").first().text()
		def h3 = doc.select("h3").first().text()

		then: "Console output should contains UNSTABLE string and html page contains element of 'Identified problems'"
		jobXmlFile.text!=null
		buildResult.Status.equals(UNSATBLE)
		h2.equals("Identified problems")
		h3.equals("Issue with BuildLogIndication Description: Create and verify new BFA entry of BuildLogIndication. Indication 1")
	}
}