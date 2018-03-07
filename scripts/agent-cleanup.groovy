@NonCPS
def deleteOfflineAgents() {
  for (agent in hudson.model.Hudson.instance.slaves) {
    if (agent.getComputer().isOffline()) {
      agent.getComputer().setTemporarilyOffline(true, null);
      agent.getComputer().doDoDelete();
    }
  }
}

node {
  stage('Cleanup offline/suspended agents') {
    deleteOfflineAgents()
  }
}