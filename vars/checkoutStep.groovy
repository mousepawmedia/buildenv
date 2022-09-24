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
        if (pipelineParams.diff_id != '') {
            withCredentials([string(credentialsId: 'PhabricatorConduitKey', variable: 'TOKEN')])  {
                sh "cd ${pipelineParams.directory} && \
                    arc patch D${pipelineParams.revision_id} --conduit-token ${TOKEN}"
            }
        }
    }
}
