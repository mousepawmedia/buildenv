// repo, project
def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'BRANCH', defaultValue: 'devel')
            string(name: 'PHID', defaultValue: '')
            choice(name: 'OS_FILTER', choices: ['all', 'bionic', 'focal'], description: 'Run on specific platform.')
        }
        environment {
            PROJECT = "${pipelineParams.project}"
            REPO = "${pipelineParams.repo}"
            SHELL_BEFORE = "${pipelineParams.containsKey('shell_before') ? pipelineParams.shell_before : "."}"
            SHELL_AFTER = "${pipelineParams.containsKey('shell_after') ? pipelineParams.shell_after : "."}"
        }

        agent any

        stages {
            stage('Matrix') {
                matrix {
                    agent {
                        node {
                            label "mpm-${OS}"
                            customWorkspace "/workspace/${OS}/${COMPILER}"
                        }
                    }
                    when { 
                        anyOf {
                            expression { params.OS_FILTER == 'all' }
                            expression { params.OS_FILTER == OS }
                        } 
                    }
                    axes {
                        axis {
                            name 'OS'
                            values 'bionic', 'focal'
                        }
                        axis {
                            name 'COMPILER'
                            values 'clang', 'gcc'
                        }
                    }
                    stages {
                        stage('Checkout') {
                            steps {
                                checkoutStep(
                                    'repo': env.REPO,
                                    'branch': params.BRANCH,
                                    'directory': env.PROJECT,
                                    'diff_id': ''
                                )
                            }
                        }
                        stage('Setup Environment') {
                            environment {
                                CC = "${COMPILER == 'clang' ? 'clang' : 'gcc' }"
                                CPP = "${COMPILER == 'clang' ? 'clang++' : 'g++' }"
                            }
                            steps {
                                sh "sudo update-alternatives --set cc /usr/bin/${env.CC} && \
                                    sudo update-alternatives --set c++ /usr/bin/${env.CPP}"
                            }
                        }
                        stage('Copy Archive') {
                            steps {
                                script {

                                    def containsDeps = sh(script: "test -f ${env.PROJECT}/dependencies_central.txt && echo true", returnStdout: true)

                                    if (containsDeps) {
                                        echo 'Unarchiving dependencies needed...'

                                        def deps_str = sh(script: 'cat dependencies_central.txt', returnStdout: true)

                                        def deps_arr = deps_str.split(',')

                                        for (int i = 0; i < deps_arr.size(); ++i) {
                                            // Copy artifacts from last succesful build
                                            copyArtifacts projectName: "${deps[i]}_central"
                                            target: "workspace/${OS}/${COMPILER}"

                                            sh "cd ${deps_arr[i]} && \
                                            tar -xzvf *.tar.gz"
                                        }

                                    } else {
                                        echo 'This project does not have dependencies to unarchive'
                                    }
                                }
                            }
                        }
                        stage('Build') {
                            options {
                                timeout(time: 240, unit: "MINUTES", activity: true)
                            }
                            steps {
                                sh "${env.SHELL_BEFORE}"
                                // if libdeps is being built Opus has to be reconfigured.
                                script {
                                    if (env.PROJECT == "libdeps") {
                                        sh "cd ${env.PROJECT} && \
                                            make ubuntu-fix-aclocal"
                                    }
                                }
                                sh "cd ${env.PROJECT} && make ready"
                                sh "${env.SHELL_AFTER}"
                            }
                        }
                        stage('Archive') {
                            steps {
                                script {
                                    if (env.PROJECT == "libdeps") {
                                        // sh "cd ${env.PROJECT} && \
                                        // tar -czvf ${env.PROJECT}.tar.gz libs --transform 's,^,${env.PROJECT}/,'"

                                        sh "cd ${env.PROJECT} && \
                                        tar -czvf ${env.PROJECT}.tar.gz libs"
                                    } else {
                                        sh "cd ${env.PROJECT} && \
                                        tar -czvf ${env.PROJECT}.tar.gz ${env.PROJECT}"
                                    }
                                }

                                archiveArtifacts artifacts: "${env.PROJECT}/*.tar.gz",
                                allowEmptyArchive: false,
                                caseSensitive: true,
                                onlyIfSuccessful: true
                            }
                        }
                        stage('Clean workspace') {
                            steps {
                                sh 'rm -r -f *'
                            }
                        }
                        stage('Report') {
                            // If a Phabricator PHID was provided...
                            when { not {
                                expression { params.PHID == '' }
                            } }
                            steps {
                                step([
                                    $class: 'PhabricatorNotifier',
                                    //commentFile: '.phabricator-comment',
                                    commentOnSuccess: true,
                                    commentSize: '1000',
                                    commentWithConsoleLinkOnFailure: true,
                                    customComment: false,
                                    preserveFormatting: false,
                                    sendPartialResults: true
                                ])
                            }
                        }
                    }
                }
            }
            stage('Final Report') {
                // If a Phabricator PHID was provided...
                when { not {
                    expression { params.PHID == '' }
                } }
                steps {
                    step([
                        $class: 'PhabricatorNotifier',
                        //commentFile: '.phabricator-comment',
                        commentOnSuccess: true,
                        commentSize: '1000',
                        commentWithConsoleLinkOnFailure: true,
                        customComment: false,
                        preserveFormatting: false,
                        sendPartialResults: false
                    ])
                }
            }
        }
    }
}
