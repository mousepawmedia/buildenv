// repo, project
def call(Map pipelineParams) {
    pipeline {
        parameters {
            string(name: 'BRANCH', defaultValue: 'devel')
            string(name: 'PHID', defaultValue: '')
            string(name: 'DIFF_ID', defaultValue: '')
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
                                'directory': 'target',
                                'diff_id': params.DIFF_ID
                            )
                        }
                    }
                    stage('Copy Archive') {
                        steps {
                            script {
                                // check if file exists
                                def containsDeps = sh(script: "test -f target/dependencies.txt && echo true || echo false", returnStdout: true)

                                if (containsDeps.contains('true')) {
                                    echo 'Unarchiving dependencies needed...'

                                    // read file content to store it on a variable
                                    def deps_str = sh(script: "cat target/dependencies.txt", returnStdout: true)

                                    // convert deps_str to an array
                                    def deps_arr = deps_str.trim().split(',')

                                    for (int i = 0; i < deps_arr.size(); ++i) {
                                        // copy artifacts from last succesful build
                                        copyArtifacts projectName: "${deps_arr[i]}_central"
                                        target: "workspace/focal/clang"

                                        sh "cd ${deps_arr[i]} && \
                                            tar -xzvf *.tar.gz"
                                    }
                                }
                                        
                                if (containsDeps.contains('false')) {
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
                            sh "cd target && \
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
                                    'directory': 'target',
                                    'diff_id': params.DIFF_ID
                                )
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
                                script {
                                    // check if file exists
                                    def containsDeps = sh(script: "test -f target/dependencies.txt && echo true || echo false", returnStdout: true)

                                    if (containsDeps.contains('true')) {
                                        echo 'Unarchiving dependencies needed...'

                                        // read file content to store it on a variable
                                        def deps_str = sh(script: "cat target/dependencies.txt", returnStdout: true)

                                        // convert deps_str to an array
                                        def deps_arr = deps_str.trim().split(',')

                                        for (int i = 0; i < deps_arr.size(); ++i) {
                                            // copy artifacts from last succesful build
                                            copyArtifacts projectName: "${deps_arr[i]}_central"
                                            target: "workspace/${OS}/${COMPILER}"

                                            sh "cd ${deps_arr[i]} && \
                                                tar -xzvf *.tar.gz"
                                        }
                                    }
                                            
                                    if (containsDeps.contains('false')) {
                                        echo 'This project does not have dependencies to unarchive'
                                    }
                                }
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
                                script {  
                                    sh "${env.SHELL_BEFORE}"

                                    // goldilocks must be built from stable branch
                                    if (env.PROJECT == 'goldilocks') {
                                        sh "cd ${env.PROJECT} && \
                                            git checkout stable"
                                    }

                                    sh "cd target && \
                                        make ${env.MAKE_WHAT}"
                                    
                                    sh "${env.SHELL_AFTER}"
                                }
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
                                sh "cd target && \
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
                                sh "cd target && \
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
