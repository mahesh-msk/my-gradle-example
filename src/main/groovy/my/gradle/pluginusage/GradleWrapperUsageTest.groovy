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
import groovy.json.JsonSlurper
import com.bosch.jenkins.ccas.util.*
import java.time.*
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import groovy.json.JsonSlurper

/**
 * <h1>This class is to create and verify a jenkins job for the usage of Gradle_Wrapper5_3_1 custom tool.</h1><br>
 * @author FAF2KOR
 *
 */
class GradleWrapperUsageTest extends spock.lang.Specification {
	
	static final String JSON_BEGIN = "JsonBegin"
	static final String JSON_END = "JsonEnd"
	static final String ERROR_MSG_START = "What went wrong:"
	static final String ERROR_MSG_END = "Try:"

	static final String SUCCESS = "SUCCESS"
	static final String FAILURE = "FAILURE"
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
	 * <H1>Test case creates a job to verify file copy operation and file renaming</H1>
	 */
	void "create a job to verify custom tool for Gradle_Wrapper5_3_1 with positive usecase"() {
		given: '''pipeline text script for a job: Step 1: install custom tool Gradle_Wrapper5_3_1 and set the path to local variable
                                                  Step 2: Create build.gradle file with task for hello world
                                                  Step 3: invoke the task using sh command 
                                                 -Step 4: Verify the output of shell command, we should get hello world on the console'''
		String jobName = 'gradleWrapperUsagePositive'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
node('master') {
    def jobsOutput = [:]
    def message = ""
    def error = ""
    try{
        deleteDir()
         stage ('prepare env ') {
            def GRADLE_HOME = tool name: 'Gradle_Wrapper5_3_1', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
            echo "Path to my_tool ${GRADLE_HOME}"
            
        if (GRADLE_HOME == null ) {
                currentBuild.result = "FAILURE"
        }
        else{
            stage('file create'){
                 writeFile file: 'gradle.properties', text: 
                                """org.gradle.daemon=false
                                 org.gradle.parallel=false
                                 org.gradle.jvmargs=-Xmx256m"""
                fileOperations([fileCreateOperation(fileContent: 
                        $/buildscript {
                            tasks.register(\'hello\') {
                            	doFirst {
                            		println \'Hello World\'
                            	}
                            }
                        }/$, fileName: 'build.gradle')])
            }
            sh "${GRADLE_HOME} --stop"
            message =  sh(script: "${GRADLE_HOME} hello", returnStdout: true)
        }
        
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
		        '''
		def jobXmlFile = jobdslHandle.GetJobXml(jobName, pipelinetext, testProjectDir.getRoot().getAbsolutePath())
	
		when:"Create and run a job and get the json console output"
		def Result=JenkinsCLI.CreateJob(jobName, jobXmlFile)
		Result=JenkinsCLI.BuildJob(jobName)
		def buildStatus =Result.getExitCode()
		def consoleOutput = Result.getProcesslog()
		def jsonOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> BuildResult >>>>>>>>>>>>>>>>>>>"
		println jsonOutput
		def buildResult = new JsonSlurper().parseText(jsonOutput)
	
		then:"Json output should have status with SUCCESS and message contains Hello World"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
		buildResult.LogMessage.contains("Hello World")
	}

	/**
	 * <H1>Test case creates a job to verify file copy operation and file renaming</H1>
	 */
	void "create a job to verify custom tool for Gradle_Wrapper5_3_1 with negative usecase"() {
		given: '''pipeline text script for a job: Step 1: install custom tool Gradle_Wrapper5_3_1 and set the path to local variable
                                                  Step 2: Create build.gradle file with task which throws an exception
                                                  Step 3: invoke the task using sh command 
                                                  Step 4: Verify the output of shell command, we should get error message from gradle exception on the console'''
		String jobName = 'gradleWrapperUsageNegative'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
node('master') {
    def jobsOutput = [:]
    def message = ""
    def error = ""
    try{
        deleteDir()
         stage ('prepare env ') {
            def GRADLE_HOME = tool name: 'Gradle_Wrapper5_3_1', type: 'com.cloudbees.jenkins.plugins.customtools.CustomTool'
            echo "Path to my_tool ${GRADLE_HOME}"
            
        if (GRADLE_HOME == null ) {
                currentBuild.result = "FAILURE"
        }
        else{
            stage('file create'){
               writeFile file: 'gradle.properties', text: 
                                """org.gradle.daemon=false
                                 org.gradle.parallel=false
                                 org.gradle.jvmargs=-Xmx256m"""
                fileOperations([fileCreateOperation(fileContent: 
                        $/buildscript {
                            tasks.register(\'throwError\') {
                            	doLast {
                            		throw new Exception("Error occurred during the execution of task")
                            	}
                            }
                        }/$, fileName: 'build.gradle')])
            }
            sh "${GRADLE_HOME} --stop"
            message =  sh(script: "${GRADLE_HOME} throwError", returnStdout: true)
        }
        
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
		        '''
		def jobXmlFile = jobdslHandle.GetJobXml(jobName, pipelinetext, testProjectDir.getRoot().getAbsolutePath())
	
		when:"Create and run a job and get the json console output"
		def Result=JenkinsCLI.CreateJob(jobName, jobXmlFile)
		Result=JenkinsCLI.BuildJob(jobName)
		def buildStatus =Result.getExitCode()
		def consoleOutput = Result.getProcesslog()
		def jsonOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		def errorMessage = consoleOutput.substring(consoleOutput.indexOf(ERROR_MSG_START) + ERROR_MSG_START.length(), consoleOutput.indexOf(ERROR_MSG_END));
		println ">>>>>>>>>>>>>>>>>>>>> BuildResult >>>>>>>>>>>>>>>>>>>"
		println jsonOutput
		println errorMessage
		def buildResult = new JsonSlurper().parseText(jsonOutput)
	
		then:"Json output should have status with FAILURE and json error message contains 'script returned exit code 1'"
		jobXmlFile.text!=null
		buildResult.Status.equals(FAILURE)
		buildResult.ErrorMessage.contains("script returned exit code 1")
		errorMessage.contains("Error occurred during the execution of task")
	}
	
}
