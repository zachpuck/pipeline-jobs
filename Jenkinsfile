agent {
  node('master') {
    properties([
      [$class: 'BuildBlockerProperty',
       blockLevel: 'GLOBAL',
       blockingJobs: '^.*-pipeline',
       scanQueueFor: 'ALL',
       useBuildBlocker: true],
     disableConcurrentBuilds(),
     [$class: 'BuildDiscarderProperty', 
        strategy: [
          $class: 'LogRotator', 
          daysToKeepStr: '10'
        ]
      ]
    ])

    stage('Checkout') {
      checkout scm
    }

    stage('Build') {
      jobDsl targets: ['jobdsl/**/*.groovy'].join('\n'),
        removedJobAction: 'DELETE',
        removedViewAction: 'DELETE',
        ignoreExisting: false,
        sandbox: true,
        additionalClasspath: ['lib/*.jar','src/main/groovy'].join('\n')
    }
  }
}