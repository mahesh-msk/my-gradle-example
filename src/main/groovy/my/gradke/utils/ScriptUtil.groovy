package my.gradke.utils

/**
 *Groovy class provides helper script util methods.
 * @author FAF2KOR
 *
 */
class ScriptUtil {
	
	/**
	 * Function will return the groovy script for approving pending script approvals.
	 */
	def static String sciptApprovalData(){
		return '''
                import org.jenkinsci.plugins.scriptsecurity.scripts.*;
                import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.*;


              
                
                def sa = ScriptApproval.get()

				//list pending approvals
				for (ScriptApproval.PendingScript pending : sa.getPendingScripts()) {
				       sa.approveScript(pending.getHash());
     	               println "Approved : " + pending.script
				}
				
				for (ScriptApproval.PendingSignature pending : sa.getPendingSignatures()) {
				        sa.approveSignature(pending.signature);
     					println "Approved : " + pending.signature
				}
              
                def alreadyApproved = sa.getApprovedSignatures()

                def SIGNATURES = ["method com.sonyericsson.jenkins.plugins.bfa.PluginImpl getKnowledgeBase",
				"method com.sonyericsson.jenkins.plugins.bfa.db.KnowledgeBase getCauses",
				"method com.sonyericsson.jenkins.plugins.bfa.db.KnowledgeBase removeCause java.lang.String",
				"method com.sonyericsson.jenkins.plugins.bfa.db.KnowledgeBase saveCause com.sonyericsson.jenkins.plugins.bfa.model.FailureCause",
				"method com.sonyericsson.jenkins.plugins.bfa.model.FailureCause getComment",
				"method com.sonyericsson.jenkins.plugins.bfa.model.FailureCause getDescription",
				"method com.sonyericsson.jenkins.plugins.bfa.model.FailureCause getId",
				"method com.sonyericsson.jenkins.plugins.bfa.model.FailureCause getIndications",
				"method com.sonyericsson.jenkins.plugins.bfa.model.IFailureCauseMetricData getCategories",
				"method hudson.model.Saveable save",
				"method jenkins.model.Jenkins reload",
				"method org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval getPendingScripts",
				"method org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval getPendingSignatures",
				"new com.sonyericsson.jenkins.plugins.bfa.model.FailureCause java.lang.String java.lang.String java.lang.String java.lang.String java.util.Date java.util.List java.util.List java.util.List",
			    "new com.sonyericsson.jenkins.plugins.bfa.model.indication.BuildLogIndication java.lang.String",
				"new com.sonyericsson.jenkins.plugins.bfa.model.indication.MultilineBuildLogIndication java.lang.String",
				"new java.util.ArrayList",
				"staticMethod com.sonyericsson.jenkins.plugins.bfa.PluginImpl getInstance",
				"staticMethod jenkins.model.Jenkins getInstance",
				"staticMethod org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval get",
                "staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.net.URL",
                "method org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper getRawBuild",
                "method hudson.model.Actionable getAction java.lang.Class",
                "method com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction getFailureCauseDisplayData",
                "method com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseDisplayData getFoundFailureCauses",
                "method com.sonyericsson.jenkins.plugins.bfa.model.IFailureCauseMetricData getName",
                "method com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause getDescription",
                "method com.sonyericsson.jenkins.plugins.bfa.model.FoundFailureCause getIndications",
                "method com.sonyericsson.jenkins.plugins.bfa.model.indication.FoundIndication getPattern",
                "method com.sonyericsson.jenkins.plugins.bfa.model.indication.FoundIndication getFirstMatchingLine"]
				
				// pre script approval
				for(signature in SIGNATURES){
                    if (!alreadyApproved.contains(signature)) {
                          sa.approveSignature(signature)
                    }
                }
			''' 
	}
	
}
