pipeline {
    agent any

    stages {
        stage('Clean') {
            steps {
                sh '''
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
            }
        }
        stage('Test') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant test"
                }
            }
        }
        stage('Javadoc') {
            steps {
                withAnt(installation: 'myinstall') {
                    sh "ant javadoc"
                }
            }
        }
    }
}
