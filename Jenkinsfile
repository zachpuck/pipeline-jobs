node {
  options {
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
  }

  stage('Checkout') {
    checkout scm
  }

  stage('Build') {
    steps {
      jobDsl targets: ['jobdsl/**/*.groovy'].join('\n'),
       removedJobAction: 'DELETE',
       removedViewAction: 'DELETE',
       lookupStrategy: 'SEED_JOB',
       ignoreExisting: false,
       sandbox: true,
       additionalClasspath: ['lib/*.jar','src/main/groovy'].join('\n')
    }
  }
}