job('{{ .Values.github.jobsRepo }}-jobs') {

  displayName('Re-create {{ .Values.github.jobsRepo }} jobs')
  description('Re-create all jobs from the {{ .Values.github.jobsRepo }} repository')

  blockOn('.*-pipeline') {
    blockLevel('GLOBAL')
    scanQueueFor('ALL')
  }

  logRotator {
    artifactDaysToKeep(10)
  }

  scm {
    git {    
      branch('master')

      remote {
        credentials('github-access')
        url('https://{{ .Values.github.baseUrl }}/{{ .Values.github.jobsOrg }}/{{ .Values.github.jobsRepo }}')
      }
    }
  }

  steps {
    jobDsl {
      additionalClasspath(['lib/*.jar','src/main/groovy'].join('\\n'))
      ignoreExisting(false)
      removedJobAction('DELETE')
      removedViewAction('DELETE')
      sandbox(true)
      targets('jobdsl/**/*.groovy')
    }
  }
}

pipeline {
  
  blockOn('.*-pipeline') {
    blockLevel('GLOBAL')
    scanQueueFor('ALL')
  }

  logRotator {
    artifactDaysToKeep(10)
  }


  stage('Checkout') {
    checkout scm
  }

  stage('Build') {
    steps {
      jobDsl {
        additionalClasspath(['lib/*.jar','src/main/groovy'].join('\\n'))
        ignoreExisting(false)
        removedJobAction('DELETE')
        removedViewAction('DELETE')
        sandbox(true)
        targets('jobdsl/**/*.groovy')
      }
    }
  }
}