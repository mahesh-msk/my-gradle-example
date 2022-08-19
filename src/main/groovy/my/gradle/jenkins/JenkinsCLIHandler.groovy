package my.gradle.jenkins

import com.bosch.jenkins.ccas.util.*

class JenkinsCLIHandler
{
	File GroovyFile
	def JarPath
	def rsaPath
	def JenkinsAdd
	def CLIDest
	def key
	def JenkinsCLIHandler(def CLIFolder, def JenkinsAdd)
	{	
		this.CLIDest=CLIFolder
		this.JarPath="${CLIFolder}/JenkinsCLI/jenkins-cli.jar"
		this.rsaPath="${CLIFolder}/temp/cio1cobrsa"
		this.JenkinsAdd=JenkinsAdd
		def ant = new AntBuilder()
		if(!new File(JarPath).exists())
		{
			if(!new File("${CLIFolder}/JenkinsCLI/").exists())
			{
				new File("${CLIFolder}/JenkinsCLI/").mkdirs();
			}

			ant.get(src: "${JenkinsAdd}/jnlpJars/jenkins-cli.jar", dest: "${CLIFolder}/JenkinsCLI/jenkins-cli.jar")
		}
		String SSHKey='''-----BEGIN RSA PRIVATE KEY-----
MIIEogIBAAKCAQEAg16YsMwj4OdNOmkE3SJmrJGA5cVqmY774uKUxlKHF4WPy1v5
AZUkOoLa5f+GQyQoNDEwgItxay6PA0NNSUIqHILXtzWsTauT5cxQEUUivV29BGNm
lA4KiwKpKNn/G4/Z09RY+3kpmDoncIdN6A8w8sKs6TyQevG6HQQHieJQnanZikH1
B4qgpUSwFiwFllxrN124DEnfcuoKqTYHQ7ynglac7arKUJD1BG7eFNBGAXSKVKbb
VpiZGosvflzktnMk3gBTYzLep0loOp5ql6ePT5E5yEHicCHYmmcqFtyCBaOQk+Kj
cXmdimN2EhSiO3pl8WRZy8JTYMKkkxsQisx2BwIBJQKCAQBDdcQHwsZQ5XqvSq94
eXL+q5U3uGcsQoFY1TekHIqPicZhgkGZB2WhgXdTgzcbjxuQbElWxCV8OoDKUv4s
icKZBOtQPigMNYNMfayTPy2D1jChY3ndU1GFp2ufWy/5ZYuIc/ZXnxVcAi/trUOu
hFdn522MhuJopaTCyrfDWIpDHyH883UqtFYZjUjpE8xVU/rFQTCbXnzJYQiUikBk
tIprNQ68bZ4E5Sqa7MKqTKX2kiQ+uAuilppch6f+naplB2UPysZnwrvA2lNPzrv4
3CZX+Crhz3gVRkQy/tUGbUlenFzNTYHX07vHXPD/S+vptleiMBlzDpqtegIW03EA
VPdlAoGBALzcrJ7JFj4UhQLL1oof/XWPu+9WnL0PM5bXKzp+Uff+0l4c3447Na9v
5CPIz4u/J8LNnoT/xCNFP1XkCs86u1yLnVNVbY2bH534ItxSaMtBdgt2LutbI1fg
+bn6mrpR84t3rYDwHl5T+/2g+FGZBsmM+ZE6Ih50+J3IUFyDRD81AoGBALIR1i61
Zgu9TjpBE/sGGbx4WLrlbmVHV87mrLspukOlRVgDifrjZxsIh8Xy5Lpvm/o1URdC
obUGKLrgYmtYqWHVUWK8+UG9B13qb7RuGMo6YFQTmeTQnhbc24BuDqSdITVxdcdW
UyJhVCneRRlviclQ/6R7rDAgtAK5SeVyrNvLAoGAR3YXzWDX+9BprgfwT/Btvceu
5PBW+22JJFFqTXvnqfG3YeFpWGluQmGwROQy2u5iElujw58L8at/v6Jk9HcWdgtQ
SQuRPH/icx+efODpYaoQ/Wr8/xuQ1SSxhKQDMb4kzPzZ3cKjtP0u6liVT06on0oZ
Pd6lIEfvXkvLYUZs2aUCgYEAntGjWhdo2gm0eSVJK/6FqBhPIztUn4TRxl8IwprW
j1wNaiyrfuZ+jbuAA5NyDhCEMi+Ng3LHk521n8E1L08TnHIl/h4qaxBghDH07Qg/
n5vZX750f/hVpbA5ZLUvp5MPx+G8EqbwM2uQQQSC0X8n3RDkAWdiOMM4xCi3h3Qd
oW8CgYEAtMmYojEudA4uBNl+LApw00Q6ITV1wfQz3OkWc1XOYUOTxO5mbw8eXPkZ
xFzEW2ewFsXrEdPHKXNBsQ+yW6LNhH18qNVun4eK5AU6sfWm7gWXmMHRFiPwsz/n
D5WjpXkqrux9kMa5evylCSAKPkea8/MgtnKAiCJ/xAjfQSwFZao=
-----END RSA PRIVATE KEY----- '''

		this.key= SSHKey
	}

	def RunGroovy(def GroovyData)
	{
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		//def Command="java -jar ./jenkins-cli.jar -ssh -user mas9abt -i ${rsaFile.absolutePath} -s ${jenkinsAdd} groovy ="
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add("groovy");
		args.add("=");
		GroovyFile=new File("${CLIDest}/temp/Groovydata.txt")
		GroovyFile.text= GroovyData
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args, GroovyFile)
		DeleteRSAFile()
		return Result
	}
	def CheckCasc(def ConfigData)
	{
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		//def Command="java -jar ./jenkins-cli.jar -ssh -user mas9abt -i ${rsaFile.absolutePath} -s ${jenkinsAdd} groovy ="
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add("check-configuration");
		def ConfigDataFile=new File("${CLIDest}/temp/ConfigData.txt")
		ConfigDataFile.text= ConfigData
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args, ConfigDataFile)
		DeleteRSAFile()
		return Result
	}
	def ApplyCasc(def ConfigData)
	{
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		//def Command="java -jar ./jenkins-cli.jar -ssh -user mas9abt -i ${rsaFile.absolutePath} -s ${jenkinsAdd} groovy ="
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add("apply-configuration");
		def ConfigDataFile=new File("${CLIDest}/temp/ConfigData.txt")
		ConfigDataFile.text= ConfigData
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args, ConfigDataFile)
		DeleteRSAFile()
		return Result
	}
	def CreateJob(def jobName, def jobXml) {
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add('create-job');
		args.add(jobName);	
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args, jobXml)
		DeleteRSAFile()
		return Result
	}
	def BuildJob(def jobName) {
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		args= new ArrayList<String>();
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add('build');
		args.add(jobName);
		args.add('-v');
		args.add('-w');
		args.add('-s');
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args)
		DeleteRSAFile()
		return Result
	}
	def CheckJobExists(def jobName) {
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		args= new ArrayList<String>();
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add('get-job');
		args.add(jobName);			
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args)
		DeleteRSAFile()
		if(Result.exitCode==0) {
			return true
		}
		else {
			return false
		}			
	}
	def DeleteJob(def jobName) {
		def rsafile = CreateRSAFile()
		List<String> args= new ArrayList<String>();
		args= new ArrayList<String>();
		args.clear();
		args.add("java");
		args.add("-jar");
		args.add(JarPath.replace("\\","/"));
		args.add("-ssh");
		args.add("-user");
		args.add("cio1cob");
		args.add("-s");
		args.add(JenkinsAdd);
		args.add("-i");
		args.add(rsafile.absolutePath.replace("\\", "/"));
		args.add('delete-job');
		args.add(jobName);
		ExecuteProcessBuilder JenkinsCLI=new ExecuteProcessBuilder();
		def Result= JenkinsCLI.RunProcess(args)
		DeleteRSAFile()
		return Result
	}
	def CreateRSAFile() {
		File Tempdir=new File("${CLIDest}/temp")
		File rsafile=new File(rsaPath)
		if(!Tempdir.exists())
		{
			Tempdir.mkdirs();
		}
		if(!rsafile.exists())
		{
			rsafile<<key
		}
		return rsafile
	}
	def DeleteRSAFile() {
		File rsafile=new File(rsaPath)
		if(rsafile.exists()){
			rsafile.delete()
		}
	}
}

