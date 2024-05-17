import groovy.json.JsonSlurperClassic

import java.math.RoundingMode
import java.text.DecimalFormat

BUILD_STEP = ""

GMS_APK_BUILD_LOG = "gms_build.log"
QA_APK_BUILD_LOG = "qa_build.log"

MODULE_LIST = ['app', 'domain', 'shared/original-core-ui', 'data']

LINT_REPORT_FOLDER = "lint_reports"
LINT_REPORT_ARCHIVE = "lint_reports.zip"
LINT_REPORT_SUMMARY_MAP = [:]

APP_UNIT_TEST_SUMMARY = ""
DOMAIN_UNIT_TEST_SUMMARY = ""
DATA_UNIT_TEST_SUMMARY = ""
APP_UNIT_TEST_RESULT = ""
DOMAIN_UNIT_TEST_RESULT = ""
DATA_UNIT_TEST_RESULT = ""
COREUI_UNIT_TEST_RESULT = ""
FEATURE_SYNC_UNIT_TEST_RESULT = ""
FEATURE_DEVICECENTER_UNIT_TEST_RESULT = ""

APP_COVERAGE = ""
DOMAIN_COVERAGE = ""
DATA_COVERAGE = ""
COVERAGE_ARCHIVE = "coverage.zip"
COVERAGE_FOLDER = "coverage"
ARTIFACTORY_DEVELOP_CODE_COVERAGE = ""

JSON_LINT_REPORT_LINK = ""

NODE_LABELS = 'mac-jenkins-slave-android || mac-jenkins-slave'


/**
 * Folder to contain build outputs, including APK, AAG and symbol files
 */
ARCHIVE_FOLDER = "archive"
NATIVE_SYMBOLS_FILE = "symbols.zip"

/**
 * common.groovy file with common methods
 */
def common

/**
 * Decide whether we should skip the current build. If MR title starts with "Draft:"
 * or "WIP:", then CI pipeline skips all stages in a build. After these 2 tags have
 * been removed from MR title, newly triggered builds will resume to normal.
 *
 * @return true if current stage should be skipped. Otherwise return false.
 */
def shouldSkipBuild() {
    String mrTitle = env.GITLAB_OA_TITLE
    if (mrTitle != null && !mrTitle.isEmpty()) {
        return mrTitle.toLowerCase().startsWith("draft:") ||
                mrTitle.toLowerCase().startsWith("wip:")
    }
    // If title is null, this build is probably triggered by 'jenkins rebuild' comment.
    // In such case, build should not be skipped.
    return false
}

/**
 * Fetch the message of the last commit from environment variable.
 *
 * @return The commit message text if GitLab plugin has sent a valid commit message, which is
 * denoted as a Code Block in Gitlab.
 *
 * Otherwise, return a Bold "N/A" normally when CI build is triggered by MR comment "jenkins rebuild".
 */
String getLastCommitMessage() {
    println("entering getLastCommitMessage()")
    def lastCommitMessage = env.GITLAB_OA_LAST_COMMIT_MESSAGE
    if (lastCommitMessage == null) {
        return '**N/A**'
    } else {
        // use markdown backticks to format commit message into a code block
        return "\n```\n$lastCommitMessage\n```\n".stripIndent().stripMargin()
    }
}

pipeline {
    agent { label NODE_LABELS }
    options {
        // Stop the build early in case of compile or test failures
        skipStagesAfterUnstable()
        buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '1'))
        timeout(time: 1, unit: 'HOURS')
        gitLabConnection('GitLabConnection')
    }
    environment {

        LC_ALL = "en_US.UTF-8"
        LANG = "en_US.UTF-8"

        NDK_ROOT = "/opt/buildtools/android-sdk/ndk/21.3.6528147"
        JAVA_HOME = "/opt/buildtools/zulu17.42.19-ca-jdk17.0.7-macosx"
        ANDROID_HOME = "/opt/buildtools/android-sdk"

        // PATH for necessary commands
        PATH = "/opt/buildtools/android-sdk/cmake/3.22.1/bin:/Applications/MEGAcmd.app/Contents/MacOS:/opt/buildtools/zulu17.42.19-ca-jdk17.0.7-macosx/bin:/opt/brew/bin:/opt/brew/opt/gnu-sed/libexec/gnubin:/opt/brew/opt/gnu-tar/libexec/gnubin:/opt/buildtools/android-sdk/platform-tools:/opt/buildtools/android-sdk/build-tools/30.0.3:$PATH"

        // Jenkins build log will be saved in this file.
        CONSOLE_LOG_FILE = "console.txt"

        BUILD_LIB_DOWNLOAD_FOLDER = '${WORKSPACE}/mega_build_download'

        IS_CI_BUILD = 'true'
    }
    post {
        failure {
            script {
                common = load('jenkinsfile/common.groovy')

                if (common.hasGitLabMergeRequest()) {

                    // download Jenkins console log
                    common.downloadJenkinsConsoleLog(CONSOLE_LOG_FILE)

                    // upload Jenkins console log
                    String jsonJenkinsLog = common.uploadFileToGitLab(CONSOLE_LOG_FILE)

                    // upload unit test report if unit test fail
                    String unitTestResult = ""
                    if (BUILD_STEP == "Unit Test and Code Coverage") {
                        if (!APP_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>App Unit Test: ${APP_UNIT_TEST_RESULT}"
                        }
                        if (!DOMAIN_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>Domain Unit Test: ${DOMAIN_UNIT_TEST_RESULT}"
                        }
                        if (!DATA_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>Data Unit Test: ${DATA_UNIT_TEST_RESULT}"
                        }

                        if (!COREUI_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>CoreUi Unit Test: ${COREUI_UNIT_TEST_RESULT}"
                        }

                        if (!FEATURE_SYNC_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>feature/sync Unit Test: ${FEATURE_SYNC_UNIT_TEST_RESULT}"
                        }

                        if (!FEATURE_DEVICECENTER_UNIT_TEST_RESULT.isEmpty()) {
                            unitTestResult += "<br>feature/devicecenter Unit Test: ${FEATURE_DEVICECENTER_UNIT_TEST_RESULT}"
                        }
                    }

                    def failureMessage = ":x: Build Failed(Build: ${env.BUILD_NUMBER})" +
                            "<br/>Failure Stage: ${BUILD_STEP}" +
                            "<br/>Last Commit Message: ${getLastCommitMessage()}" +
                            "Last Commit ID: ${env.GIT_COMMIT}" +
                            "<br/>Build Log: ${jsonJenkinsLog}" +
                            unitTestResult
                    common.sendToMR(failureMessage)
                } else {
                    withCredentials([usernameColonPassword(credentialsId: 'Jenkins-Login', variable: 'CREDENTIALS')]) {
                        def comment = ":x: Android Build failed for branch: ${env.GIT_BRANCH}"
                        if (env.CHANGE_URL) {
                            comment = ":x: Android Build failed for branch: ${env.GIT_BRANCH} \nMR Link:${env.CHANGE_URL}"
                        }
                        slackSend color: "danger", message: comment
                        sh 'curl -u $CREDENTIALS ${BUILD_URL}/consoleText -o console.txt'
                        slackUploadFile filePath: "console.txt", initialComment: "Android Build Log"
                    }
                }
            }
        }
        success {
            script {
                common = load('jenkinsfile/common.groovy')

                if (common.hasGitLabMergeRequest()) {
                    // If CI build is skipped due to Draft status, send a comment to MR
                    if (shouldSkipBuild()) {
                        def skipMessage = ":raising_hand: Android CI Pipeline Build Skipped! <BR/> " +
                                "Newly triggered builds will resume after you have removed <b>Draft:</b> or " +
                                "<b>WIP:</b> from the beginning of MR title."
                        common.sendToMR(skipMessage)
                    } else {
                        // Create the String to be posted as a comment in Gitlab
                        String mergeRequestMessage = ":white_check_mark: Build Succeeded!(Build: ${env.BUILD_NUMBER})\n\n" +
                                "**Last Commit:** (${env.GIT_COMMIT})" + getLastCommitMessage() +
                                "**Build Warnings:**\n" + getBuildWarnings() + "\n\n" +
                                buildLintSummaryTable(JSON_LINT_REPORT_LINK) + "\n\n" +
                                buildCodeComparisonResults()

                        // Send mergeRequestMessage to MR
                        common.sendToMR(mergeRequestMessage)

                        def successSlackMessage = "Android Line Code Coverage:" +
                                "\nCommit:\t${env.GIT_COMMIT}" +
                                "\nBranch:\t${env.GIT_BRANCH}" +
                                "\n- app coverage: $APP_COVERAGE" +
                                "\n- domain coverage: $DOMAIN_COVERAGE" +
                                "\n- data coverage: $DATA_COVERAGE"
                        slackSend color: "good", message: successSlackMessage
                    }
                }
            }
        }
        cleanup {
            // delete whole workspace after each build, to save Jenkins storage
            cleanWs(cleanWhenFailure: true)
        }
    }
    stages {
        stage('Load Common Script') {
            steps {
                script {
                    BUILD_STEP = 'Preparation'

                    common = load('jenkinsfile/common.groovy')
                }
            }
        }
        stage('Preparation') {
            when {
                expression { (!shouldSkipBuild()) }
            }
            steps {
                script {
                    BUILD_STEP = "Preparation"
                }
                gitlabCommitStatus(name: 'Preparation') {
                    script {
                        sh("rm -fv ${CONSOLE_LOG_FILE}")
                        sh("set")
                        sh("rm -fv unit_test_result*.zip")

                        sh("rm -frv $ARCHIVE_FOLDER")
                        sh("mkdir -p ${WORKSPACE}/${ARCHIVE_FOLDER}")
                    }
                }
            }
        }
        stage("Build, Test and Lint") {
            when {
                expression { (!shouldSkipBuild()) }
            }
            parallel {
                stage('Build APK (GMS+QA)') {
                    agent { label NODE_LABELS }
                    when {
                        expression { (!shouldSkipBuild()) }
                    }
                    steps {
                        script {
                            common.downloadDependencyLibForSdk()
                        }
                        gitlabCommitStatus(name: 'Build APK (GMS+QA)') {

                            withCredentials([
                                    string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                                    string(credentialsId: 'ARTIFACTORY_ACCESS_TOKEN', variable: 'ARTIFACTORY_ACCESS_TOKEN'),
                            ]) {
                                withEnv([
                                        "ARTIFACTORY_USER=${ARTIFACTORY_USER}",
                                        "ARTIFACTORY_ACCESS_TOKEN=${ARTIFACTORY_ACCESS_TOKEN}"
                                ]) {
                                    sh "./gradlew app:assembleGmsDebug 2>&1  | tee ${GMS_APK_BUILD_LOG}"
                                    sh "./gradlew app:assembleGmsQa 2>&1  | tee ${QA_APK_BUILD_LOG}"
                                }
                            }

                            sh """
                                if grep -q -m 1 \"^FAILURE: \" ${GMS_APK_BUILD_LOG}; then
                                    echo GMS APK build failed. Exitting....
                                    exit 1
                                fi
                                if grep -q -m 1 \"^FAILURE: \" ${QA_APK_BUILD_LOG}; then
                                    echo QA APK build failed. Exitting....
                                    exit 1
                                fi
                            """
                        }
                    }
                    post {
                        failure {
                            script {
                                BUILD_STEP = "Build APK (GMS+QA)"
                            }
                        }
                        cleanup {
                            cleanWs(cleanWhenFailure: true)
                        }
                    }
                } //stage('Build APK (GMS+QA)')

                stage('Unit Test and Code Coverage') {
                    agent { label NODE_LABELS }
                    when {
                        expression { (!shouldSkipBuild()) }
                    }
                    steps {
                        script {
                            common.downloadDependencyLibForSdk()
                        }
                        gitlabCommitStatus(name: 'Unit Test and Code Coverage') {
                            script {

                                withCredentials([
                                        string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                                        string(credentialsId: 'ARTIFACTORY_ACCESS_TOKEN', variable: 'ARTIFACTORY_ACCESS_TOKEN'),
                                ]) {
                                    withEnv([
                                            "ARTIFACTORY_USER=${ARTIFACTORY_USER}",
                                            "ARTIFACTORY_ACCESS_TOKEN=${ARTIFACTORY_ACCESS_TOKEN}"
                                    ]) {
                                        // domain coverage
                                        try {
                                            sh "./gradlew domain:jacocoTestReport"
                                        } finally {
                                            DOMAIN_UNIT_TEST_RESULT = unitTestArchiveLink("domain/build/reports/tests/test", "domain_unit_test_result.zip")
                                        }
                                        DOMAIN_COVERAGE = "${getTestCoverageSummary("$WORKSPACE/domain/build/reports/jacoco/test/jacocoTestReport.csv")}"
                                        println("DOMAIN_COVERAGE = ${DOMAIN_COVERAGE}")

                                        // data coverage
                                        try {
                                            sh "./gradlew data:testDebugUnitTestCoverage"
                                        } finally {
                                            DATA_UNIT_TEST_RESULT = unitTestArchiveLink("data/build/reports/tests/testDebugUnitTest", "data_unit_test_result.zip")
                                        }
                                        DATA_COVERAGE = "${getTestCoverageSummary("$WORKSPACE/data/build/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.csv")}"
                                        println("DATA_COVERAGE = ${DATA_COVERAGE}")

                                        // run coverage for app module
                                        try {
                                            sh "./gradlew app:createUnitTestCoverageReport"
                                        } finally {
                                            APP_UNIT_TEST_RESULT = unitTestArchiveLink("app/build/reports/tests/testGmsDebugUnitTest", "app_unit_test_result.zip")
                                        }
                                        APP_COVERAGE = "${getTestCoverageSummary("$WORKSPACE/app/build/reports/jacoco/gmsDebugUnitTestCoverage.csv")}"
                                        println("APP_COVERAGE = ${APP_COVERAGE}")

                                        try {
                                            sh "./gradlew feature:devicecenter:testDebugUnitTest"
                                        } finally {
                                            FEATURE_DEVICECENTER_UNIT_TEST_RESULT = unitTestArchiveLink("feature/devicecenter/build/reports/tests/testDebugUnitTest", "feature_devicecenter_unit_test_result.zip")
                                        }

                                        try {
                                            sh "./gradlew feature:sync:testDebugUnitTest"
                                        } finally {
                                            FEATURE_SYNC_UNIT_TEST_RESULT = unitTestArchiveLink("feature/sync/build/reports/tests/testDebugUnitTest", "feature_sync_unit_test_result.zip")
                                        }

                                        try {
                                            sh "./gradlew shared:original-core-ui:testDebugUnitTest"
                                        } finally {
                                            COREUI_UNIT_TEST_RESULT = unitTestArchiveLink("shared/original-core-ui/build/reports/tests/testDebugUnitTest", "shared_original_core_ui_unit_test_result.zip")
                                        }

                                        // below code is only run when UnitTest is OK, before test reports are cleaned up.
                                        // If UnitTest is failed, summary is collected at post.failure{} phase
                                        // We have to collect the report here, before they are cleaned in the last stage.
                                        APP_UNIT_TEST_SUMMARY = unitTestSummary("${WORKSPACE}/app/build/test-results/testGmsDebugUnitTest")
                                        DOMAIN_UNIT_TEST_SUMMARY = unitTestSummary("${WORKSPACE}/domain/build/test-results/test")
                                        DATA_UNIT_TEST_SUMMARY = unitTestSummary("${WORKSPACE}/data/build/test-results/testDebugUnitTest")

                                        // Compare Coverage
                                        String developCoverageLocation = "${env.ARTIFACTORY_BASE_URL}/artifactory/android-mega/cicd/coverage/coverage_summary.txt"

                                        // Navigate to the "/cicd/coverage/" path
                                        // Download the code coverage from Artifactory.
                                        // Afterwards, rename the downloaded code coverage text file to "develop_coverage_summary.txt"
                                        sh """
                                            cd ${WORKSPACE}
                                            rm -frv cicd
                                            mkdir -pv ${WORKSPACE}/cicd/coverage
                                            cd ${WORKSPACE}/cicd/coverage
                                            curl -u ${ARTIFACTORY_USER}:${ARTIFACTORY_ACCESS_TOKEN} -o develop_coverage_summary.txt ${developCoverageLocation}
                                            ls
                                        """

                                        // Once the file has been downloaded, call the script to parse the Code Coverage results
                                        ARTIFACTORY_DEVELOP_CODE_COVERAGE = "${getArtifactoryDevelopCodeCoverage("$WORKSPACE/cicd/coverage/develop_coverage_summary.txt")}"
                                        println("ARTIFACTORY_DEVELOP_CODE_COVERAGE from Groovy: ${ARTIFACTORY_DEVELOP_CODE_COVERAGE}")
                                    }
                                }


                            }
                        }
                    }
                    post {
                        failure {
                            script {
                                BUILD_STEP = "Unit Test and Code Coverage"
                            }
                        }
                        cleanup {
                            cleanWs(cleanWhenFailure: true)
                        }
                    }
                } //stage('Unit Test and Code Coverage')

                stage('Lint Check') {
                    agent { label NODE_LABELS }
                    when {
                        expression { (!shouldSkipBuild()) }
                    }
                    steps {
                        // Run Lint and analyse the results
                        script {
                            common.downloadDependencyLibForSdk()
                        }

                        gitlabCommitStatus(name: 'Lint Check') {

                            withCredentials([
                                    string(credentialsId: 'ARTIFACTORY_USER', variable: 'ARTIFACTORY_USER'),
                                    string(credentialsId: 'ARTIFACTORY_ACCESS_TOKEN', variable: 'ARTIFACTORY_ACCESS_TOKEN'),
                            ]) {
                                withEnv([
                                        "ARTIFACTORY_USER=${ARTIFACTORY_USER}",
                                        "ARTIFACTORY_ACCESS_TOKEN=${ARTIFACTORY_ACCESS_TOKEN}"
                                ]) {
                                    sh "mv custom_lint.xml lint.xml"
                                    sh "./gradlew lint"
                                }
                            }

                            script {
                                MODULE_LIST.each { module ->
                                    LINT_REPORT_SUMMARY_MAP.put(module, lintSummary(module))
                                }
                                archiveLintReports()

                                JSON_LINT_REPORT_LINK = common.uploadFileToGitLab(LINT_REPORT_ARCHIVE)
                            }
                        }
                    }
                    post {
                        failure {
                            script {
                                BUILD_STEP = "Lint Check"
                            }
                        }
                        cleanup {
                            cleanWs(cleanWhenFailure: true)
                        }
                    }
                }  //stage('Lint Check')
            }
        }
    }
}

/**
 * Returns a Markdown table-formatted String that holds all the Lint Results for available modules
 *
 * @param jsonLintReportLink A String that contains a link to all Lint Results
 *
 * @return a Markdown table-formatted String
 */
String buildLintSummaryTable(String jsonLintReportLink) {
    println("Entering buildLintSummaryTable()")
    // Declare a JsonSlurperClassic object
    def jsonSlurperClassic = new JsonSlurperClassic()

    // Declare the initial value for the Table String
    String tableStr = "| Module | Fatal | Error | Warning | Information | Error Message |\n" +
            "| :---: | :---: | :---: | :---: | :---: | :---: |\n"

    int fatalCount = 0
    int errorCount = 0
    int warningCount = 0
    // Iterate through all the values in LINT_REPORT_SUMMARY_MAP and add a row per module
    // The standard method of iterating a map returns an error when used with a Jenkins pipeline,
    // which is why the map iteration is set up in this manner
    for (def key in LINT_REPORT_SUMMARY_MAP.keySet()) {
        // Parse the JSON String received from lint_report.py into a Map
        def jsonObject = jsonSlurperClassic.parseText(LINT_REPORT_SUMMARY_MAP[key])

        // Add a new row to the table
        tableStr += "| **$key** " +
                "| $jsonObject.fatalCount " +
                "| $jsonObject.errorCount " +
                "| $jsonObject.warningCount " +
                "| $jsonObject.informationCount " +
                "| $jsonObject.errorMessage |\n"
        fatalCount += jsonObject.fatalCount as int
        errorCount += jsonObject.errorCount as int
        warningCount += jsonObject.warningCount as int
    }

    // Create Summary to be returned after iterating through all modules
    String lintSummary = "<details><summary><b>Lint Summary:</b> Fatal(${fatalCount}) Error(${errorCount}) Warning(${warningCount})</summary>" + "\n ${jsonLintReportLink} \n\n" + tableStr + "</details>"

    // Return the final result
    lintSummary
}

/**
 * Compares the Code Coverage results of all available modules between the source branch and
 * the latest develop branch from Artifactory
 *
 * @return A Markdown-formatted table String that contains the Code Coverage results of all
 * available modules between the source branch and the latest develop branch from Artifactory
 */
String buildCodeComparisonResults() {
    println("Entering buildCodeComparisonResults()")
    def latestDevelopResults = new JsonSlurperClassic().parseText(ARTIFACTORY_DEVELOP_CODE_COVERAGE)

    String codeComparisonSummary = "<details><summary><b>Code Coverage and Comparison:</b>"

    String tableString = "\n\n".concat("| Module | Test Cases | Coverage | Coverage Change |").concat("\n")
            .concat("|:---|:---|:---|:---|").concat("\n")

    def currentModuleTestCases = []
    def currentModuleCoverage = ""
    def currentModuleTestResultsLink = ""
    def isCoverageReduced = false

    for (def latestDevelopModuleResults in latestDevelopResults) {
        // Compare the name from moduleResult with a static module name
        switch (latestDevelopModuleResults.name) {
            case "**app**":
                currentModuleTestCases = APP_UNIT_TEST_SUMMARY.split(',')
                currentModuleCoverage = APP_COVERAGE
                currentModuleTestResultsLink = APP_UNIT_TEST_RESULT
                break
            case "**domain**":
                currentModuleTestCases = DOMAIN_UNIT_TEST_SUMMARY.split(',')
                currentModuleCoverage = DOMAIN_COVERAGE
                currentModuleTestResultsLink = DOMAIN_UNIT_TEST_RESULT
                break
            case "**data**":
                currentModuleTestCases = DATA_UNIT_TEST_SUMMARY.split(',')
                currentModuleCoverage = DATA_COVERAGE
                currentModuleTestResultsLink = DATA_UNIT_TEST_RESULT
                break
        }
        println("Current Coverage of ${latestDevelopModuleResults.name}: $currentModuleCoverage")

        // Build the Columns
        String testCasesColumn = buildTestCasesColumn(currentModuleTestCases, currentModuleTestResultsLink, latestDevelopModuleResults)
        String coverageColumn = buildCoverageColumn(currentModuleCoverage, latestDevelopModuleResults)

        def currentModuleLinePercentage = currentModuleCoverage.split("%")[0]
        def currentModuleBigDecimal = new BigDecimal(currentModuleLinePercentage).setScale(2, RoundingMode.HALF_UP)

        def latestDevelopTotalLines = Float.parseFloat(latestDevelopModuleResults.totalLines)
        def latestDevelopCoveredLines = Float.parseFloat(latestDevelopModuleResults.coveredLines)
        def latestDevelopLinePercentage = (latestDevelopCoveredLines / latestDevelopTotalLines) * 100
        def latestDevelopBigDecimal = new BigDecimal(latestDevelopLinePercentage).setScale(2, RoundingMode.HALF_UP)

        if (currentModuleBigDecimal < latestDevelopBigDecimal) {
            isCoverageReduced = true
        }

        String coverageChangeColumn = buildCoverageChangeColumn(currentModuleBigDecimal, latestDevelopBigDecimal)

        // Add a Column for every item in the list
        tableString = tableString.concat("| ${latestDevelopModuleResults.name} | $testCasesColumn | $coverageColumn | $coverageChangeColumn |").concat("\n")

        //Build a summary for every item in the list and add it to header
        codeComparisonSummary += " ${latestDevelopModuleResults.name.replaceAll('\\*', '')}(${coverageChangeColumn.replaceAll('\\*', '')})"
    }

    def result = ""
    if (isCoverageReduced) {
        result = ":warning: This MR has caused a reduction in the test coverage. Please add some unit tests.\n\n".concat(codeComparisonSummary).concat("</b></summary>").concat(tableString).concat("</details>")
    } else {
        result = codeComparisonSummary.concat("</b></summary>").concat(tableString).concat("</details>")
    }

    return result
}

/**
 * Builds a Markdown-formatted String of the test cases of a specific module from the current
 * branch and the latest develop branch in Artifactory
 *
 * @param currentModuleTestCases The Module results of the current branch
 * @param currentModuleTestResultsLink The link to the Module test results of the current branch
 * @param latestDevelopModuleTestCases The Module results of the latest develop branch in Artifactory
 *
 * @return A String that serves as an entry for the "Test Cases" column
 */
String buildTestCasesColumn(def currentModuleTestCases,
                            def currentModuleTestResultsLink,
                            def latestDevelopModuleTestCases
) {
    println("Entering buildTestCasesColumn")
    println("currentModuleTestCases = $currentModuleTestCases")
    println("currentModuleTestResultsLink = $currentModuleTestResultsLink")
    println("latestDevelopModuleTestCases = $latestDevelopModuleTestCases")

    // Build the "Current Branch" Column first
    String currentBranchColumn = "**Current Branch:**".concat("<br><br>")

    if (currentModuleTestCases[0].toInteger() > 0) {
        currentBranchColumn = currentBranchColumn.concat("_Total Cases:_ ")
                .concat("**${currentModuleTestCases[0]}**").concat("<br>")
    }
    if (currentModuleTestCases[1].toInteger() > 0) {
        currentBranchColumn = currentBranchColumn.concat("_Skipped Cases:_ ")
                .concat("**${currentModuleTestCases[1]}**").concat("<br>")
    }
    if (currentModuleTestCases[2].toInteger() > 0) {
        currentBranchColumn = currentBranchColumn.concat("_Error Cases:_ ")
                .concat("**${currentModuleTestCases[2]}**").concat("<br>")
    }
    if (currentModuleTestCases[3].toInteger() > 0) {
        currentBranchColumn = currentBranchColumn.concat("_Failed Cases:_ ")
                .concat("**${currentModuleTestCases[3]}**").concat("<br>")
    }
    currentBranchColumn = currentBranchColumn.concat("_Duration (s):_ ")
            .concat("**${currentModuleTestCases[4]}**").concat("<br>")
    currentBranchColumn = currentBranchColumn.concat("_Test Report Link:_")
            .concat("<br>").concat(currentModuleTestResultsLink).concat("<br><br>")


    // Afterwards, build the "Latest develop Branch" Column
    String latestDevelopColumn = "**Latest develop Branch:**".concat("<br><br>")

    if (latestDevelopModuleTestCases.totalTestCases.toInteger() > 0) {
        latestDevelopColumn = latestDevelopColumn.concat("_Total Cases:_ ")
                .concat("**${latestDevelopModuleTestCases.totalTestCases}**").concat("<br>")
    }
    if (latestDevelopModuleTestCases.skippedTestCases.toInteger() > 0) {
        latestDevelopColumn = latestDevelopColumn.concat("_Skipped Cases:_ ")
                .concat("**${latestDevelopModuleTestCases.skippedTestCases}**").concat("<br>")
    }
    if (latestDevelopModuleTestCases.errorTestCases.toInteger() > 0) {
        latestDevelopColumn = latestDevelopColumn.concat("_Error Cases:_ ")
                .concat("**${latestDevelopModuleTestCases.errorTestCases}**").concat("<br>")
    }
    if (latestDevelopModuleTestCases.failedTestCases.toInteger() > 0) {
        latestDevelopColumn = latestDevelopColumn.concat("_Failed Cases:_ ")
                .concat("**${latestDevelopModuleTestCases.failedTestCases}**").concat("<br>")
    }
    latestDevelopColumn = latestDevelopColumn.concat("_Duration (s):_ ")
            .concat("**${latestDevelopModuleTestCases.duration}**")

    currentBranchColumn.concat(latestDevelopColumn)
}

/**
 * Builds a Markdown-formatted String of the coverage of a specific module from the current
 * branch and the latest develop branch in Artifactory
 *
 * @param currentModuleCoverage The Module results of the current branch
 * @param latestDevelopModuleCoverage The Module results of the latest develop branch in Artifactory
 *
 * @return A String that serves as an entry for the "Coverage" column
 */
String buildCoverageColumn(def currentModuleCoverage, def latestDevelopModuleCoverage) {
    def df = new DecimalFormat("0.00")

    // Build the "Current Branch" Column first
    String currentBranchColumn = "**Current Branch:**".concat("<br><br>")

    def currentInitialArray = currentModuleCoverage.split('=')
    def currentLineArray = currentInitialArray[1].split('/')

    currentBranchColumn = currentBranchColumn.concat("_Total Lines:_ ")
            .concat("**${currentLineArray[1]}**").concat("<br>")
    currentBranchColumn = currentBranchColumn.concat("_Covered Lines:_ ")
            .concat("**${currentLineArray[0]}**").concat("<br>")
    currentBranchColumn = currentBranchColumn.concat("_Percentage Covered_: ")
            .concat("**${currentInitialArray[0]}**").concat("<br><br>")

    // Afterwards, build the "Latest develop Branch" Column
    String latestDevelopColumn = "**Latest develop Branch:**".concat("<br><br>")

    def latestDevelopTotalLines = Float.parseFloat(latestDevelopModuleCoverage.totalLines)
    def latestDevelopCoveredLines = Float.parseFloat(latestDevelopModuleCoverage.coveredLines)
    def latestDevelopLinePercentage = df.format((latestDevelopCoveredLines / latestDevelopTotalLines) * 100)

    latestDevelopColumn = latestDevelopColumn.concat("_Total Lines:_ ")
            .concat("**${latestDevelopModuleCoverage.totalLines}**").concat("<br>")
    latestDevelopColumn = latestDevelopColumn.concat("_Covered Lines:_ ")
            .concat("**${latestDevelopModuleCoverage.coveredLines}**").concat("<br>")
    latestDevelopColumn = latestDevelopColumn.concat("_Percentage Covered_: ")
            .concat("**$latestDevelopLinePercentage%**")

    return currentBranchColumn.concat(latestDevelopColumn)
}

/**
 * Builds a Markdown-formatted String of the coverage change of a specific module from the current
 * branch and the latest develop branch in Artifactory
 *
 * @param currentModuleCoverage The Module results of the current branch
 * @param latestDevelopModuleResults The Module results of the latest develop branch in Artifactory
 *
 * @return A String that serves as an entry for the "Coverage Change" column
 */
String buildCoverageChangeColumn(def currentModuleBigDecimal, def latestDevelopBigDecimal) {
    def result = currentModuleBigDecimal - latestDevelopBigDecimal

    if (result > 0) {
        return "**+$result%**"
    } else if (result < 0) {
        return "**$result%**"
    } else {
        return "**No Change**"
    }
}

/**
 * Combines the GMS and QA Build Warnings into one String
 *
 * @return A String that contains some or all Build Warnings combined together.
 * If there are no Build Warnings, return "None".
 */
String getBuildWarnings() {
    println("Entering getBuildWarnings()")
    String result = ""
    if (fileExists(GMS_APK_BUILD_LOG)) {
        String gmsBuildWarnings = sh(script: "cat ${GMS_APK_BUILD_LOG} | grep -a '^w:' || true", returnStdout: true).trim()
        println("gmsBuildWarnings = $gmsBuildWarnings")
        if (!gmsBuildWarnings.isEmpty()) {
            result = "<details><summary>:warning: GMS Build Warnings :warning:</summary>" + wrapBuildWarnings(gmsBuildWarnings) + "</details>"
        }
    }

    if (fileExists(QA_APK_BUILD_LOG)) {
        String qaBuildWarnings = sh(script: "cat ${QA_APK_BUILD_LOG} | grep -a '^w:' || true", returnStdout: true).trim()
        println("qaGmsBuildWarnings = $qaBuildWarnings")
        if (!qaBuildWarnings.isEmpty()) {
            result += "<details><summary>:warning: QA GMS Build Warnings :warning:</summary>" + wrapBuildWarnings(qaBuildWarnings) + "</details>"
        }
    }

    if (result == "") result = "None"
    println("readBuildWarnings() = ${result}")
    return result
}

String wrapBuildWarnings(String rawWarning) {
    if (rawWarning == null || rawWarning.isEmpty()) {
        return ""
    } else {
        return rawWarning.split('\n').join("<br/>")
    }
}

/**
 * Analyse unit test report and get the summary string
 * @param testReportPath path of the unit test report in xml format
 * @return summary string of unit test
 */
String unitTestSummary(String testReportPath) {
    return sh(
            script: "python3 ${WORKSPACE}/jenkinsfile/junit_report.py ${testReportPath}",
            returnStdout: true).trim()
}

/**
 * Executes lint_report.py in order to parse the Lint Results and create a Lint Summary
 *
 * @param module The name of the module (e.g. app, domain, sdk)
 * @return The Lint Summary Report of the given module from lint_report.py. Here's a sample return value:
 *
 * {"fatalCount": 10, "errorCount": 20, "warningCount": 30, "informationCount": 40, "errorMessage": ""}
 */
String lintSummary(String module) {
    summary = sh(
            script: "python3 ${WORKSPACE}/jenkinsfile/lint_report.py $WORKSPACE/${module}/build/reports/lint-results.xml",
            returnStdout: true).trim()
    // If summary is empty, return a String with 0 counts and a "No lint results found" error message
    if (!summary) summary = '{"fatalCount": "0", "errorCount": "0", "warningCount": "0", "informationCount": "0", "errorMessage": "No lint results found"}'
    print("lintSummary($module) = $summary")
    return summary
}

/**
 * Archive all HTML lint reports into a zip file.
 */
def archiveLintReports() {
    sh """
        cd ${WORKSPACE}
        rm -frv ${LINT_REPORT_FOLDER}
        mkdir -pv ${LINT_REPORT_FOLDER}
        rm -fv ${LINT_REPORT_ARCHIVE}
    """

    MODULE_LIST.eachWithIndex { module, _ ->
        sh("cp -fv ${module}/build/reports/lint*.html ${WORKSPACE}/${LINT_REPORT_FOLDER}/${module.replace('/', '_')}_lint_report.html")
    }

    sh """
        cd ${WORKSPACE}
        zip -r ${LINT_REPORT_ARCHIVE} ${LINT_REPORT_FOLDER}/*.html
    """
}

/**
 * Archive all HTML coverage reports into one zip file
 */
def archiveCoverageReport() {
    sh """
        cd ${WORKSPACE}
        rm -frv ${COVERAGE_FOLDER}
        mkdir -pv ${COVERAGE_FOLDER}/app
        mkdir -pv ${COVERAGE_FOLDER}/domain
        mv -v ${WORKSPACE}/domain/build/coverage-report/* $WORKSPACE/$COVERAGE_FOLDER/domain/
        mv -v ${WORKSPACE}/app/build/reports/jacoco/html/* $WORKSPACE/$COVERAGE_FOLDER/app/
        
        zip -r ${COVERAGE_ARCHIVE} $WORKSPACE/$COVERAGE_FOLDER/*
        ls -l ${COVERAGE_ARCHIVE}
    """
}

/**
 *
 * @param reportPath relative path of the test report folder,
 *                  for example: "app/build/reports" or "domain/build/reports"
 *
 * @param targetFileName target archive file name
 * @return true if test report files are available. Otherwise return false.
 */
def archiveUnitTestReport(String reportPath, String targetFileName) {
    sh("rm -f ${WORKSPACE}/${targetFileName}")
    if (fileExists(WORKSPACE + "/" + reportPath)) {
        sh """
            cd ${WORKSPACE}
            cd ${reportPath}
            zip -r ${targetFileName} * 
            cd ${WORKSPACE}
            cp ${reportPath}/${targetFileName} ${targetFileName}
        """
        return true
    } else {
        return false
    }
}

/**
 * Get the link of the HTML test report.
 *
 * @param reportPath relative path to the HTML format test report
 * @param archiveTargetName file name of the test report zip file
 */
def unitTestArchiveLink(String reportPath, String archiveTargetName) {
    String result
    if (archiveUnitTestReport(reportPath, archiveTargetName)) {
        common = load('jenkinsfile/common.groovy')
        unitTestFileLink = common.uploadFileToGitLab(archiveTargetName)
        result = "${unitTestFileLink}"
    } else {
        result = "Unit Test report not available, perhaps test code has compilation error. Please check full build log."
    }
    return result
}

/**
 * Reads and calculates the Test Coverage by a given csv format report
 * @param csvReportPath path to the csv coverage file, generated by JaCoCo
 * @return a String containing the Test Coverage report
 */
String getTestCoverageSummary(String csvReportPath) {
    summary = sh(
            script: "python3 ${WORKSPACE}/jenkinsfile/coverage_report.py ${csvReportPath}",
            returnStdout: true).trim()
    print("coverage path(${csvReportPath}): ${summary}")
    return summary
}

/**
 * Parses the file that contains the Code Coverage results of the latest develop branch from Artifactory
 * into a formatted String, so that the results can be easily transformed and displayed in the Gitlab MR
 * @param coveragePath The full path to the file containing the Code Coverage from the latest develop
 * @return A formatted String of the Code Coverage
 */
String getArtifactoryDevelopCodeCoverage(String coveragePath) {
    summary = sh(
            script: "python3 ${WORKSPACE}/jenkinsfile/artifactory_develop_code_coverage.py ${coveragePath}",
            returnStdout: true).trim()
    print("artifactory develop coverage path(${coveragePath}): ${summary}")
    return summary
}

/**
 * check if a certain value is defined by checking the tag value
 * @param value value of tag
 * @return true if tag has a value. false if tag is null or zero length
 */
static boolean isDefined(String value) {
    return value != null && !value.isEmpty()
}
