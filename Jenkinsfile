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
                /*withAnt(installation: 'myinstall') {
                    sh "ant build"
                }*/
                script {
                    sh '''
                        mkdir moje
                        echo "zkouska" > moje/neco.txt
                    '''                    
                    zip zipFile: 'release.zip', archive: true, dir: 'moje', overwrite: true
                }                
            }
        }
        stage('Test') {
            steps {
                /*withAnt(installation: 'myinstall') {
                    sh "ant test"
                }
                script {
                    zip zipFile: 'reports.zip', archive: false, dir: 'reports'
                    archiveArtifacts artifacts: 'reports.zip', fingerprint: true
                }*/
                echo "TEST"
            }
        }
        stage('Javadoc') {
            steps {
                /*withAnt(installation: 'myinstall') {
                    sh "ant javadoc"
                }
                script {
                    zip zipFile: 'javadoc.zip', archive: false, dir: 'javadoc'
                    archiveArtifacts artifacts: 'javadoc.zip', fingerprint: true
                }*/
                echo "JAVADOC"
            }
        }
    }
}
