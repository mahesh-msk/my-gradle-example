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

/**
 * <h1>This class is to create and verify a jenkins job for the File Operation plugin.</h1>
 * <p> Use cases:
 * 1. simple copy => scenario1 rename <br>
 * 2. simple copy => scenario2 overwrite <br>
 * 3. simple with pattern(flat =true => copy all the files under destination without that retains the directory structure of the files it contains) <br>
 * 4. simple with pattern(flat =false=> copy all the files under destination and retains the directory structure of the files it contains) <br>
 * 5. copy a folder<br>
 * 6. finding files</p> 
 * 
 * @author FAF2KOR
 *
 */
class FileOperationsTest extends spock.lang.Specification {

	static final String JSON_BEGIN = "JsonBegin"
	static final String JSON_END = "JsonEnd"

	static final String SUCCESS = "SUCCESS"
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
	void "create a job to verify file copy operation and file renaming"() {
		given: '''pipeline text script for a job: Step 1: Create folder f1 under that create file a.txt
                                                  Step 2: Create folder f2 and copy  a.txt file to folder f2
                                                  Step 3: Rename a.txt in f2 folder to d.txt
                                                 -Step 4: Verify d.txt file should exist in folder f2'''
		String jobName = 'fileOperationsCopyAndFileRename'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
								import groovy.json.*
								def jobsOutput = [:]
								def fileOp = ""
								def error = ""
								node('master') {
								    try{
								        stage('Simple copy with rename') {
								            deleteDir()
								            // create folder f1 and create a.txt file under that
								             fileOperations([folderCreateOperation('f1'), fileCreateOperation(fileContent: 'sample text content inside a.txt file', fileName: 'f1/a.txt')])
								             // create folder f2 and copy a.txt file f1 -> f2 folder
								             fileOperations([folderCreateOperation('f2'), fileCopyOperation(flattenFiles: true, includes: '**/a.txt', renameFiles: false, targetLocation: 'f2')])
								            // rename a.txt file to d.txt
								            fileOperations([fileRenameOperation(destination: 'f2/d.txt', source: 'f2/a.txt')])
								            //  check d.txt file exist in f2
								             def d_fileExist = fileExists 'f2/d.txt'
								             if (d_fileExist) {
								                 fileOp = "File d.txt exist." 
								                 
								             } else {
								                 fileOp = "File d.txt does not exist."
								                 currentBuild.result = "FAILURE"
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
						                                LogMessage:   "${fileOp}",
						                                ErrorMessage: "${error}"
								                     ]
								         def json = JsonOutput.toJson(jobsOutput)
								        //if you need pretty print (multiline) json
								        json = JsonOutput.prettyPrint(json)
								          echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)

		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * <H1>Test case creates a job to verify file copy operation with file overwrite</H1>
	 */
	void "create a job to verify file copy operation with file overwrite"() {
		given:'''pipeline text script for a job: Step 1: create folder f1 under that create 2 files a.txt and ab.txt with some content
                                                 Step 2: Create folder f2 and copy  a.txt file to folder f2
                                                 Step 3: Change the content of a.txt in folder f1
                                                 Step 4: Again copy  a.txt file from folder f1 to folder f2
                                                 Step 5: Verify whether a.txt in folder f2 got overwritten or not?'''
		String jobName = 'fileOperationsCopyAndFileOverwrite'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
def jobsOutput = [:]
def fileOp = ""
def error = ""
node('master') {
    try{
       stage('Simple copy with content change') {
            deleteDir()
            // create folder f1 and create a.txt and ab.txt files under f1
            fileOperations([folderCreateOperation('f1'), fileCreateOperation(fileContent: 'Sample file content in f1/a.txt file.', fileName: 'f1/a.txt'),fileCreateOperation(fileContent: 'Sample file content in f1/ab.txt file.', fileName: 'f1/ab.txt')])
            // create folder f2 and copy a.txt file from f1 -> f2 folder
            fileOperations([folderCreateOperation('f2'), fileCopyOperation(flattenFiles: true, includes: '**/a.txt', targetLocation: 'f2')])
            //get the file content of a.txt in f2
            def initialContent = readFile encoding: 'UTF-8', file: 'f2/a.txt'
            fileOp = "File content before overwrite: ${initialContent}"
            // change the content of a.txt file
            writeFile encoding: 'UTF-8', file: 'f1/a.txt', text: 'Content of a.txt file is changed.'
            //copy a.txt and ab.txt files from f1 -> f2 folder
            fileOperations([fileCopyOperation( flattenFiles: true, includes: 'f1/a*.txt', targetLocation: 'f2')])
            //get the file content of a.txt in f2
            def finalContent = readFile encoding: 'UTF-8', file: 'f2/a.txt'
            fileOp  = """${fileOp} 
			File content after overwrite: ${finalContent}"""
            if (!finalContent.equals('Content of a.txt file is changed.')) {
                echo "content not same"
                currentBuild.result = 'FAILURE'
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
                        LogMessage:   "${fileOp}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
        //  echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
        def buildResult = new JsonSlurper().parseText(consoleOutput)

		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * <H1>Test case creates a job and tests the copying of file from one folder to another folder and
	 *files are copied directly to the target location without preserving source file sub-directory structure</H1>. 
	 */
	void "create a job to verify file copy operation with pattern Flat=True"() {
		given:'''pipeline text script for a job: Step 1: Create directory tree - > f1 -> f2  -> f3
                                                 Step 2: Create files in folders [f2 -> a.txt ,v.txt] and [f3 -> ab.txt,c.txt] 
                                                 Step 3: Create new folder d1 and copy all files with File Pattern [**/a*.txt] and set Flat=True 
                                                 Step 4: Verify that files are copied directly to the target location(i.e d1 folder) without preserving source file sub-directory structure.'''
		String jobName = 'fileOperationsCopyWithPatternFlatTrue'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
def jobsOutput = [:]
def fileOp = ""
def error = ""
node('master') {
    try{
       stage('Copy with pattern: Flat = true') {
           deleteDir()               
            // create directory tree - > f1 -> f2  -> f3
             fileOperations([folderCreateOperation('f1/f2/f3')])
            // create a file into  f2-> a.txt ,v.txt
            fileOperations([fileCreateOperation(fileContent: 'content in f1/a.txt', fileName: 'f1/a.txt'), fileCreateOperation(fileContent: 'content in f1/v.txt', fileName: 'f1/v.txt')])
            //create a file into f3-> ab.txt,c.txt
            fileOperations([fileCreateOperation(fileContent: 'content in f3/ab.txt', fileName: 'f3/ab.txt'), fileCreateOperation(fileContent: 'content in f3/c.txt', fileName: 'f3/c.txt')])
            //create new folder d1 and copy all pattern file here
            fileOperations([folderCreateOperation('d1'), fileCopyOperation( flattenFiles: true, includes: '**/a*.txt', renameFiles: false,  targetLocation: 'd1')])
            //  find all files in d1
            def allFilesInD1  = findFiles(glob: 'd1/a*.txt') 
            fileOp = """No. of files in d1 folder: ${allFilesInD1.length}
            File Info of ${allFilesInD1[0].name}:  ${allFilesInD1[0].path} ${allFilesInD1[0].directory}}
            File Info of ${allFilesInD1[1].name}: ${allFilesInD1[1].path} ${allFilesInD1[1].directory}}"""
            if (allFilesInD1.length != 2) {
                currentBuild.result = 'FAILURE'
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
                        LogMessage:   "${fileOp}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
        //  echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)

		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * <H1>Test case creates a job and tests the copying of file from one folder to target folder and 
	 * the target location will preserve source file sub-directory structure.</H1>
	 */
	void "create a job to verify file copy operation with pattern Flat=false"() {
		given:'''pipeline text script for a job: Step 1: Create directory tree - > f1 -> f2  -> f3
                                                 Step 2: Create files in folders [f2 -> a.txt ,v.txt] and [f3 -> ab.txt,c.txt] 
                                                 Step 3: Create new folder d1 and copy all files with File Pattern [**/a*.txt] and set Flat=False 
                                                 Step 4: verify that files are copied to the target location(i.e d1 folder) with corresponding sub-folders [ ensure that copy will preserve source file sub-directory structure].'''
		String jobName = 'fileOperationsCopyWithPatternFlatFalse'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
def jobsOutput = [:]
def fileOp = ""
def error = ""
node('master') {
    try{
       stage('Copy with pattern: Flat = false') {
           deleteDir()               
            // create directory tree - > f1 -> f2  -> f3
             fileOperations([folderCreateOperation('f1/f2/f3')])
             // create a file into  f2-> a.txt ,v.txt
            fileOperations([fileCreateOperation(fileContent: 'content in f1/a.txt', fileName: 'f1/a.txt'), fileCreateOperation(fileContent: 'content in f1/v.txt', fileName: 'f1/v.txt')])
           //create a file into f3-> ab.txt,c.txt
             fileOperations([fileCreateOperation(fileContent: 'content in f3/ab.txt', fileName: 'f3/ab.txt'), fileCreateOperation(fileContent: 'content in f3/c.txt', fileName: 'f3/c.txt')])
             //create new folder d1 and copy all pattern file here
             fileOperations([folderCreateOperation('d1'), fileCopyOperation( flattenFiles: false, includes: '**/a*.txt', renameFiles: false,  targetLocation: 'd1')])
            //  find all files in d1
            def allFilesInD1F1  = findFiles(glob: 'd1/f1/a*.txt') 
            def allFilesInD1F2  = findFiles(glob: 'd1/f2/a*.txt') 
            def allFilesInD1F3  = findFiles(glob: 'd1/f3/a*.txt') 
            fileOp = """No. of files in d1/f1 folder: ${allFilesInD1F1.length}
            No. of files in d1/f2 folder: ${allFilesInD1F2.length}
            No. of files in d1/f3 folder: ${allFilesInD1F3.length}
            File Info of ${allFilesInD1F1[0].name}:  ${allFilesInD1F1[0].path} ${allFilesInD1F1[0].directory}}
            File Info of ${allFilesInD1F3[0].name}: ${allFilesInD1F3[0].path} ${allFilesInD1F3[0].directory}}"""
            if (allFilesInD1F1.length <= 0 || allFilesInD1F2.length > 0 || allFilesInD1F3.length <= 0) {
                currentBuild.result = 'FAILURE'
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
                        LogMessage:   "${fileOp}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
        //  echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)

		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * <H1>Test case creates a job and tests the copying of folder from source to destination path.</H1>
	 */
	void "create a job to verify file copy operation of folder"() {
		given:'''pipeline text script for a job: Step 1: Create directory tree - > f1 -> f2  -> f3
                                                 Step 2: Create files in folders [f1 -> a.txt ], [f2 -> ab.txt]and [f3-> ac.txt]
                                                 Step 3: Create folder d1 and copy folder f2 to d1 folder  
                                                 Step 4: verify that all the files inside the folder f2 will copy to d2 folder.'''
		String jobName = 'fileOperationsFolderCopy'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
def jobsOutput = [:]
def fileOp = ""
def error = ""
node('master') {
    try{
        stage('Copy folder') {
           deleteDir()                
            // create folder f1 -> f2
             fileOperations([folderCreateOperation('f1/f2/f3'), fileCreateOperation(fileContent: 'some content', fileName: 'f1/f2/a.txt')])
             fileOperations([fileCreateOperation(fileContent: 'some content', fileName: 'f1/ab.txt')])
             fileOperations([fileCreateOperation(fileContent: 'some content', fileName: 'f1/f2/f3/ac.txt')])
             //-copy **/f2 to d2
             fileOperations([folderCreateOperation('d2'),folderCopyOperation(destinationFolderPath: 'd2', sourceFolderPath: 'f1/')])
            //  find all files in d2
            def d2  = findFiles(glob: 'd2/**/' )
            fileOp = """No. of files in d1/f1 folder: ${d2.length}
            fileOp "File info of ${d2[0].name}: ${d2[0].path} ${d2[0].directory}"""
            if (d2.length <= 0) {
                currentBuild.result = 'FAILURE'
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
                        LogMessage:   "${fileOp}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
        //  echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)
		
		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}

	/**
	 * <H1>Test case creates a job and verifies the finding files for the given pattern.</H1>
	 */
	void "create a job to verifies the finding files operation"() {
		given:'''pipeline text script for a job: Step 1: Create directory tree - > f1 -> f2  -> f3
                                                 Step 2: Create files in folders [f1 ->  xyz.txt, axyza.json], [f2 ->  vaa.txt , azzzas.ls]and [f3-> faa.txt]
                                                 Step 3: Find all files which end with 'a' with any extension and find all files which starts with character 'a'
                                                 Step 4: Verify the number of filer ends with 'a' and starts with 'a'.'''
		String jobName = 'fileOperationsFindFiles'
		LocalDateTime TimeNow = LocalDateTime.now();
		String pipelinetext = '''
import groovy.json.*
def jobsOutput = [:]
def fileOp = ""
def error = ""
node('master') {
    try{
        stage('Find files') {
            deleteDir()
            // create some random files
            fileOperations([folderCreateOperation('f1/f2/f3'), fileCreateOperation(fileContent: 'some content', fileName: 'f1/xyz.txt'), fileCreateOperation(fileContent: 'some content', fileName: 'f1/axyza.json')])
            fileOperations([fileCreateOperation(fileContent: 'some content', fileName: 'f2/vaa.txt'), fileCreateOperation(fileContent: 'some content', fileName: 'f2/azzzas.ls')])
            fileOperations([fileCreateOperation(fileContent: 'some content', fileName: 'f3/faa.txt')])
            // find all file names starts with a and ends with a
            def fswa  = findFiles excludes: '', glob: '**/a*'
            fileOp =   """${fileOp}
            No. of files starts with 'a': ${fswa.length}"""
            def fewa  = findFiles excludes: '', glob: '**/*a.*'
            fileOp=  """${fileOp}
            No. of files ends with 'a': ${fewa.length}"""
            
            fileOp = """${fileOp}
            List of files starts with char 'a'"""
             for (int j = 0; j < fswa.length; j++) {
               fileOp = """${fileOp}
               File info of ${fswa[j].name}: ${fswa[j].path} ${fswa[j].directory} ${fswa[j].length} ${fswa[j].lastModified}"""
            }
            
            fileOp = """${fileOp}
            List of files ends with char 'a'"""
            for (int i = 0; i < fewa.length; i++) {
               fileOp = """${fileOp}
               File info of ${fewa[i].name}: ${fewa[i].path} ${fewa[i].directory} ${fewa[i].length} ${fewa[i].lastModified}"""
            }
          
            if (fswa.length != 2 || fewa.length != 3) {
                currentBuild.result = 'FAILURE'
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
                        LogMessage: "${fileOp}",
                        ErrorMessage: "${error}"
                    ]
         def json = JsonOutput.toJson(jobsOutput)
        //if you need pretty print (multiline) json
        json = JsonOutput.prettyPrint(json)
       // echo "Jenkins Result Pretty print:"
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
		consoleOutput = consoleOutput.substring(consoleOutput.indexOf(JSON_BEGIN) + JSON_BEGIN.length(), consoleOutput.indexOf(JSON_END));
		println ">>>>>>>>>>>>>>>>>>>>> Validate BuildResult >>>>>>>>>>>>>>>>>>>"
		println consoleOutput
		def buildResult = new JsonSlurper().parseText(consoleOutput)
		
		then: "Console output should contains SUCCESS string"
		jobXmlFile.text!=null
		buildResult.Status.equals(SUCCESS)
	}
}