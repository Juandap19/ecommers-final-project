@Library('jenkins-microservices-library@main') _

pipeline {
    agent any
     tools {
        jdk 'jdk-17' // Use the name you defined in the Jenkins Tools section
    }
    parameters {
        choice(
            name: 'MICROSERVICE',
            choices: [
                'ALL',
                'api-gateway',
                'service-discovery',
                'cloud-config', 
                'user-service',
                'product-service',
                'order-service',
                'payment-service',
                'shipping-service',
                'favourite-service',
                'proxy-client'
            ],
            description: 'Seleccionar microservicio específico o ALL para todos'
        )
        booleanParam(
            name: 'SKIP_SECURITY_SCAN',
            defaultValue: false,
            description: 'Saltar escaneo de seguridad con Trivy'
        )
    }
    
    environment {
        // Credenciales y configuración
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        SONAR_HOST_URL = 'http://sonarqube:9000'
        
        // Configuración de Java
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        
        // Configuración de la aplicación
        APP_NAME = 'ecommerce-microservice-backend'
        
        // Docker Hub
        DOCKERHUB_USERNAME = 'j2loop'
        DOCKERHUB_CREDENTIALS_ID = 'DOCKERHUB_CREDENTIALS'
        
        // GitHub
        GITHUB_TOKEN = credentials('GITHUB_TOKEN')
        
        // Notificaciones
        EMAIL_RECIPIENTS = 'juanjolo1204lo@gmail.com'
        
        // Variables dinámicas
        SEMANTIC_VERSION = ''
        IS_PRODUCTION_DEPLOY = 'false'
        SERVICES_TO_BUILD = ''
        LOCAL_IMAGES = ''
        BUILT_IMAGES = ''
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Obteniendo código fuente...'
                checkout scm
            }
        }
        
        stage('Compile') {
            steps {
                script {
                    sh 'chmod +x mvnw' 
                    buildStages.compileProject()
                }
            }
        }
        
        stage('Calculate Version') {
            steps {
                script {
                    env.SEMANTIC_VERSION = versioningStages.calculateSemanticVersion()
                }
            }
        }
        
        stage('Detect Services') {
            steps {
                script {
                    echo "🔍 Inicializando detección de servicios..."
                    def servicesToBuildList = []

                    try {
                        def detectedServices = buildStages.detectServicesToBuild(params)
                        echo "✅ Biblioteca compartió: ${detectedServices}"
                        if (detectedServices instanceof List) {
                            servicesToBuildList = detectedServices.collect { it.toString() }.findAll { !it.isEmpty() }
                        } else if (detectedServices) {
                            servicesToBuildList = [detectedServices.toString()]
                        }
                        
                        // Fix service name mapping issues
                        servicesToBuildList = servicesToBuildList.collect { service ->
                            if (service == 'proxy-service') {
                                return 'proxy-client'
                            }
                            return service
                        }
                    } catch (e) {
                        echo "⚠️ Error llamando a la biblioteca: ${e.message}. Usando fallback."
                    }
                    
                    if (servicesToBuildList.isEmpty()) {
                        echo "🔧 Lógica de emergencia activada..."
                        def possibleServices = [
                            'api-gateway', 'service-discovery', 'cloud-config',
                            'user-service', 'product-service', 'order-service',
                            'payment-service', 'shipping-service', 'favourite-service',
                            'proxy-client'
                        ]
                        servicesToBuildList = possibleServices.findAll { service -> fileExists("${service}/pom.xml") }
                    }
                    
                    if (servicesToBuildList.isEmpty()) {
                        echo "🚨 Fallback absoluto: No se pudo detectar ningún servicio."
                        servicesToBuildList = ['user-service', 'product-service']
                        currentBuild.result = 'UNSTABLE'
                    }

                    def servicesString = servicesToBuildList.join(',')
                    
                    // --- LA SOLUCIÓN DEFINITIVA ---
                    // Escribimos el resultado en un archivo en el workspace.
                    writeFile file: 'services_to_build.txt', text: servicesString
                    
                    echo "✅ Servicios detectados y guardados en 'services_to_build.txt': ${servicesString}"
                    
                    // Usamos 'stash' para asegurar que el archivo esté disponible para las siguientes etapas,
                    // incluso si se ejecutan en diferentes agentes.
                    stash name: 'build-info', includes: 'services_to_build.txt'
                    
                    // Lógica adicional del stage
                    if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                        env.IS_PRODUCTION_DEPLOY = 'true'
                        echo "🚀 Despliegue a PRODUCCIÓN detectado"
                    }
                }
            }
        }
        
        stage('Tests') {
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    def servicesToBuild = servicesString.split(',')
                    if (servicesToBuild.size() > 0) {
                        echo "🧪 Ejecutando tests para servicios: ${servicesToBuild.join(', ')}"
                        try {
                            // Run tests for each service directly
                            servicesToBuild.each { service ->
                                echo "🧪 Ejecutando tests para ${service}..."
                                try {
                                    sh """
                                        echo "Ejecutando tests para ${service}..."
                                        ./mvnw test -pl ${service} -am
                                        echo "✅ Tests completados para ${service}"
                                    """
                                } catch (Exception serviceError) {
                                    echo "⚠️ Error en tests de ${service}: ${serviceError.getMessage()}"
                                    currentBuild.result = 'UNSTABLE'
                                }
                            }
                            echo "✅ Todos los tests completados"
                        } catch (Exception e) {
                            echo "⚠️ Error ejecutando tests: ${e.getMessage()}"
                            echo "Continuando pipeline con tests fallidos"
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No hay servicios para probar"
                    }
                }
            }
            post {
                always {
                    script {
                        try {
                            // Publish test results for all services
                            unstash 'build-info'
                            def servicesString = readFile('services_to_build.txt').trim()
                            def servicesToBuild = servicesString.split(',')
                            
                            servicesToBuild.each { service ->
                                // Publish unit test results
                                if (fileExists("${service}/target/surefire-reports/*.xml")) {
                                    junit "${service}/target/surefire-reports/*.xml"
                                }
                                // Publish integration test results  
                                if (fileExists("${service}/target/failsafe-reports/*.xml")) {
                                    junit "${service}/target/failsafe-reports/*.xml"
                                }
                            }
                        } catch (Exception e) {
                            echo "⚠️ Error publicando resultados de tests: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    def servicesToBuild = servicesString.split(',')
                    if (servicesToBuild.size() > 0) {
                        echo "📦 Empaquetando microservicios usando lógica local..."
                        try {
                            // Direct packaging without shared library call
                            servicesToBuild.each { service ->
                                try {
                                    echo "📦 Empaquetando ${service}..."
                                    
                                    if (fileExists("${service}/pom.xml")) {
                                        sh """
                                            cd ${service}
                                            ../mvnw clean package -DskipTests
                                            echo "✅ JAR generado para ${service}"
                                            ls -la target/*.jar || echo "⚠️  No se encontró JAR en target/"
                                        """
                                    } else {
                                        sh """
                                            ./mvnw clean package -pl ${service} -am -DskipTests
                                            echo "✅ JAR generado para ${service}"
                                            ls -la ${service}/target/*.jar || echo "⚠️  No se encontró JAR en ${service}/target/"
                                        """
                                    }
                                } catch (Exception localError) {
                                    echo "❌ Error empaquetando ${service}: ${localError.getMessage()}"
                                }
                            }
                        } catch (Exception e) {
                            echo "⚠️ Error empaquetando microservicios: ${e.getMessage()}"
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No hay servicios para empaquetar"
                    }
                }
            }
        }
        
        stage('Build Docker') {
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    def servicesToBuild = servicesString.split(',')
                    if (servicesToBuild.size() > 0) {
                        echo "🐳 Construyendo imágenes Docker para servicios: ${servicesToBuild.join(', ')}"
                        
                        // Build Docker images locally with validation
                        def localImages = []
                        def builtImages = []
                        
                        servicesToBuild.each { service ->
                            try {
                                if (fileExists("${service}/Dockerfile")) {
                                    // Verificar que el JAR existe
                                    def jarExists = sh(
                                        script: "ls ${service}/target/*.jar 2>/dev/null | wc -l",
                                        returnStdout: true
                                    ).trim()
                                    
                                    if (jarExists == "0") {
                                        echo "❌ No se encontró JAR para ${service}. Saltando construcción Docker."
                                    } else {
                                        echo "🐳 Construyendo imagen Docker para ${service}..."
                                        def imageName = "${env.DOCKERHUB_USERNAME}/${service}:${env.BUILD_NUMBER}"
                                        sh "docker build -t ${imageName} ${service}/"
                                        localImages.add(imageName)
                                        echo "✅ Imagen construida: ${imageName}"
                                    }
                                } else {
                                    echo "⚠️ No se encontró Dockerfile en ${service}/"
                                }
                            } catch (Exception dockerError) {
                                echo "❌ Error construyendo Docker para ${service}: ${dockerError.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                        
                        env.LOCAL_IMAGES = localImages.join(',')
                        env.BUILT_IMAGES = localImages.join(',')
                        
                        // Save images list to file for other stages
                        writeFile file: 'local_images.txt', text: localImages.join(',')
                        stash name: 'docker-images', includes: 'local_images.txt'
                        
                        if (localImages.isEmpty()) {
                            echo "⚠️ No se construyeron imágenes Docker"
                            currentBuild.result = 'UNSTABLE'
                        } else {
                            echo "✅ Imágenes Docker construidas: ${localImages.join(', ')}"
                            echo "🐛 DEBUG: LOCAL_IMAGES env var set to: ${env.LOCAL_IMAGES}"
                        }
                    } else {
                        echo "No hay servicios para construir imágenes Docker"
                        env.LOCAL_IMAGES = ''
                        env.BUILT_IMAGES = ''
                    }
                }
            }
        }
        
        stage('Security & Quality') {
            steps {
                script {
                    try {
                        def localImages = []
                        
                        // Try to get images from stash first
                        try {
                            unstash 'docker-images'
                            if (fileExists('local_images.txt')) {
                                def imagesString = readFile('local_images.txt').trim()
                                if (!imagesString.isEmpty()) {
                                    localImages = imagesString.split(',')
                                }
                            }
                        } catch (Exception stashError) {
                            echo "⚠️ No se pudo obtener imágenes del stash: ${stashError.getMessage()}"
                        }
                        
                        // Fallback to environment variable
                        if (localImages.isEmpty() && env.LOCAL_IMAGES) {
                            localImages = env.LOCAL_IMAGES.split(',')
                        }
                        
                        echo "🐛 DEBUG: Found ${localImages.size()} images for security scan: ${localImages.join(', ')}"
                        
                        if (localImages.size() > 0) {
                            securityStages.runAllSecurityScans(localImages, params.SKIP_SECURITY_SCAN)
                        } else {
                            echo "No hay imágenes locales para escanear"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Error en escaneo de seguridad: ${e.getMessage()}"
                        echo "Continuando pipeline sin escaneo de seguridad"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Push Images') {
            steps {
                script {
                    def localImages = []
                    
                    // Try to get images from stash first
                    try {
                        unstash 'docker-images'
                        if (fileExists('local_images.txt')) {
                            def imagesString = readFile('local_images.txt').trim()
                            if (!imagesString.isEmpty()) {
                                localImages = imagesString.split(',')
                            }
                        }
                    } catch (Exception stashError) {
                        echo "⚠️ No se pudo obtener imágenes del stash: ${stashError.getMessage()}"
                    }
                    
                    // Fallback to environment variable
                    if (localImages.isEmpty() && env.LOCAL_IMAGES) {
                        localImages = env.LOCAL_IMAGES.split(',')
                    }
                    
                    echo "🐛 DEBUG: Found ${localImages.size()} images to push: ${localImages.join(', ')}"
                    
                    if (localImages.size() > 0) {
                        echo "🚀 Subiendo imágenes a Docker Hub: ${localImages.join(', ')}"
                        
                        withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            sh '''
                                echo "🔐 Iniciando sesión en Docker Hub..."
                                echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                            '''
                            
                            localImages.each { imageName ->
                                try {
                                    echo "⬆️ Subiendo imagen: ${imageName}"
                                    sh "docker push ${imageName}"
                                    echo "✅ Imagen subida: ${imageName}"
                                } catch (Exception pushError) {
                                    echo "❌ Error subiendo imagen ${imageName}: ${pushError.getMessage()}"
                                    currentBuild.result = 'UNSTABLE'
                                }
                            }
                            
                            sh '''
                                echo "🔓 Cerrando sesión de Docker Hub..."
                                docker logout
                            '''
                        }
                    } else {
                        echo "No hay imágenes locales para subir"
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    try {
                        // Only run if SonarQube analysis was performed
                        if (env.SONAR_TOKEN && env.SONAR_HOST_URL) {
                            securityStages.waitForQualityGate()
                        } else {
                            echo "ℹ️ SonarQube no configurado, saltando Quality Gate"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Error en Quality Gate: ${e.getMessage()}"
                        echo "Continuando pipeline sin validación de Quality Gate"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Security Policy Check') {
            when {
                expression { !params.SKIP_SECURITY_SCAN }
            }
            steps {
                script {
                    try {
                        testStages.checkSecurityPolicy()
                    } catch (Exception e) {
                        echo "⚠️ Error en Security Policy Check: ${e.getMessage()}"
                        echo "Continuando pipeline sin validación de políticas de seguridad"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Production Approval') {
            when {
                expression { env.IS_PRODUCTION_DEPLOY == 'true' }
            }
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    def servicesToDeploy = servicesString.split(',')
                    if (servicesToDeploy.size() > 0) {
                        deploymentStages.requestProductionApproval(servicesToDeploy, env.SEMANTIC_VERSION)
                    } else {
                        echo "No hay servicios para aprobación de producción"
                    }
                }
            }
        }
        
        stage('GitHub Release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                }
            }
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    versioningStages.createGitHubRelease(env.SEMANTIC_VERSION, servicesString)
                }
            }
        }
        
        stage('Load Testing') {
            steps {
                script {
                    unstash 'build-info'
                    def servicesString = readFile('services_to_build.txt').trim()
                    def servicesToBuild = servicesString.split(',')
                    if (servicesToBuild.size() > 0) {
                        echo "🧪 Ejecutando pruebas de carga para servicios: ${servicesToBuild.join(', ')}"
                        
                        try {
                            // Run load tests inline
                            if (fileExists("locust")) {
                                sh '''
                                    cd locust
                                    echo "🐛 Preparando entorno Locust..."
                                    
                                    # Instalar dependencias de Python si no existen
                                    if ! command -v python3 &> /dev/null; then
                                        echo "📦 Instalando Python3..."
                                        apt-get update && apt-get install -y python3 python3-pip
                                    fi
                                    
                                    # Verificar si existe requirements.txt
                                    if [ -f "requirements.txt" ]; then
                                        # Instalar dependencias de Locust
                                        pip3 install -r requirements.txt
                                    else
                                        # Instalar Locust directamente
                                        pip3 install locust
                                    fi
                                    
                                    # Configurar URL base para las pruebas
                                    export LOCUST_HOST=${LOCUST_HOST:-http://localhost:8080}
                                    
                                    echo "🚀 Iniciando pruebas de carga..."
                                    
                                    # Verificar si existe locustfile.py
                                    if [ -f "locustfile.py" ]; then
                                        # Ejecutar Locust en modo headless
                                        locust -f locustfile.py \
                                            --headless \
                                            --users 10 \
                                            --spawn-rate 2 \
                                            --run-time 2m \
                                            --host $LOCUST_HOST \
                                            --html locust-report.html \
                                            --csv locust-stats
                                    else
                                        echo "⚠️ No se encontró locustfile.py, creando prueba básica..."
                                        echo 'from locust import HttpUser, task
class BasicUser(HttpUser):
    @task
    def test_health(self):
        self.client.get("/health", catch_response=True)' > locustfile.py
                                        
                                        locust -f locustfile.py \
                                            --headless \
                                            --users 5 \
                                            --spawn-rate 1 \
                                            --run-time 1m \
                                            --host $LOCUST_HOST \
                                            --html locust-report.html \
                                            --csv locust-stats
                                    fi
                                    
                                    echo "✅ Pruebas de carga completadas"
                                    
                                    # Mostrar resumen de resultados
                                    if [ -f "locust-stats_stats.csv" ]; then
                                        echo "📊 Resumen de pruebas de carga:"
                                        head -5 locust-stats_stats.csv
                                    fi
                                '''
                            } else {
                                echo "📁 Directorio locust no encontrado, saltando pruebas de carga"
                            }
                        } catch (Exception e) {
                            echo "⚠️ Error ejecutando pruebas de carga: ${e.getMessage()}"
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No hay servicios para pruebas de carga"
                    }
                }
            }
            post {
                always {
                    script {
                        try {
                            // Publish load test results inline
                            if (fileExists("locust/locust-report.html")) {
                                publishHTML([
                                    allowMissing: true,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'locust',
                                    reportFiles: 'locust-report.html',
                                    reportName: 'Locust Load Test Report'
                                ])
                            }
                            
                            archiveArtifacts artifacts: 'locust/locust-stats*.csv,locust/locust-report.html', 
                                           fingerprint: true, 
                                           allowEmptyArchive: true
                        } catch (Exception e) {
                            echo "⚠️ Error publicando resultados de pruebas de carga: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                try {
                    // Check the build status and call the correct notification function
                    if (currentBuild.result == 'SUCCESS') {
                        notificationStages.sendSuccessNotification()
                    } else if (currentBuild.result == 'FAILURE') {
                        notificationStages.sendFailureNotification()
                    } else if (currentBuild.result == 'UNSTABLE') {
                        notificationStages.sendUnstableNotification()
                    } else if (currentBuild.result == 'ABORTED') {
                        notificationStages.sendAbortedNotification()
                    }
                } catch (Exception e) {
                    echo "Error sending notification: ${e.getMessage()}"
                }
                
                // Pipeline cleanup
                echo "Pipeline cleanup completed"
                
                // Clean workspace if needed
                try {
                    cleanWs(deleteDirs: true, notFailBuild: true)
                } catch (Exception e) {
                    echo "Warning: Could not clean workspace: ${e.getMessage()}"
                }
            }
        }
    }
}

// Funciones auxiliares para pruebas
def runUnitTests(List services) {
    echo "Ejecutando pruebas unitarias para servicios: ${services.join(', ')}"
    
    services.each { service ->
        try {
            echo "🧪 Ejecutando pruebas unitarias para ${service}..."
            sh """
                ./mvnw test -pl ${service} -am -Dtest.profile=unit
                echo "✅ Pruebas unitarias completadas para ${service}"
            """
        } catch (Exception e) {
            echo "⚠️ Error en pruebas unitarias de ${service}: ${e.getMessage()}"
            currentBuild.result = 'UNSTABLE'
        }
    }
    
    publishUnitTestResults(services)
}

def runIntegrationTests(List services) {
    echo "Ejecutando pruebas de integración para servicios: ${services.join(', ')}"
    
    services.each { service ->
        try {
            echo "🔗 Ejecutando pruebas de integración para ${service}..."
            sh """
                ./mvnw verify -pl ${service} -am -Dtest.profile=integration
                echo "✅ Pruebas de integración completadas para ${service}"
            """
        } catch (Exception e) {
            echo "⚠️ Error en pruebas de integración de ${service}: ${e.getMessage()}"
            currentBuild.result = 'UNSTABLE'
        }
    }
    
    publishIntegrationTestResults(services)
}

def runLoadTests(List services) {
    echo "Ejecutando pruebas de carga para servicios: ${services.join(', ')}"
    
    sh """
        cd locust
        echo "🐛 Preparando entorno Locust..."
        
        # Instalar dependencias de Python si no existen
        if ! command -v python3 &> /dev/null; then
            echo "📦 Instalando Python3..."
            apt-get update && apt-get install -y python3 python3-pip
        fi
        
        # Instalar dependencias de Locust
        pip3 install -r requirements.txt
        
        # Configurar URL base para las pruebas
        export LOCUST_HOST=\${LOCUST_HOST:-http://localhost:8080}
        
        echo "🚀 Iniciando pruebas de carga..."
        
        # Ejecutar Locust en modo headless
        locust -f locustfile.py \
            --headless \
            --users 10 \
            --spawn-rate 2 \
            --run-time 2m \
            --host \$LOCUST_HOST \
            --html locust-report.html \
            --csv locust-stats
        
        echo "✅ Pruebas de carga completadas"
        
        # Mostrar resumen de resultados
        if [ -f "locust-stats_stats.csv" ]; then
            echo "📊 Resumen de pruebas de carga:"
            head -5 locust-stats_stats.csv
        fi
    """
}

def publishUnitTestResults(List services) {
    services.each { service ->
        if (fileExists("${service}/target/surefire-reports/*.xml")) {
            junit "${service}/target/surefire-reports/*.xml"
        }
    }
}

def publishIntegrationTestResults(List services) {
    services.each { service ->
        if (fileExists("${service}/target/failsafe-reports/*.xml")) {
            junit "${service}/target/failsafe-reports/*.xml"
        }
    }
}

def publishLoadTestResults() {
    if (fileExists("locust/locust-report.html")) {
        publishHTML([
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'locust',
            reportFiles: 'locust-report.html',
            reportName: 'Locust Load Test Report'
        ])
    }
    
    archiveArtifacts artifacts: 'locust/locust-stats*.csv,locust/locust-report.html', 
                   fingerprint: true, 
                   allowEmptyArchive: true
}

def generateTestSummaryReport(String buildNumber, String branchName, String servicesToBuild) {
    def reportContent = libraryResource('templates/test-summary.html')
    
    // Reemplazar placeholders
    reportContent = reportContent.replace('${BUILD_NUMBER}', buildNumber)
    reportContent = reportContent.replace('${BRANCH_NAME}', branchName)
    reportContent = reportContent.replace('${BUILD_DATE}', new Date().toString())
    reportContent = reportContent.replace('${SERVICES_TO_BUILD}', servicesToBuild)
    
    writeFile file: 'test-summary-report.html', text: reportContent
    
    publishHTML([
        allowMissing: true,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: '.',
        reportFiles: 'test-summary-report.html',
        reportName: 'Test Summary Report'
    ])
}

        