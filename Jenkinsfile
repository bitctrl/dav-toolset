pipeline {
    agent {
        label '!QNX'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '2'))
        disableConcurrentBuilds()
        timeout(time: 2, unit: 'HOURS')
    }

    tools {
        jdk 'AdoptOpenJDK17'
    }

   stages {
        stage ('Initialize') {
            steps {
                // use name of the patchset as the build name
                buildName "#${BUILD_NUMBER} (${GIT_BRANCH})  @ ${NODE_NAME}"
                buildDescription "Executed @ ${NODE_NAME}"
            }
        }

        stage ('Build') {
            steps {
               withGradle {
               		sh 'chmod +x ./gradlew'
                    sh './gradlew clean build --refresh-dependencies'
                }
            }
        }

		 stage ('Deploy') {
            steps {
               withGradle {
               		sh 'chmod +x ./gradlew'
                    sh './gradlew publishMavenJavaPublicationToUserSnapshotRepository'
                }
            }
        }

    }
    post {

        always {
        	//junit 'build/test-results/test/**/*.xml'
            archiveArtifacts artifacts: '**/build/distributions/*.zip', fingerprint: false

             recordIssues(
                    tools: [taskScanner(highTags: 'FIXME', ignoreCase: true, includePattern: '**/*.java', lowTags: 'XXX', normalTags: 'TODO'),
                            spotBugs(useRankAsPriority: true), checkStyle()]
                )
            chuckNorris()
        }
        success{
            cleanWs()
        }

    }

}
