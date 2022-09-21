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
            sh """
            #!/bin/bash

            cd ${pipelineParams.directory}
            
            if arc patch D${pipelineParams.revision_id}; then
                echo "Successfully patched D${pipelineParams.revision_id}"
            else 
                echo "Installing certificate"
                arc install-certificate
                arc patch D${pipelineParams.revision_id}
            fi
            """
        }
    }
}
