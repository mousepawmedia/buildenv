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
                    withCredentials([usernamePassword(credentialsId: '33c8e2d8-db88-4155-966f-2a818cbd2dd0', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "sudo docker login -u ${USERNAME} -p ${PASSWORD} registry.mousepawmedia.com"
                    }
                }
            }

            stage('Build & Push mpm-* Dockerfiles') {
                matrix {
                    axes {
                        axis {
                            name 'DOCKERFILE'
                            values 'bionic', 'focal', 'jammy'
                        }
                    }
                    stages {
                        stage('Build') {
                            steps {
                                echo "Building Dockerfiles..."
                                sh "sudo docker build -t registry.mousepawmedia.com/mpm-${DOCKERFILE}:latest target/dockerfiles/mpm-${DOCKERFILE}"
                            }
                        }
                        stage('Publish') {
                            steps {
                                echo "Pushing images to registry.mousepawmedia.com"
                                sh "sudo docker image push registry.mousepawmedia.com/mpm-${DOCKERFILE}:latest"
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
                            values 'bionic', 'focal', 'jammy'
                        }
                    }
                    stages {
                        stage('Build') {
                            steps {
                                echo "Building Dockerfiles..."
                                sh "sudo docker build -t registry.mousepawmedia.com/jenkins.mpm-${DOCKERFILE}:latest target/dockerfiles/jenkins.mpm-${DOCKERFILE}"
                            }
                        }
                        stage('Publish') {
                            steps {
                                echo "Pushing images to registry.mousepawmedia.com"
                                sh "sudo docker image push registry.mousepawmedia.com/jenkins.mpm-${DOCKERFILE}:latest"
                            }
                        }
                    }
                }
            }
        }
    }
}