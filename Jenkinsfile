@Library('cicd-global-library') _

dockerPipeline {
    // the application name
    name = "employer-account-service"

    // the docker tag semver (X.X.X)
    major_version = "0"
    minor_version = "0"

    useFabric8 = true

    dockerBranchRegex = "master|develop"

    // channel where slack messages are posted (skipped if not set)
    // slackChannel = "employer-build-status"

    buildCommand = {
        // perform additional tasks as part of the compilation
        // * static code analysis
        // * scalafmt check done by `scalafmtCheckAll` command alias
        sh("sbt clean scalafmtSbtCheck scalafmtCheckAll ci:compile it:compile stage")
    }

    testCommand = {
        sh("sbt coverage test \"it:testOnly *AccountServiceRestSpec\" coverageReport")
    }
}
