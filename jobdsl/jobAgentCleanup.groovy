#!/bin/groovy
pipelineJob('agent-cleanup') {
  displayName('Orphaned jenkins agent reaper')

  definition {
    cps {
      script(readFileFromWorkspace('scripts/agent-cleanup.groovy'))
      sandbox()
    }
  }

  triggers {    
    cron('@daily')
  }

  logRotator {
    numToKeep(3)
  }
}