def call(pipelineParams) {
    parameters {
        string(name: 'BRANCH', defaultValue: 'devel')
    }
    environment {
        REPO = "${pipelineParams.repo}"
        SHELL_BEFORE = "${pipelineParams.containsKey('shell_before') ? pipelineParams.shell_before : "."}"
        SHELL_AFTER = "${pipelineParams.containsKey('shell_after') ? pipelineParams.shell_after : "."}"
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
                checkoutStep(
                    'repo': env.REPO,
                    'branch': params.BRANCH,
                    'directory': 'target',
                    'diff_id': '',
                    'revision_id': ''
                )
            }
        }
        stage('Login') {
            steps {
                sh 'docker login registry.mousepawmedia.com'
            }
        }
        stage('Build & Push mpm-* Dockerfiles') {
            matrix {
                axes {
                    axis {
                        name 'DOCKERFILE'
                        value 'bionic', 'focal'
                    }
                }
                stages {
                    stage('Build') {
                        steps {
                            sh "cd target/dockerfiles && \
                            docker image build  mpm-${DOCKERFILE}"
                        }
                    }
                    stage('Publish') {
                        steps {
                            sh "cd target/dockerfiles && \
                            docker image push registry.mousepawmedia.com/mpm-${DOCKERFILE}:latest"
                        }
                    }
                }
            }
        }
        stage('Build & Push jenkins.mpm-* Dockerfiles') {
            matrix {
                axes {
                    axis {
                        name 'DOCKERFILE'
                        value 'bionic', 'focal'
                    }
                }
                stages {
                    stage('Build') {
                        steps {
                            sh "cd target/dockerfiles && \
                            docker image build  jenkins.mpm-${DOCKERFILE}"
                        }
                    }
                    stage('Publish') {
                        steps {
                            sh "cd target/dockerfiles && \
                            docker image push registry.mousepawmedia.com/jenkins.mpm-${DOCKERFILE}:latest"
                        }
                    }
                }
            }
        }
        stage('Clean workspace') {
            steps {
                sh 'rm -rf *'
            }
        }
    }
}