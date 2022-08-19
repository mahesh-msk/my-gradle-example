package my.gradke.utils
/**
 * Groovy class that will execute a process
 */
class ExecuteProcessBuilder {
	/**
	 * Function that will run the process based on the arguments passed and returns the exit code/process log</br>
	 * @param cmds Pass the command line arguments as a list that will execute the process.
	 */
	def ProcessResult RunProcess(List<String> cmds){
		def ProcessLog=""	 
		ProcessResult ToolResult=new ProcessResult()
		def OMCLBuildProcess=new ProcessBuilder(cmds).redirectErrorStream(true).start()
		OMCLBuildProcess.inputStream.eachLine{
			println it
			ProcessLog+=it+"\n"
			}
		OMCLBuildProcess.waitFor()
		int ExitCode=OMCLBuildProcess.exitValue()
		ToolResult.processlog=ProcessLog
		ToolResult.exitCode=ExitCode
		return ToolResult
	}
	def ProcessResult RunProcess(List<String> cmds, def Fileinput){
		def ProcessLog=""
		ProcessResult ToolResult=new ProcessResult()
		ProcessBuilder process=new ProcessBuilder()
		process.command(cmds)
		process.redirectInput(Fileinput)
		process.redirectErrorStream(true)
		def OMCLBuildProcess=process.start()
		/*OutputStream stdin = process.getOutputStream(); // write to this
		process.redirectErrorStream(true)
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));	
		writer.write(input);
		writer.flush();
		writer.close();*/

		OMCLBuildProcess.inputStream.eachLine{
			println it
			ProcessLog+=it+"\n"
			}
		OMCLBuildProcess.waitFor()
		int ExitCode=OMCLBuildProcess.exitValue()
		ToolResult.processlog=ProcessLog
		ToolResult.exitCode=ExitCode
		return ToolResult
	}
}