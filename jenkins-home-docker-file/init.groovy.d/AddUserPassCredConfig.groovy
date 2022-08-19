
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;
def CredDict=Eval.me("['cid9abt':['ciuser201906', 'ArtUser', '']]")
CredDict.each{ k, v ->

Credentials c = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,v[1], v[2], k, v[0])


SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), c)
}
