package my.gradke.utils
/**
 * Groovy class that will get/set the process result
 */
class ProcessResult {
	String processlog,exceptionLog
	int exitCode	/**
	 * Function that gets the process log</br> 
	 */
	public String getProcesslog() {
		return processlog;
	}
	/**
	 * Function that sets the process log</br>
	 * @param processlog String parameter that will contain the process log
	 */
	public void setProcesslog(String processlog) {
		this.processlog = processlog;
	}
	/**
	 * Function that gets the exception log</br>
	 */
	public String getExceptionLog() {
		return exceptionLog;
	}
	/**
	 * Function that sets the exception log</br>
	 * @param exceptionLog String parameter that will contain the exception log
	 */
	public void setExceptionLog(String exceptionLog) {
		this.exceptionLog = exceptionLog;
	}
	/**
	 * Function that gets the exit code</br>
	 */
	public int getExitCode() {
		return exitCode;
	}
	/**
	 * Function that sets the exit code</br>
	 * @param exitCode integer parameter that will contain the exit code
	 */
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

}