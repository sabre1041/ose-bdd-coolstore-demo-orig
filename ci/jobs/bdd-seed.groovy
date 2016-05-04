def jobs = [
    [ name: 'coolstore-app-pipeline', git: 'https://github.com/sabre1041/ose-bdd-coolstore-demo.git', scriptPath: 'projects/brms-coolstore-demo/Jenkinsfile', openShiftHost: 'master.osebdd.example.com', openShiftProject: 'coolstore-bdd', openShiftApplication: 'coolstore-app'],
    [ name: 'coolstore-rules-pipeline', git: 'https://github.com/sabre1041/ose-bdd-coolstore-demo.git', scriptPath: 'projects/coolstore-kjar-s2i/Jenkinsfile', openShiftHost: 'master.osebdd.example.com', openShiftProject: 'coolstore-bdd', openShiftApplication: 'coolstore-rules']
]

jobs.each { job ->

    workflowJob(job.name) {
		parameters {
			stringParam "OPENSHIFT_HOST",job.openShiftHost,"OpenShift Host"
			stringParam "OPENSHIFT_PROJECT",job.openShiftProject, "OpenShift Project"
			stringParam "OPENSHIFT_APPLICATION",job.openShiftApplication, "OpenShift Application"
		}

      definition {
        cpsScm {
            scm {
                git job.git
            }
            scriptPath job.scriptPath
        }    
      }
    
    }
}