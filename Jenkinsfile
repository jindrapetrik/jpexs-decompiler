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
                script {
                    zip zipFile: 'release.zip', archive: false, dir: 'dist'
                    archiveArtifacts artifacts: 'release.zip', fingerprint: true
                }
            }
        }
        stage('Test') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant test"
                }
                script {
                    zip zipFile: 'reports.zip', archive: false, dir: 'reports'
                    archiveArtifacts artifacts: 'reports.zip', fingerprint: true
                }
            }
        }
        stage('Javadoc') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant javadoc"
                }
                script {
                    zip zipFile: 'javadoc.zip', archive: false, dir: 'javadoc'
                    archiveArtifacts artifacts: 'javadoc.zip', fingerprint: true
                }
            }
        }
    }
}
