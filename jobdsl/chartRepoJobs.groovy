#!/bin/groovy

Map pipelines = [:]
try {
  Yaml yaml = new Yaml()
  pipelines = yaml.load(readFileFromWorkspace('pipelines.yaml'))
}
catch (FileNotFoundException e) {
  out.println('Could not find pipelines.yaml!')
  return 1
} 


for (pipeline in pipelines) {
  if (pipeline.type == 'cnct') {
    multibranchPipelineJob(pipeline.uniqueId) {
      description(pipeline.description)
      displayName(pipeline.displayName)

      branchSources {
        github {
          id (pipeline.uniqueId)
          repoOwner(pipeline.org)
          repository(pipeline.repo)
          scanCredentialsId(pipelines.githubCredentials)
          buildOriginBranch(true)
          buildOriginBranchWithPR(true)
          buildOriginPRHead(false)
          buildOriginPRMerge(false)
          buildForkPRMerge(true)
          buildForkPRHead(false)
        }
      }

      strategy {
        defaultBranchPropertyStrategy {
          props {
            triggerPRCommentBranchProperty {
              commentBody('.*test this please.*')
            }
          }
        }
      }

      // defaults Jenkinsfile
      configure { node ->
        (node / 'factory').@class = 'org.jenkinsci.plugins.pipeline.multibranch.defaults.PipelineBranchDefaultsProjectFactory'
        (node / 'factory').@plugin = 'pipeline-multibranch-defaults'
      }

      orphanedItemStrategy {
        discardOldItems {
          daysToKeep(pipeline.keepDays)
        }
      }
    }
  } else if (pipeline.type == 'standard') {
    pipelineJob(pipeline.uniqueId) {
      description(pipeline.description)
      displayName(pipeline.displayName)

      if (pipeline.concurrent) {
        concurrentBuild(true)
      } else {
        concurrentBuild(false)
      }

      definition {
        cpsScm {
          lightweight(true)

          scm {
            git {
              branch('master')
              
              browser {
                githubWeb {
                  repoUrl('https://{{ .Values.github.baseUrl }}/{{ .Values.github.jobsOrg }}/{{ .Values.github.jobsRepo }}')
                }
              }

              remote {
                credentials('github-access')
                github('{{ .Values.github.jobsOrg }}/{{ .Values.github.jobsRepo }}', 'master', 'https', '{{ .Values.github.baseUrl }}')
              }
            }
          }
        }
      }      
    }
  }
}