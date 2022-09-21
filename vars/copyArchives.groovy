// directory, target 
def call(Map pipelineParams) {
    script {
        // check if file exists
        def containsDeps = sh(script: "test -f ${pipelineParams.directory}/dependencies.txt && echo true || echo false", returnStdout: true)

        if (containsDeps.contains('true')) {
            echo 'Unarchiving dependencies needed...'

            // read file content to store it on a variable
            def deps_str = sh(script: "cat ${pipelineParams.directory}/dependencies.txt", returnStdout: true)

            // convert deps_str to an array
            def deps_arr = deps_str.trim().split(',')

            for (int i = 0; i < deps_arr.size(); ++i) {
                // copy artifacts from last succesful build
                copyArtifacts projectName: "${deps_arr[i]}_central"
                target: pipelineParams.target

                sh "cd ${deps_arr[i]} && \
                tar -xzvf *.tar.gz"
            }
        }
                                        
        if (containsDeps.contains('false')) {
            echo 'This project does not have dependencies to unarchive'
        }
    }
}