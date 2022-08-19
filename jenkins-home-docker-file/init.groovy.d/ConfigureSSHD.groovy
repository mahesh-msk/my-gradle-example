
	import jenkins.model.*

def instance = Jenkins.getInstance()
def sshd = instance.getDescriptor("org.jenkinsci.main.modules.sshd.SSHD")
println "SSH port: 	 ${sshd?.port}"
if (sshd?.port==-1) {
    sshd.port=32814
    instance.save()
    println "port changed to: 	 ${sshd?.port}"
    
}
