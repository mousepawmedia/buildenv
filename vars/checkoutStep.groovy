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
    sh "${pipelineParams.diff_id == '' ? '' : 'cd ${pipelineParams.directory} && arc patch ${pipelineParams.diff_id}' }"
}
