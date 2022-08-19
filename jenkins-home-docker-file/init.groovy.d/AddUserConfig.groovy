
import jenkins.model.*
import hudson.security.*
//public key cio1cob
	def publicKey='AAAAB3NzaC1yc2EAAAABJQAAAQEAg16YsMwj4OdNOmkE3SJmrJGA5cVqmY774uKUxlKHF4WPy1v5AZUkOoLa5f+GQyQoNDEwgItxay6PA0NNSUIqHILXtzWsTauT5cxQEUUivV29BGNmlA4KiwKpKNn/G4/Z09RY+3kpmDoncIdN6A8w8sKs6TyQevG6HQQHieJQnanZikH1B4qgpUSwFiwFllxrN124DEnfcuoKqTYHQ7ynglac7arKUJD1BG7eFNBGAXSKVKbbVpiZGosvflzktnMk3gBTYzLep0loOp5ql6ePT5E5yEHicCHYmmcqFtyCBaOQk+KjcXmdimN2EhSiO3pl8WRZy8JTYMKkkxsQisx2Bw== rsa-key-20180709'
//create user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
		
def user =hudsonRealm.createAccount("cio1cob","admin")

    
   // def email_param = new hudson.tasks.Mailer.UserProperty(email)
  //  user.addProperty(email_param)
	
    //set public key for the user
    if ( publicKey != "" ) {
      def keys_param = new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl(publicKey)
      user.addProperty(keys_param)
    }
println user.getFullName()+" user is created" 
user.save()
def instance = Jenkins.getInstance()
