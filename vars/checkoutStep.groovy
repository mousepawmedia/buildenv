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

            withCredentials([[
                $class: 'StringCredentials', 
                credentialsId: 'd29a819a-e02a-442b-bf47-049f9d77562f', 
                variable: 'TOKEN'
            ]]) {
                
            sh '''
                #!/bin/bash

                set +x
                cd $pipelineParams.directory
                
                if arc patch D$pipelineParams.revision_id --conduit-token $TOKEN; then
                    echo "Successfully patched D$pipelineParams.revision_id"
                else 
                    echo "Installing certificate"
                    arc install-certificate
                    arc patch D$pipelineParams.revision_id
                fi
            '''
            }
        }
    }
}
