
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import com.ibm.team.build.internal.hjplugin.*;
import hudson.tools.ZipExtractionInstaller;
import hudson.tools.InstallSourceProperty;

def instance = Jenkins.getInstance()
def descriptor = instance.getDescriptor("com.ibm.team.build.internal.hjplugin.RTCBuildToolInstallation")

// *************** Setting values for RTC BuildToolKit current for both linux and !linux (a.k.a Windows) labels ******************
ZipExtractionInstaller linuxConfigCurrent = new ZipExtractionInstaller ("linux", "http://abtv5209.de.bosch.com:8281/RTC/buildtoolkit/RTC-BuildSystem-Toolkit-Linux-6.0.6.zip", "jazz/buildsystem/buildtoolkit");
ZipExtractionInstaller windowsConfigCurrent = new ZipExtractionInstaller ("!linux", "http://abtv5209.de.bosch.com:8281/RTC/buildtoolkit/RTC-BuildSystem-Toolkit-Win-6.0.6.zip", "jazz/buildsystem/buildtoolkit");
InstallSourceProperty rtcConfigCurrent = new InstallSourceProperty([linuxConfigCurrent, windowsConfigCurrent])

RTCBuildToolInstallation rtcBuildToolConfigCurrent = new RTCBuildToolInstallation ('current', '', [rtcConfigCurrent])
descriptor.setInstallations(rtcBuildToolConfigCurrent)

descriptor.save()

println descriptor.getInstallations()

