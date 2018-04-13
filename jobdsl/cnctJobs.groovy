@Grab(group='org.yaml', module='snakeyaml', version='1.17') 

import models.*
import templates.*
import jenkins.model.Jenkins

import hudson.FilePath
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import groovy.io.FileType

/**
 * Simple value object to store configuration information for a project.
 *
 * Member variables without a value defined are required and those with a value
 * defined are optional.
 */

void jobDslConfig(pipeline) {
  multibranchPipelineJob(pipeline.uniqueId) {
    description(pipeline.description)
    displayName(pipeline.displayName)

    if (pipeline.type == 'cnct') {
      // defaults Jenkinsfile
      configure { project ->
        project.name = Jenkins.instance.getDescriptor('PipelineMultiBranchDefaultsProject',).clazz.getCanonicalName()
      }
      configure {
        Node factoryNode = it / factory
        factoryNode.attributes().put 'class', 'org.jenkinsci.plugins.pipeline.multibranch.defaults.PipelineBranchDefaultsProjectFactory'
        (factoryNode / 'owner').attributes().put 'class', 'org.jenkinsci.plugins.pipeline.multibranch.defaults.PipelineMultiBranchDefaultsProject'
      }
    }

    branchSources {
      branchSource {
        source {
          github {
            repoOwner(pipeline.org)
            repository(pipeline.repo)
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

    configure { project ->
      project / 'sources' / 'data' / 'jenkins.branch.BranchSource' / source(class: 'org.jenkinsci.plugins.github_branch_source.GitHubSCMSource') {
        id(pipeline.uniqueId)
        apiUri(pipeline.apiUrl)
        credentialsId(pipeline.credentials)
        repoOwner(pipeline.org)
        repository(pipeline.repo)

        traits() {
          'org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait'() {
            strategyId(3)
          }
          
          'org.jenkinsci.plugins.github__branch__source.ForkPullRequestDiscoveryTrait'() {
            strategyId(1)
            trust(class: 'org.jenkinsci.plugins.github_branch_source.ForkPullRequestDiscoveryTrait$TrustPermission')
          }

          'jenkins.plugins.git.traits.RefSpecsSCMSourceTrait'() {
            'templates'() {
              'jenkins.plugins.git.traits.RefSpecsSCMSourceTrait_-RefSpecTemplate'() {
                value('+refs/heads/master:refs/remotes/@{remote}/master')
              }
            }
          }
        }
      }
    }
  }
}

void createJobs() {
  def constr = new CustomClassLoaderConstructor(this.class.classLoader)
  def yaml = new Yaml(constr)

  // Build a list of all config files ending in .yml
  if (hudson.model.Executor.currentExecutor()) {
    def cwd = hudson.model.Executor.currentExecutor().getCurrentWorkspace().absolutize()
    def configFiles = new FilePath(cwd, 'configs').list('*.yaml')

    // Create/update a pull request job for each config file
    configFiles.each { file ->
      def pipeline = yaml.load(file.readToString())
      jobDslConfig(pipeline)
    }
  } else {
    def configsPath = new File('./configs')
    configsPath.eachFileMatch(FileType.FILES, , ~/^.*\.yaml/) {
      def pipeline = yaml.load(readFileFromWorkspace(it.name))
      jobDslConfig(pipeline)
    }
  }

  // re register all webhooks
  Jenkins.instance.getExtensionList('com.cloudbees.jenkins.GitHubWebHook')[0].get().reRegisterAllHooks()
}

createJobs()