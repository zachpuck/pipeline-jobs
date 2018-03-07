pipelineJob('Jenkins Offline Agent Cleanup') {
  definition {
    cps {
      script(readFileFromWorkspace('scripts/agent-cleanup.groovy'))
    }
  }

  triggers {    
    cron('@daily')
  }

  logRotator {
    numToKeep(3)
  }
}