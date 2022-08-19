
//==================================================
//Artifactory Script
//==================================================
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*
import org.jfrog.hudson.util.Credentials;

def inst = Jenkins.getInstance()

def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
   def creds = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials.class,
        jenkins.model.Jenkins.instance
    )

def deployerCredentials  = creds.findResult { it.id == "ArtUser" ? it : null }
def depCredConfig=new CredentialsConfig("", "",deployerCredentials.id)
def resolverCredentials = new Credentials("", "")

def sinst = [new ArtifactoryServer(
  "rb-artifactory",
  "https://rb-artifactory.bosch.com/artifactory",
  depCredConfig,
  depCredConfig,
  300,
  false ,
  3,
0)
]
desc.setUseCredentialsPlugin(true)
desc.setArtifactoryServers(sinst)

desc.save()
