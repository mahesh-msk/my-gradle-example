job('a-simple-shell-script') {
    description("Simple shell job generated by the DSL")
    label('linux')
    logRotator {
        numToKeep 20
    }
    steps {
        shell('echo "abc" > output.txt')
    }
    publishers {
        archiveArtifacts 'output.txt'
    }
}
