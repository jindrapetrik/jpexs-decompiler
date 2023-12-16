pipeline {
    agent any

    stages {
        stage('Clean') {
            steps {
                sh '''
                    rm -rf dist
                    rm -rf build
                    rm -rf javadoc
                    rm -rf reports
                '''
            }
        }
        stage('Build') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant build"
                }
                zip zipFile: 'release.zip', dir: 'dist'
                archiveArtifacts artifacts: 'release.zip', fingerprint: true
            }
        }
        stage('Test') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant test"
                }
                zip zipFile: 'reports.zip', dir: 'reports'
                archiveArtifacts artifacts: 'reports.zip', fingerprint: true
            }
        }
        stage('Javadoc') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant javadoc"
                }
                zip zipFile: 'javadoc.zip', dir: 'javadoc'
                archiveArtifacts artifacts: 'javadoc.zip', fingerprint: true
            }
        }
    }
}
