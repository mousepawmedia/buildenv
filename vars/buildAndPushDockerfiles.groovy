def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'BRANCH', defaultValue: 'devel')
        }
        environment {
            REPO = "${pipelineParams.repo}"
        }

        agent any

        stages {
            stage('Build Environment') {
                agent {
                    node {
                        label "mpm-focal"
                        customWorkspace "/workspace/focal/clang"
                    }
                }
                stages {
                    // stage('Checkout') {
                    //     steps {
                    //         checkoutStep(
                    //             'repo': env.REPO,
                    //             'branch': params.BRANCH,
                    //             'directory': 'target',
                    //             'diff_id': '',
                    //             'revision_id': ''
                    //         )
                    //     }
                    // }
                    stage('Install Docker') {
                        steps {
                            sh 'sudo apt-get install ca-certificates curl gnupg lsb-release'
                            sh 'sudo mkdir -p /etc/apt/keyrings'
                            sh 'curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg'
                            sh 'echo \
                                "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
                                $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null'
                            sh 'sudo apt-get update'
                            sh 'sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin'
                        }
                    }
                    // stage('Login') {
                    //     steps {

                    //         sh 'printenv'

                    //         sh 'docker login registry.mousepawmedia.com'
                    //     }
                    // }
                    // stage('Build & Push mpm-* Dockerfiles') {
                    //     matrix {
                    //         axes {
                    //             axis {
                    //                 name 'DOCKERFILE'
                    //                 values 'bionic', 'focal'
                    //             }
                    //         }
                    //         stages {
                    //             stage('Build') {
                    //                 steps {
                    //                     sh "cd target/dockerfiles && \
                    //                     docker image build  mpm-${DOCKERFILE}"
                    //                 }
                    //             }
                    //             stage('Publish') {
                    //                 steps {
                    //                     sh "cd target/dockerfiles && \
                    //                     docker image push registry.mousepawmedia.com/mpm-${DOCKERFILE}:latest"
                    //                 }
                    //             }
                    //         }
                    //     }
                    // }
                    // stage('Build & Push jenkins.mpm-* Dockerfiles') {
                    //     matrix {
                    //         axes {
                    //             axis {
                    //                 name 'DOCKERFILE'
                    //                 values 'bionic', 'focal'
                    //             }
                    //         }
                    //         stages {
                    //             stage('Build') {
                    //                 steps {
                    //                     sh "cd target/dockerfiles && \
                    //                     docker image build  jenkins.mpm-${DOCKERFILE}"
                    //                 }
                    //             }
                    //             stage('Publish') {
                    //                 steps {
                    //                     sh "cd target/dockerfiles && \
                    //                     docker image push registry.mousepawmedia.com/jenkins.mpm-${DOCKERFILE}:latest"
                    //                 }
                    //             }
                    //         }
                    //     }
                    // }
                    stage('Clean workspace') {
                        steps {
                            sh 'rm -rf *'
                        }
                    }
                }
            }
        }
    }
}