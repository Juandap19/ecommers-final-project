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
                    try {
                        // First try the library function
                        def servicesToBuild = []
                        try {
                            servicesToBuild = buildStages.detectServicesToBuild(params)
                            echo "✅ Biblioteca compartida detectó servicios: ${servicesToBuild}"
                        } catch (Exception libError) {
                            echo "⚠️ Error en biblioteca compartida: ${libError.getMessage()}"
                            echo "🔍 Detectando servicios automáticamente..."
                            
                            // Fallback: Replicate shared library logic
                            def microservices = [
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
                            ]
                            def servicesToDetect = []
                            
                            // Check if it's a PR (similar to shared library logic)
                            if (env.CHANGE_TARGET) {
                                echo "🔄 PR detectado, analizando cambios..."
                                try {
                                    def changes = sh(
                                        script: "git diff --name-only origin/${env.CHANGE_TARGET}...HEAD",
                                        returnStdout: true
                                    ).trim().split('\n')
                                    
                                    servicesToDetect = microservices.findAll { service ->
                                        changes.any { it.startsWith("${service}/") }
                                    }
                                    echo "📝 Cambios detectados en: ${changes.join(', ')}"
                                    echo "🎯 Servicios afectados: ${servicesToDetect.join(', ')}"
                                } catch (Exception gitError) {
                                    echo "⚠️ Error detectando cambios: ${gitError.getMessage()}"
                                    servicesToDetect = []
                                }
                            } else {
                                // Build manual o push a main
                                def serviceToBuild = params.MICROSERVICE ?: 'ALL'
                                if (serviceToBuild == 'ALL') {
                                    servicesToDetect = microservices.findAll { service ->
                                        fileExists("${service}/pom.xml")
                                    }
                                    echo "🎯 Construyendo TODOS los microservicios disponibles"
                                } else {
                                    servicesToDetect = [serviceToBuild]
                                    echo "📋 Construyendo microservicio específico: ${serviceToBuild}"
                                }
                            }
                            
                            // Default para testing si no se encuentra nada (como en shared library)
                            if (servicesToDetect.isEmpty()) {
                                echo "ℹ️ No se detectaron cambios en microservicios"
                                servicesToDetect = ['user-service'] // Default como en la biblioteca
                                echo "🔧 Usando servicio por defecto para testing"
                            }
                            
                            // Verificar que los servicios existen
                            servicesToBuild = servicesToDetect.findAll { service ->
                                if (fileExists("${service}/pom.xml")) {
                                    echo "✅ Verificado: ${service}"
                                    return true
                                } else {
                                    echo "⚠️ ${service} no tiene pom.xml, omitiendo..."
                                    return false
                                }
                            }
                        }
                        
                        env.SERVICES_TO_BUILD = servicesToBuild ? servicesToBuild.join(',') : ''
                        echo "🔨 Servicios finales para construir: ${env.SERVICES_TO_BUILD}"
                        
                        // Determinar si es despliegue a producción (como en shared library)
                        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                            env.IS_PRODUCTION_DEPLOY = 'true'
                            echo "🚀 Despliegue a PRODUCCIÓN detectado"
                        }
                        
                        if (!env.SERVICES_TO_BUILD) {
                            error "No se detectaron microservicios para construir"
                        }
                        
                    } catch (Exception e) {
                        echo "❌ Error crítico detectando servicios: ${e.getMessage()}"
                        env.SERVICES_TO_BUILD = ''
                        currentBuild.result = 'FAILURE'
                        error "No se pudieron detectar los microservicios"
                    }
                }
            }
        }
        
        stage('Tests') {
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                    if (servicesToBuild.size() > 0) {
                        try {
                            testStages.runAllTests(servicesToBuild)
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
                            if (env.SERVICES_TO_BUILD) {
                                generateTestSummaryReport(
                                    env.BUILD_NUMBER, 
                                    env.BRANCH_NAME, 
                                    env.SERVICES_TO_BUILD
                                )
                            }
                        } catch (Exception e) {
                            echo "⚠️ Error generando reporte de tests: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                    if (servicesToBuild.size() > 0) {
                        try {
                            buildStages.packageMicroservices(servicesToBuild)
                        } catch (Exception e) {
                            echo "⚠️ Error empaquetando microservicios: ${e.getMessage()}"
                            echo "Intentando empaquetar localmente (usando lógica de shared library)..."
                            // Fallback: package locally using shared library approach
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
                    def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                    if (servicesToBuild.size() > 0) {
                        try {
                            def images = buildStages.buildDockerImages(servicesToBuild, env.DOCKERHUB_USERNAME)
                            env.LOCAL_IMAGES = images.local.join(',')
                            env.BUILT_IMAGES = images.built.join(',')
                        } catch (Exception e) {
                            echo "⚠️ Error construyendo imágenes Docker: ${e.getMessage()}"
                            echo "Intentando construir imágenes localmente..."
                            
                            // Fallback: Build Docker images locally with validation
                            def localImages = []
                            def builtImages = []
                            
                            servicesToBuild.each { service ->
                                try {
                                    if (fileExists("${service}/Dockerfile")) {
                                        // Verificar que el JAR existe (como en shared library)
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
                                }
                            }
                            
                            env.LOCAL_IMAGES = localImages.join(',')
                            env.BUILT_IMAGES = localImages.join(',')
                            
                            if (localImages.isEmpty()) {
                                echo "⚠️ No se construyeron imágenes Docker"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    } else {
                        echo "No hay servicios para construir imagenes Docker"
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
                        def localImages = env.LOCAL_IMAGES?.split(',') ?: []
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
                    def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                    if (servicesToBuild.size() > 0) {
                        deploymentStages.pushDockerImages(servicesToBuild, env.DOCKERHUB_USERNAME)
                    } else {
                        echo "No hay servicios para subir imagenes"
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    try {
                        securityStages.waitForQualityGate()
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
                        securityStages.checkSecurityPolicy()
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
                    def servicesToDeploy = env.SERVICES_TO_BUILD?.split(',') ?: []
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
                    versioningStages.createGitHubRelease(env.SEMANTIC_VERSION, env.SERVICES_TO_BUILD)
                }
            }
        }
        
        stage('Load Testing') {
            steps {
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                    if (servicesToBuild.size() > 0) {
                        runLoadTests(servicesToBuild)
                    } else {
                        echo "No hay servicios para pruebas de carga"
                    }
                }
            }
            post {
                always {
                    publishLoadTestResults()
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
        dir(service) {
            sh """
                echo "🧪 Ejecutando pruebas unitarias para ${service}..."
                ./mvnw test -Dtest.profile=unit
                echo "✅ Pruebas unitarias completadas para ${service}"
            """
        }
    }
    
    publishUnitTestResults(services)
}

def runIntegrationTests(List services) {
    echo "Ejecutando pruebas de integración para servicios: ${services.join(', ')}"
    
    services.each { service ->
        dir(service) {
            sh """
                echo "🔗 Ejecutando pruebas de integración para ${service}..."
                ./mvnw verify -Dtest.profile=integration
                echo "✅ Pruebas de integración completadas para ${service}"
            """
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

        