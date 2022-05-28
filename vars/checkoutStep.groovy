// repo, branch, directory, diff_id
def call(Map pipelineParams) {
    checkout ([
        $class: 'GitSCM',
        branches: [[
            name: "refs/heads/${pipelineParams.branch}"
        ]],
        extensions: [[
            $class: 'RelativeTargetDirectory',
            relativeTargetDir: "${pipelineParams.directory}"
        ]],
        userRemoteConfigs: [[
            credentialsId: 'git-ssh',
            url: "${pipelineParams.repo}"
        ]]
    ])

    // Apply patch if specified
    script {

        echo PhabricatorPlugin.DIFFERENTIAL_ID_FIELD

        if (pipelineParams.diff_id != '') {
            sh "cd ${pipelineParams.directory} && \
                arc patch ${pipelineParams.diff_id}"
        }
    }
}
