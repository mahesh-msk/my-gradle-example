package my.gradle.jenkins

import com.bosch.jenkins.ccas.util.*
import javaposse.jobdsl.dsl.FileJobManagement
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.GeneratedJob
import javaposse.jobdsl.dsl.GeneratedView
import javaposse.jobdsl.dsl.ScriptRequest
import javaposse.jobdsl.dsl.DslScriptLoader

class JobDslHandler
{
	def JobDslHandler()
	{	
		
	}

	def GetJobXml(def jobName, def pipelineScript, def projectDir) {
		String jobdsl = """
			def Job=pipelineJob("${jobName}")
			Job.with
					{
			 definition 
						{
							cps
							{
								 	
								script(\'\'\'${pipelineScript}\'\'\')
								sandbox(true)
							}
						}
						logRotator
						{
							daysToKeep(14)
						}	
			}"""
		
		File JobDSLFile=new File(projectDir+"/JobDSLFile.groovy")
		JobDSLFile.text=jobdsl
		File cwd = new File(projectDir)
		URL cwdURL = cwd.toURI().toURL()
		FileJobManagement jm = new FileJobManagement(cwd)
		ScriptRequest request = new ScriptRequest(JobDSLFile.text, cwdURL, false, JobDSLFile.absolutePath)
		GeneratedItems generatedItems = new DslScriptLoader(jm).runScripts([request])
		for (GeneratedJob job : generatedItems.jobs) {
			println ("Generated item: $job")
		}
		def jobXmlFile=new File(projectDir+"/${jobName}.xml")
		println jobXmlFile.text
		return jobXmlFile		
	}
}

