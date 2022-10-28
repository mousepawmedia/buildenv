// repo, project
def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'BRANCH', defaultValue: 'devel')
            string(name: 'PHID', defaultValue: '')
            string(name: 'DIFF_ID', defaultValue: '')
            string (name: 'REVISION_ID', defaultValue: '')
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
            stage('Canary') {
                agent {
                    /* The canary build, will use Focal/Clang. This must be
                     * explicitly declared for the Canary build to work, as it
                     * relies on the Central builds.
                     */
                    node {
                        label "mpm-focal"
                        customWorkspace "/workspace/focal/clang"
                    }
                }
                stages {
                    stage('Checkout') {
                        steps {
                            checkoutStep(
                                'repo': env.REPO,
                                'branch': params.BRANCH,
                                'directory': env.PROJECT,
                            )
                        }
                    }
                    stage('Patch Revision') {
                        when {
                            expression { params.DIFF_ID != '' }
                        }
                        steps {
                            withCredentials([string(credentialsId: 'PhabricatorConduitKey', variable: 'TOKEN')])  {
                                sh "cd ${env.PROJECT} && \
                                    arc patch D${params.REVISION_ID} --conduit-token ${TOKEN}"
                            }
                        }
                    }
                    stage('Copy Archive') {
                        steps {
                            copyArchives(
                                'directory': env.PROJECT,
                                'target': 'workspace/focal/clang'
                            )
                        }
                    }
                    stage('Build') {
                        options {
                            timeout(time: 240, unit: "MINUTES", activity: true)
                        }
                        steps {
                            sh "${env.SHELL_BEFORE}"

                            sh "cd ${env.PROJECT} && \
                                make tester_debug"
                                
                            sh "${env.SHELL_AFTER}"
                        }
                    }
                    stage('Clean workspace') {
                        steps {
                            sh 'rm -r -f *'
                        }
                    }
                }
            }
            stage('Matrix') {
                matrix {
                    agent {
                        node {
                            label "mpm-${env.OS}"
                            customWorkspace "/workspace/${OS}/${COMPILER}"
                        }
                    }
                    when { 
                        anyOf {
                            expression { params.OS_FILTER == 'all' }
                            expression { params.OS_FILTER == env.OS }
                        } 
                    }
                    axes {
                        axis {
                            name 'OS'
                            values 'bionic', 'focal'
                        }
                        axis {
                            name 'COMPILER'
                            values 'clang'
                        }
                        axis {
                            name 'TARGET'
                            values 'debug', 'release'
                        }
                    }
                    stages {
                        stage('Checkout') {
                            steps {
                                checkoutStep(
                                    'repo': env.REPO,
                                    'branch': params.BRANCH,
                                    'directory': env.PROJECT,
                                )
                            }
                        }
                        stage('Patch Revision') {
                            when {
                                expression { params.DIFF_ID != '' }
                            }
                            steps {
                                withCredentials([string(credentialsId: 'PhabricatorConduitKey', variable: 'TOKEN')])  {
                                    sh "cd ${env.PROJECT} && \
                                        arc patch D${params.REVISION_ID} --conduit-token ${TOKEN}"
                                }
                            }
                        }
                        stage('Setup Environment') {
                            environment {
                                CC = "${env.COMPILER == 'clang' ? 'clang' : 'gcc' }"
                                CPP = "${env.COMPILER == 'clang' ? 'clang++' : 'g++' }"
                            }
                            steps {
                                sh "sudo update-alternatives --set cc /usr/bin/${env.CC} && \
                                    sudo update-alternatives --set c++ /usr/bin/${env.CPP}"
                            }
                        }
                        stage('Copy Archive') {
                            steps {
                                copyArchives(
                                    'directory': env.PROJECT,
                                    'target': "workspace/${OS}/${COMPILER}"
                                )
                            }
                        }
                        stage('Build') {
                            options {
                                timeout(time: 240, unit: "MINUTES", activity: true)
                            }
                            environment {
                                MAKE_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "${env.SHELL_BEFORE}"

                                sh "cd ${env.PROJECT} && \
                                    make ${env.MAKE_WHAT}"
                                    
                                sh "${env.SHELL_AFTER}" 
                            }
                        }
                        stage('Test') {
                            options {
                                timeout(time: 240, unit: "MINUTES", activity: true)
                            }
                            environment {
                                RUN_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "cd ${env.PROJECT} && \
                                ./${env.RUN_WHAT} --runall"
                            }
                        }
                        stage('Valgrind') {
                            options {
                                timeout(time: 240, unit: "MINUTES", activity: true)
                            }
                            environment {
                                RUN_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "cd ${env.PROJECT} && \
                                valgrind --leak-check=full --errors-for-leak-kinds=all --error-exitcode=1 ./${env.RUN_WHAT} --runall"
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
        }
        post {
            always {
                script {
                    if (params.PHID != '') {
                        // If a Phabricator PHID was provided...
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
}
