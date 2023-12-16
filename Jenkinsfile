pipeline {
    agent any

    environment {
          COMMIT_HASH = sh(script: 'git rev-parse --short HEAD', returnStdout: true)
    }

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
                    zip zipFile: 'release${COMMIT_HASH}.zip', archive: true, dir: 'dist', overwrite: true
                }                
            }
        }
        stage('Test') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant test"
                }
                script {
                    zip zipFile: 'reports${COMMIT_HASH}.zip', archive: true, dir: 'reports', overwrite: true
                }
            }
        }
        stage('Javadoc') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant javadoc"
                }
                script {
                    zip zipFile: 'javadoc${COMMIT_HASH}.zip', archive: true, dir: 'javadoc', overwrite: true
                }                
            }
        }
    }
}
