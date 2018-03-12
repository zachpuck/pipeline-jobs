@Grab(group='org.yaml', module='snakeyaml', version='1.17') 

import models.*
import templates.*
import jenkins.model.Jenkins

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

    multibranchPipelineJob(pipeline.uniqueId) {
      description(pipeline.description)
      displayName(pipeline.displayName)

      if (pipeline.type == 'cnct') {
        // defaults Jenkinsfile
        configure { project ->
          project.name = Jenkins.instance.getDescriptor('PipelineMultiBranchDefaultsProject',).clazz.getCanonicalName()
          project / factory(class: Jenkins.instance.getDescriptor('PipelineBranchDefaultsProjectFactory',).clazz.getCanonicalName())
        }
      }

      branchSources {
        branchSource {
          source {
            github {
              repoOwner(pipeline.org)
              repository(pipeline.repo)
              apiUri(pipeline.apiUrl)
              credentialsId(pipeline.credentials)
              buildForkPRHead(false)
              buildForkPRMerge(true)
              buildOriginPRMerge(false)
              buildOriginBranch(true)
              buildOriginBranchWithPR(false)
              buildOriginPRHead(false)
              includes('master')
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

      orphanedItemStrategy {
        discardOldItems {
          daysToKeep(pipeline.keepDays)
        }
      }
    }

  }
}

createJobs()