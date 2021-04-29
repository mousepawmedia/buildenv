// project
// branch
// repo

def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'PHID', defaultValue: '')
            string(name: 'DIFF_ID', defaultValue: '')
            choice(name: 'OS_FILTER', choices: ['all', 'bionic', 'focal', 'hirsute'], description: 'Run on specific platform.')
        }
        environment {
            PROJECT = pipelineParams.project
            BRANCH = pipelineParams.branch
            REPO = pipelineParams.repo
        }

        agent any

        stages {
            stage('MatrixBuild') {
                matrix {
                    agent {
                        label "mpm-${env.OS}"
                    }
                    when { anyOf {
                        expression { params.OS_FILTER == 'all' }
                        expression { params.OS_FILTER == env.OS }
                    } }
                    axes {
                        axis {
                            name 'OS'
                            values 'bionic', 'focal', 'hirsute'
                        }
                        axis {
                            name 'COMPILER'
                            values 'clang', 'gcc'
                        }
                        axis {
                            name 'TARGET'
                            values 'debug', 'release'
                        }
                    }
                    stages {
                        stage('Checkout') {
                            steps {
                                checkout ([
                                    $class: 'GitSCM',
                                    branches: [[
                                        name: "refs/heads/${env.BRANCH}"
                                    ]],
                                    extensions: [[
                                        $class: 'RelativeTargetDirectory',
                                        relativeTargetDir: "${env.PROJECT}"
                                    ]],
                                    userRemoteConfigs: [[
                                        credentialsId: 'git-ssh',
                                        url: env.REPO
                                    ]]
                                ])
                            }
                        }
                        stage('Apply Differential') {
                            // If a Phabricator DIFF ID was provided...
                            when { not {
                                expression { params.DIFF_ID == '' }
                            } }
                            steps {
                                // Apply Phabricator Differential, if any
                                sh 'arc patch ${params.DIFF_ID}'
                            }
                        }
                        stage('Setup Environment') {
                            when {
                                expression { env.COMPILER == 'gcc' }
                            }
                            environment {
                                CC = "${env.COMPILER == 'clang' ? 'clang' : 'gcc' }"
                                CPP = "${env.COMPILER == 'clang' ? 'clang++' : 'g++' }"
                            }
                            steps {
                                sh "sudo update-alternatives --set cc /usr/bin/${env.CC} && \
                                sudo update-alternatives --set c++ /usr/bin/${env.CPP}"
                            }
                        }
                        stage('Build') {
                            options {
                                timeout(time: 3, unit: "MINUTES", activity: true)
                            }
                            environment {
                                MAKE_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "cd ${env.PROJECT} && \
                                echo \"${env.OS}/${env.COMPILER}: Building ${env.MAKE_WHAT}\" >> .phabricator-comment && \
                                make ${env.MAKE_WHAT}"
                            }
                        }
                        stage('Test') {
                            options {
                                timeout(time: 3, unit: "MINUTES", activity: true)
                            }
                            environment {
                                RUN_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "cd ${env.PROJECT} && \
                                echo \"${env.OS}/${env.COMPILER}: Testing ${env.RUN_WHAT}\" >> .phabricator-comment && \
                                ./${env.RUN_WHAT} --runall"
                            }
                        }
                        stage('Valgrind') {
                            options {
                                timeout(time: 3, unit: "MINUTES", activity: true)
                            }
                            environment {
                                RUN_WHAT = "${env.TARGET == 'debug' ? 'tester_debug' : 'tester' }"
                            }
                            steps {
                                sh "cd ${env.PROJECT} && \
                                echo \"${env.OS}/${env.COMPILER}: Valgrind Testing ${env.RUN_WHAT}\" >> .phabricator-comment && \
                                valgrind --leak-check=full --errors-for-leak-kinds=all --error-exitcode=1 ./${env.RUN_WHAT} --runall"
                            }
                        }
                        stage('Post Partial') {
                            steps {
                                step([
                                    $class: 'PhabricatorNotifier',
                                    commentFile: '.phabricator-comment',
                                    commentOnSuccess: true,
                                    commentSize: '10000',
                                    commentWithConsoleLinkOnFailure: true,
                                    customComment: true,
                                    preserveFormatting: false,
                                    sendPartialResults: true
                                ])
                            }
                        }
                    }
                }
            }
            stage('Post') {
                steps {
                    step([
                        $class: 'PhabricatorNotifier',
                        commentFile: '.phabricator-comment',
                        commentOnSuccess: true,
                        commentSize: '10000',
                        commentWithConsoleLinkOnFailure: true,
                        customComment: true,
                        preserveFormatting: false,
                        sendPartialResults: true
                    ])
                }
            }
        }
    }
}
