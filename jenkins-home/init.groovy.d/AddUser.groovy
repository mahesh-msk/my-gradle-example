
import jenkins.model.*
import hudson.security.*
//public key mas9abt
	def publicKey='ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA5z/wbTJL3tRObu1YeMO4CfTkRgoGGL6GKuG51W55c1CylRaOAWUKYURhdzu1UabRkDc5D8a0a8BsA0F8llasOJjjcZgXYYcEgwTv7Tffe6Wqkoc0NDbE0FRKJzBQjNLcJb+TZxiabgi2LS6HlXZ2jK51p7XD4BVHxw9sI6SdCy5FL+WUSM+juK9QUj7taeFcRMy4oyrPf8iXPhEFBsd4KdpQ0FGHki7TW6vfX8SPU9VVybizzReuWdThiZD9WEql1uTrQP6SKlGnuf2WlRXvQRoHMB0R/I2LSjCuWtz7WIkW90CGi3IsYJg5e5Ard1BPl9JIvNky/RpBeDEjv1bv7Q== jenkins@abtv5533'
//create user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
		
def user =hudsonRealm.createAccount("mas9abt","admin")

    
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
instance.setSecurityRealm(hudsonRealm)
instance.save()

def strategy = new hudson.security.FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(true)
instance.setAuthorizationStrategy(strategy)
println 'set Authorization Strategy to "FullControlOnceLoggedInAuthorizationStrategy"'
instance.save();
