#!/bin/groovy
import models.*
import templates.*

import hudson.FilePath
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor

/**
 * Simple value object to store configuration information for a project.
 *
 * Member variables without a value defined are required and those with a value
 * defined are optional.
 */

void createJobs() {
  def constr = new CustomClassLoaderConstructor(this.class.classLoader)
  def yaml = new Yaml(constr)

  // Build a list of all config files ending in .yml
  def cwd = hudson.model.Executor.currentExecutor().getCurrentWorkspace().absolutize()
  def configFiles = new FilePath(cwd, 'configs').list('*.yaml')

  // Create/update a pull request job for each config file
  configFiles.each { file ->
    def pipeline = yaml.load(file.readToString())
    if (pipeline.type == 'cnct') {
      multibranchPipelineJob(pipeline.uniqueId) {
        description(pipeline.description)
        displayName(pipeline.displayName)

        branchSources {
          branchSource {
            source {
              github {
                id (pipeline.uniqueId)
                repoOwner(pipeline.org)
                repository(pipeline.repo)
                credentialsId(pipeline.credentials)
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
                  buildRetentionBranchProperty {
                    buildDiscarder {
                      logRotator {
                        daysToKeepStr("${pipeline.keepDays}")
                        numToKeepStr("")
                        artifactDaysToKeepStr("")
                        artifactNumToKeepStr("")
                      }
                    }
                  }
                  triggerPRCommentBranchProperty {
                    commentBody('.*test this please.*')
                  }
                }
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
                  credentials(pipeline.credentials)
                  url('https://{{ .Values.github.baseUrl }}/{{ .Values.github.jobsOrg }}/{{ .Values.github.jobsRepo }}')
                  scriptPath('Jenkinsfile')
                }
              }
            }
          }
        }      
      }
    }
  }
}

createJobs()