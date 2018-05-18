# CNCT Pipeline JobDsl configuration

[JobDsl](https://github.com/jenkinsci/job-dsl-plugin) configuration and definitions from CNCT Pipeline Jenkins

## Repository structure

```
configs - YAML configurations for github repositories to be monitored by CNCT Jenkins
jobdsl - JobDsl code that will be executed by CNCT Jenkins seed job
scripts - additional groovy scripts
```

## Configuration

To add a repo to be monitored by CNCT jenkins, add a yaml file to `configs`. Two types of YAML configurations are currently supported

### type: cnct

CNCT pipeline type job. Jenkins will look for `pipeline.yaml` and `.versionfile` in the repository root. 

Example:

```
---
type: cnct
uniqueId: pipeline-jenkins
displayName: JenkinsCI pipeline
description: Pipeline Jenkins Helm chart and docker files
apiUrl: "https://api.github.com"
org: samsung-cnct
repo: pipeline-jenkins
keepDays: 10
credentials: github-access
trigger:
  - pipeline-vault
```

Setting | Description
--- | ---
type | Pipeline type (cnct)
uniqueId | unique ID for the pipeline
displayName | Pipeline display name in Jenkins dashboard
description | Pipeline description name in Jenkins dashboard
apiUrl | Github API URL
org | Github Org for this repo
repo | Github repository name
keepDays | Job data retention in days
credentials | Jenkins github credentials ID
trigger | Array of unique ids of pipelines to trigger after this pipeline completes successfully

### type: standard

Standard multibranch pipeline. Jenkins will look for `Jenkinsfile` in the repository root

Example:

```
---
type: standard
uniqueId: pipeline-jobs
displayName: Jenkins jobs pipeline
description: Job configuration repository
apiUrl: "https://api.github.com"
org: samsung-cnct
repo: pipeline-jobs
keepDays: 10
credentials: github-access
```

Setting | Description
--- | ---
type | Pipeline type (cnct)
uniqueId | unique ID for the pipeline
displayName | Pipeline display name in Jenkins dashboard
description | Pipeline description name in Jenkins dashboard
apiUrl | Github API URL
org | Github Org for this repo
repo | Github repository name
keepDays | Job data retention in days
credentials | Jenkins github credentials ID

## Additional job dsl code

All .groovy files under `jobdsl` will be executed as JobDsl code by CNCT Jenkins.

## Testing

TBD
