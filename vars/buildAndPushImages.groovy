def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'BRANCH', defaultValue: 'devel')
        }
        environment {
            REPO = "${pipelineParams.repo}"
        }

        agent {
            node { 
                label 'mpm-focal'
                customWorkspace '/workspace/focal/clang'
            }
        }
        
        stages {
            stage('Checkout') {
                steps {

                sh 'docker --version'

                // checkoutStep(
                //     'repo': env.REPO,
                //     'branch': params.BRANCH,
                //     'directory': 'target',
                //     'diff_id': '',
                //     'revision_id': ''
                // )
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
        }
    }
}