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
                sh "ant build"
            }
        }
        stage('Test') {
            steps {
                sh "ant test"
            }
        }
        stage('Javadoc') {
            steps {
                sh "ant javadoc"
            }
        }
    }
}
