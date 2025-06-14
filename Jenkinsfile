pipeline {
    agent any

    parameters {
        choice(
            name: 'MICROSERVICE',
            choices: ['ALL', 'api-gateway', 'service-discovery', 'user-service', 'product-service', 'order-service', 'payment-service', 'shipping-service', 'favourite-service'],
            description: 'Seleccionar microservicio específico o ALL para todos'
        )
        booleanParam(
            name: 'SKIP_SECURITY_SCAN',
            defaultValue: false,
            description: 'Saltar escaneo de seguridad con Trivy'
        )
    }

    environment {
        // Credenciales y configuración existente
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        SONAR_HOST_URL = 'http://sonarqube:9000'
        
        // Configuración de Java
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        
        // Configuración de la aplicación
        APP_NAME = 'ecommerce-microservice-backend'
        DOCKER_IMAGE = "${APP_NAME}:${env.BUILD_NUMBER}"

        DOCKERHUB_USERNAME = 'j2loop' 
        DOCKERHUB_CREDENTIALS_ID = 'DOCKERHUB_CREDENTIALS' // El ID de la credencial que creaste en Jenkins
        
        // Configuración de GitHub
        GITHUB_TOKEN = credentials('GITHUB_TOKEN') // Añadir token de GitHub en Jenkins
        
        // Configuración de notificaciones
        EMAIL_RECIPIENTS = 'juanjolo1204lo@gmail.com' // Emails para notificaciones
        
        // Variables dinámicas (se establecen durante el pipeline)
        SEMANTIC_VERSION = ''
        IS_PRODUCTION_DEPLOY = 'false'
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
                echo 'Compilando el proyecto...'
                sh './mvnw clean compile'
            }
        }

        stage('Calculate Semantic Version') {
            steps {
                echo 'Calculando versión semántica...'
                script {
                    // Obtener último tag de versión
                    def lastTag = sh(
                        script: "git describe --tags --abbrev=0 2>/dev/null || echo 'v0.0.0'",
                        returnStdout: true
                    ).trim()
                    
                    echo "🏷️  Último tag: ${lastTag}"
                    
                    // Extraer números de versión (ej: v1.2.3 -> [1, 2, 3])
                    def versionNumbers = lastTag.replaceAll(/^v/, '').split('\\.')
                    def major = versionNumbers[0] as Integer
                    def minor = versionNumbers.size() > 1 ? versionNumbers[1] as Integer : 0
                    def patch = versionNumbers.size() > 2 ? versionNumbers[2] as Integer : 0
                    
                    // Analizar commits desde el último tag para determinar tipo de versión
                    def commitMessages = sh(
                        script: "git log ${lastTag}..HEAD --pretty=format:'%s' 2>/dev/null || git log --pretty=format:'%s' -10",
                        returnStdout: true
                    ).trim()
                    
                    echo "📝 Analizando commits para versionado semántico..."
                    
                    def isMajor = commitMessages.contains('BREAKING CHANGE') || 
                                 commitMessages.contains('!:') ||
                                 commitMessages.toLowerCase().contains('breaking')
                    def isMinor = commitMessages.toLowerCase().contains('feat:') ||
                                 commitMessages.toLowerCase().contains('feature:')
                    def isPatch = commitMessages.toLowerCase().contains('fix:') ||
                                 commitMessages.toLowerCase().contains('bugfix:') ||
                                 commitMessages.toLowerCase().contains('patch:')
                    
                    // Calcular nueva versión
                    def newVersion
                    if (isMajor) {
                        newVersion = "${major + 1}.0.0"
                        echo "🚨 BREAKING CHANGE detectado - Incrementando versión MAJOR"
                    } else if (isMinor) {
                        newVersion = "${major}.${minor + 1}.0"
                        echo "✨ Nueva funcionalidad detectada - Incrementando versión MINOR"
                    } else if (isPatch || env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                        newVersion = "${major}.${minor}.${patch + 1}"
                        echo "🔧 Fix o release detectado - Incrementando versión PATCH"
                    } else {
                        // Para branches de desarrollo, usar versión con suffix
                        def branchSuffix = env.BRANCH_NAME.replaceAll(/[^a-zA-Z0-9]/, '-').toLowerCase()
                        newVersion = "${major}.${minor}.${patch + 1}-${branchSuffix}.${env.BUILD_NUMBER}"
                        echo "🔀 Branch de desarrollo - Usando versión con suffix"
                    }
                    
                    env.SEMANTIC_VERSION = newVersion
                    echo "🎯 Nueva versión semántica: v${newVersion}"
                }
            }
        }

        stage('Detect Services to Build') {
            steps {
                echo 'Detectando microservicios a construir...'
                script {
                    // Definir microservicios disponibles
                    def microservices = [
                        'api-gateway',
                        'service-discovery', 
                        'user-service',
                        'product-service',
                        'order-service',
                        'payment-service',
                        'shipping-service',
                        'favourite-service'
                    ]
                    
                    def servicesToBuild = []
                    
                    // Detectar cambios (si es commit específico) o construir todos (si es manual)
                    if (env.CHANGE_TARGET) {
                        // Es un PR, detectar cambios
                        def changes = sh(
                            script: "git diff --name-only origin/${env.CHANGE_TARGET}...HEAD",
                            returnStdout: true
                        ).trim().split('\n')
                        
                        servicesToBuild = microservices.findAll { service ->
                            changes.any { it.startsWith("${service}/") }
                        }
                    } else {
                        // Build manual o push a main, construir servicios específicos
                        def serviceToBuild = params.MICROSERVICE ?: 'ALL'
                        if (serviceToBuild == 'ALL') {
                            servicesToBuild = microservices
                        } else {
                            servicesToBuild = [serviceToBuild]
                        }
                    }
                    
                    if (servicesToBuild.isEmpty()) {
                        echo "ℹ️  No se detectaron cambios en microservicios"
                        servicesToBuild = ['user-service'] // Default para testing
                    }
                    
                    echo "🔨 Microservicios a construir: ${servicesToBuild.join(', ')}"
                    
                    // Guardar lista de servicios para etapas posteriores
                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                    
                    // Determinar si es despliegue a producción
                    if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                        env.IS_PRODUCTION_DEPLOY = 'true'
                        echo "🚀 Despliegue a PRODUCCIÓN detectado"
                    }
                }
            }
        }

        stage('Package Microservices') {
            steps {
                echo 'Empaquetando microservicios...'
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')
                    
                    for (service in servicesToBuild) {
                        echo "📦 Empaquetando ${service}..."
                        
                        // Opción 1: Si cada microservicio tiene su propio pom.xml
                        if (fileExists("${service}/pom.xml")) {
                            sh """
                                cd ${service}
                                ../mvnw clean package -DskipTests
                                echo "✅ JAR generado para ${service}"
                                ls -la target/*.jar || echo "⚠️  No se encontró JAR en target/"
                            """
                        } else {
                            // Opción 2: Si es un proyecto multi-módulo con un pom padre
                            sh """
                                ./mvnw clean package -pl ${service} -am -DskipTests
                                echo "✅ JAR generado para ${service}"
                                ls -la ${service}/target/*.jar || echo "⚠️  No se encontró JAR en ${service}/target/"
                            """
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Construyendo imágenes Docker...'
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')
                    def builtImages = []
                    def localImages = []
                    
                    for (service in servicesToBuild) {
                        if (fileExists("${service}/Dockerfile")) {
                            echo "🐳 Construyendo imagen Docker para ${service}..."
                            
                            // Verificar que el JAR existe antes de construir
                            def jarExists = sh(
                                script: "ls ${service}/target/*.jar 2>/dev/null | wc -l",
                                returnStdout: true
                            ).trim()
                            
                            if (jarExists == "0") {
                                error "❌ No se encontró JAR para ${service}. Verifica que el empaquetado fue exitoso."
                            }
                            
                            sh """
                                cd ${service}
                                # Construir la imagen con versionado semántico
                                docker build -t ${service}:${env.BUILD_NUMBER} .
                                docker tag ${service}:${env.BUILD_NUMBER} ${service}:latest
                                docker tag ${service}:${env.BUILD_NUMBER} ${service}:v${env.SEMANTIC_VERSION}
                                docker tag ${service}:${env.BUILD_NUMBER} ${DOCKERHUB_USERNAME}/${service}:${env.BUILD_NUMBER}
                                docker tag ${service}:${env.BUILD_NUMBER} ${DOCKERHUB_USERNAME}/${service}:latest
                                docker tag ${service}:${env.BUILD_NUMBER} ${DOCKERHUB_USERNAME}/${service}:v${env.SEMANTIC_VERSION}
                            """
                            
                            // Agregar imagen local para escaneo de seguridad
                            localImages.add("${service}:${env.BUILD_NUMBER}")
                            // Agregar imágenes de Docker Hub para push (con versionado semántico)
                            builtImages.add("${DOCKERHUB_USERNAME}/${service}:${env.BUILD_NUMBER}")
                            builtImages.add("${DOCKERHUB_USERNAME}/${service}:v${env.SEMANTIC_VERSION}")
                        } else {
                            echo "⚠️  No se encontró Dockerfile en ${service}/"
                        }
                    }
                    
                    // Guardar listas de imágenes para etapas posteriores
                    env.LOCAL_IMAGES = localImages.join(',')
                    env.BUILT_IMAGES = builtImages.join(',')
                    echo "📦 Imágenes locales (para escaneo): ${env.LOCAL_IMAGES}"
                    echo "📦 Imágenes para push: ${env.BUILT_IMAGES}"
                }
            }
        }

        stage('Code Quality and Security Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    steps {
                        echo 'Ejecutando análisis de SonarQube...'
                        withSonarQubeEnv('SonarQube-Server') {
                            sh """
                                ./mvnw sonar:sonar \
                                -Dsonar.projectKey=ecommerce-microservice-backend \
                                -Dsonar.projectName='Ecommerce Microservice Backend' \
                                -Dsonar.projectVersion=${env.BUILD_NUMBER} \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_TOKEN}
                            """
                        }
                    }
                }
                
                stage('Container Security Scan') {
                    when {
                        expression { !params.SKIP_SECURITY_SCAN }
                    }
                    steps {
                        echo 'Ejecutando escaneo de seguridad con Trivy (modo standalone)...'
                        script {
                            def imagesToScan = env.LOCAL_IMAGES?.split(',') ?: []
                            
                            if (imagesToScan.size() == 0) {
                                echo "ℹ️  No hay imágenes locales para escanear"
                                return
                            }
                            
                            sh """
                                # Instalar Trivy si no existe
                                if ! command -v trivy &> /dev/null; then
                                    echo "📦 Instalando Trivy..."
                                    curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
                                fi
                                
                                echo "✅ Trivy instalado y listo para escaneo local"
                            """
                            
                            def totalCritical = 0
                            def totalHigh = 0
                            def scanResults = []
                            
                            // Escanear cada imagen local construida
                            for (image in imagesToScan) {
                                // Extraer el nombre del servicio desde imagen local (ej: user-service:123)
                                def serviceName = image.split(':')[0]
                                echo "🛡️  Escaneando imagen local ${image}..."
                                
                                sh """
                                    # Escaneo completo en formato JSON
                                    trivy image \
                                        --format json \
                                        --output trivy-${serviceName}-report.json \
                                        ${image}
                                    
                                    # Escaneo resumen para consola
                                    trivy image \
                                        --format table \
                                        --output trivy-${serviceName}-summary.txt \
                                        --severity CRITICAL,HIGH \
                                        ${image}
                                    
                                    echo "📊 Resumen de ${serviceName}:"
                                    cat trivy-${serviceName}-summary.txt || echo "No hay vulnerabilidades críticas/altas"
                                """
                                
                                // Contar vulnerabilidades para este servicio
                                def criticalCount = sh(
                                    script: "cat trivy-${serviceName}-report.json | jq '.Results[]?.Vulnerabilities[]? | select(.Severity==\"CRITICAL\") | .VulnerabilityID' | wc -l",
                                    returnStdout: true
                                ).trim() as Integer
                                
                                def highCount = sh(
                                    script: "cat trivy-${serviceName}-report.json | jq '.Results[]?.Vulnerabilities[]? | select(.Severity==\"HIGH\") | .VulnerabilityID' | wc -l",
                                    returnStdout: true
                                ).trim() as Integer
                                
                                totalCritical += criticalCount
                                totalHigh += highCount
                                
                                scanResults.add([
                                    service: serviceName,
                                    image: image,
                                    critical: criticalCount,
                                    high: highCount
                                ])
                                
                                echo "🔴 ${serviceName} - Críticas: ${criticalCount}, Altas: ${highCount}"
                            }
                            
                            // Generar reporte consolidado
                            sh """
                                echo "TOTAL_CRITICAL_VULNS=${totalCritical}" > trivy-metrics.properties
                                echo "TOTAL_HIGH_VULNS=${totalHigh}" >> trivy-metrics.properties
                                echo "SCANNED_SERVICES=${imagesToScan.size()}" >> trivy-metrics.properties
                                
                                # Crear reporte consolidado HTML
                                cat > trivy-consolidated-report.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Trivy Security Report - Microservices (Local Scan)</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .service { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .critical { color: #d32f2f; font-weight: bold; }
        .high { color: #f57c00; font-weight: bold; }
        .summary { background: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <h1>🛡️ Trivy Security Report (Local Images) - Build ${BUILD_NUMBER}</h1>
    <div class="summary">
        <h2>📊 Resumen General</h2>
        <p><strong>Servicios escaneados:</strong> ${imagesToScan.size()}</p>
        <p><strong>Total vulnerabilidades críticas:</strong> <span class="critical">${totalCritical}</span></p>
        <p><strong>Total vulnerabilidades altas:</strong> <span class="high">${totalHigh}</span></p>
        <p><strong>Modo:</strong> Standalone (imágenes locales)</p>
        <p><strong>Fecha:</strong> ${new Date()}</p>
    </div>
EOF
                            """
                            
                            // Agregar detalles de cada servicio al HTML
                            for (result in scanResults) {
                                sh """
cat >> trivy-consolidated-report.html << 'EOF'
    <div class="service">
        <h3>🚀 ${result.service}</h3>
        <p><strong>Imagen local:</strong> ${result.image}</p>
        <p><strong>Vulnerabilidades críticas:</strong> <span class="critical">${result.critical}</span></p>
        <p><strong>Vulnerabilidades altas:</strong> <span class="high">${result.high}</span></p>
        <a href="trivy-${result.service}-report.json" target="_blank">Ver reporte detallado JSON</a>
    </div>
EOF
                                """
                            }
                            
                            sh 'echo "</body></html>" >> trivy-consolidated-report.html'
                            
                            echo "📈 Resumen final del escaneo local:"
                            echo "   - Total servicios: ${imagesToScan.size()}"
                            echo "   - Total vulnerabilidades críticas: ${totalCritical}"
                            echo "   - Total vulnerabilidades altas: ${totalHigh}"
                            
                            // Política de seguridad - fallar si hay vulnerabilidades críticas
                            if (totalCritical > 0) {
                                echo "❌ ADVERTENCIA: Se encontraron ${totalCritical} vulnerabilidades críticas en total"
                                echo "🚫 Considerando fallar el build por política de seguridad..."
                                // Descomentar para fallar el build:
                                // error("Build fallido por vulnerabilidades críticas detectadas")
                            }
                            
                            if (totalHigh > 20) {
                                echo "⚠️  ADVERTENCIA: Se encontraron ${totalHigh} vulnerabilidades altas en total (límite recomendado: 20)"
                                currentBuild.result = 'UNSTABLE'
                            }
                        }
                    }
                    post {
                        always {
                            // Publicar reporte consolidado
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: '.',
                                reportFiles: 'trivy-consolidated-report.html',
                                reportName: 'Trivy Security Report (Local)'
                            ])
                            
                            // Archivar todos los reportes
                            archiveArtifacts artifacts: 'trivy-*-report.json,trivy-*-summary.txt,trivy-metrics.properties,trivy-consolidated-report.html', 
                                           fingerprint: true, 
                                           allowEmptyArchive: true
                        }
                    }
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                echo 'Publicando imágenes Docker en Docker Hub...'
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')
                    
                    withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                        // Login a Docker Hub
                        sh 'echo $DOCKERHUB_PASS | docker login -u $DOCKERHUB_USER --password-stdin'
                        
                        for (service in servicesToBuild) {
                            if (fileExists("${service}/Dockerfile")) {
                                echo "📤 Publicando ${service} en Docker Hub..."
                                
                                sh """
                                    # Push imagen con número de build
                                    docker push ${DOCKERHUB_USERNAME}/${service}:${env.BUILD_NUMBER}
                                    
                                    # Push imagen latest
                                    docker push ${DOCKERHUB_USERNAME}/${service}:latest
                                    
                                    # Push imagen con versión semántica
                                    docker push ${DOCKERHUB_USERNAME}/${service}:v${env.SEMANTIC_VERSION}
                                    
                                    echo "✅ ${service} publicado exitosamente con versión v${env.SEMANTIC_VERSION}"
                                """
                            }
                        }
                        
                        // Logout de Docker Hub por seguridad
                        sh 'docker logout'
                    }
                    
                    echo "🎉 Todas las imágenes han sido publicadas en Docker Hub"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo 'Esperando resultado del Quality Gate de SonarQube...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Security Policy Check') {
            when {
                expression { !params.SKIP_SECURITY_SCAN }
            }
            steps {
                echo 'Verificando políticas de seguridad...'
                script {
                    // Leer métricas de Trivy
                    if (fileExists('trivy-metrics.properties')) {
                        def props = readProperties file: 'trivy-metrics.properties'
                        def totalCriticalVulns = props.TOTAL_CRITICAL_VULNS as Integer
                        def totalHighVulns = props.TOTAL_HIGH_VULNS as Integer
                        def scannedServices = props.SCANNED_SERVICES as Integer
                        
                        echo "📊 Métricas de seguridad consolidadas:"
                        echo "   - Servicios escaneados: ${scannedServices}"
                        echo "   - Total vulnerabilidades críticas: ${totalCriticalVulns}"
                        echo "   - Total vulnerabilidades altas: ${totalHighVulns}"
                        
                        // Definir políticas (ajusta según tus necesidades)
                        if (totalCriticalVulns > 0) {
                            echo "⚠️  POLÍTICA: Se encontraron ${totalCriticalVulns} vulnerabilidades críticas en ${scannedServices} servicios"
                            // currentBuild.result = 'UNSTABLE'  // Marca como inestable
                            // error("Build fallido por vulnerabilidades críticas") // Falla el build
                        }
                        
                        if (totalHighVulns > 30) {
                            echo "⚠️  POLÍTICA: Demasiadas vulnerabilidades altas (${totalHighVulns} > 30) en todos los servicios"
                            currentBuild.result = 'UNSTABLE'
                        }
                        
                        // Política por promedio de servicios
                        def avgCritical = totalCriticalVulns.toFloat() / scannedServices
                        def avgHigh = totalHighVulns.toFloat() / scannedServices
                        
                        echo "📈 Promedio por servicio:"
                        echo "   - Críticas: ${String.format('%.2f', avgCritical)}"
                        echo "   - Altas: ${String.format('%.2f', avgHigh)}"
                        
                        echo "✅ Verificación de políticas completada"
                    } else {
                        echo "ℹ️  No se encontraron métricas de seguridad para evaluar"
                    }
                }
            }
        }
        
        stage('Production Deployment Approval') {
            when {
                expression { env.IS_PRODUCTION_DEPLOY == 'true' }
            }
            steps {
                echo '🚨 Solicitando aprobación para despliegue a PRODUCCIÓN...'
                script {
                    def servicesToDeploy = env.SERVICES_TO_BUILD.split(',')
                    def approvalMessage = """
🚀 APROBACIÓN REQUERIDA: Despliegue a Producción

📋 Detalles del despliegue:
• Versión: v${env.SEMANTIC_VERSION}
• Branch: ${env.BRANCH_NAME}
• Build: ${env.BUILD_NUMBER}
• Servicios: ${servicesToDeploy.join(', ')}

🛡️ Verificaciones completadas:
✅ Compilación exitosa
✅ Análisis de calidad (SonarQube)
✅ Escaneo de seguridad (Trivy)
✅ Imágenes Docker construidas

⚠️  Este despliegue afectará el entorno de PRODUCCIÓN.
¿Desea continuar con el despliegue?
                    """
                    
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            def userInput = input(
                                id: 'productionDeployApproval',
                                message: approvalMessage,
                                parameters: [
                                    choice(
                                        choices: ['Aprobar y continuar', 'Rechazar despliegue'],
                                        description: 'Seleccione una opción:',
                                        name: 'APPROVAL_CHOICE'
                                    )
                                ],
                                submitterParameter: 'APPROVER'
                            )
                            
                            if (userInput.APPROVAL_CHOICE == 'Rechazar despliegue') {
                                error("❌ Despliegue a producción RECHAZADO por ${userInput.APPROVER}")
                            }
                            
                            echo "✅ Despliegue a producción APROBADO por ${userInput.APPROVER}"
                            env.DEPLOYMENT_APPROVER = userInput.APPROVER
                            
                        }
                    } catch (Exception e) {
                        echo "⏰ Timeout o rechazo de aprobación: ${e.getMessage()}"
                        currentBuild.result = 'ABORTED'
                        error("❌ Despliegue cancelado: No se recibió aprobación a tiempo")
                    }
                }
            }
        }

        stage('Create GitHub Release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                }
            }
            steps {
                echo 'Creando release en GitHub...'
                script {
                    def servicesToBuild = env.SERVICES_TO_BUILD.split(',')
                    def releaseTag = "v${env.SEMANTIC_VERSION}"
                    def releaseTitle = "Release ${releaseTag} - ${servicesToBuild.join(', ')}"
                    def releaseNotes = """
## 🚀 Release ${releaseTag}

### Microservicios actualizados:
${servicesToBuild.collect { "- ${it}" }.join('\n')}

### 📦 Imágenes Docker publicadas:
${servicesToBuild.collect { "- \`${env.DOCKERHUB_USERNAME}/${it}:v${env.SEMANTIC_VERSION}\`" }.join('\n')}
${servicesToBuild.collect { "- \`${env.DOCKERHUB_USERNAME}/${it}:latest\`" }.join('\n')}

### 📊 Información del build:
- **Build ID:** ${env.BUILD_NUMBER}
- **Branch:** ${env.BRANCH_NAME}
- **Commit:** ${env.GIT_COMMIT?.take(8)}
- **Fecha:** ${new Date().format('yyyy-MM-dd HH:mm:ss')}

### 🛡️ Seguridad:
- Análisis de calidad: ✅ SonarQube
- Escaneo de seguridad: ✅ Trivy
- Imágenes validadas y publicadas en Docker Hub

---
*Release generado automáticamente por Jenkins Pipeline*
                    """
                    
                    withCredentials([string(credentialsId: 'GITHUB_TOKEN', variable: 'GITHUB_TOKEN')]) {
                        sh """
                            # Instalar gh CLI si no existe
                            if ! command -v gh &> /dev/null; then
                                echo "📦 Instalando GitHub CLI..."
                                curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
                                echo "deb [arch=\$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
                                sudo apt update && sudo apt install gh -y
                            fi
                            
                            # Configurar autenticación
                            echo "${GITHUB_TOKEN}" | gh auth login --with-token
                            
                            # Crear el release
                            gh release create "${releaseTag}" \\
                                --title "${releaseTitle}" \\
                                --notes "${releaseNotes}" \\
                                --latest
                            
                            echo "✅ Release ${releaseTag} creado exitosamente en GitHub"
                        """
                    }
                }
            }
        }

        stage('Docker Cleanup') {
            steps {
                echo 'Limpiando recursos Docker...'
                script {
                    def imagesToClean = env.BUILT_IMAGES?.split(',') ?: []
                    
                    if (imagesToClean.size() == 0) {
                        echo "ℹ️  No hay imágenes para limpiar"
                        return
                    }
                    
                    echo "🧹 Limpiando ${imagesToClean.size()} imágenes construidas..."
                    
                    for (image in imagesToClean) {
                        // Extraer el nombre del servicio correctamente
                        def imageParts = image.split(':')[0].split('/')
                        def serviceName = imageParts[-1] // Obtener la última parte (nombre del servicio)
                        echo "🗑️  Limpiando ${image}..."
                        
                        sh """
                            # Remover imagen con tag del build
                            docker rmi ${image} || echo "⚠️  No se pudo remover ${image}"
                            
                            # Remover imagen con tag latest
                            docker rmi ${serviceName}:latest || echo "⚠️  No se pudo remover ${serviceName}:latest"
                        """
                    }
                    
                    // Limpiar imágenes huérfanas y sin usar
                    echo "🧹 Limpiando imágenes sin usar..."
                    sh """
                        # Remover imágenes sin usar (dangling)
                        docker image prune -f || echo "⚠️  No se pudo ejecutar image prune"
                        
                        # Mostrar espacio liberado
                        echo "📊 Estado actual de Docker:"
                        docker system df || echo "⚠️  No se pudo obtener información del sistema Docker"
                    """
                    
                    echo "✅ Limpieza Docker completada"
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completado'
            // Limpiar archivos temporales
            sh """
                rm -f trivy-*-report.json trivy-*-summary.txt trivy-metrics.properties || true
            """

            echo 'Limpieza final del pipeline...'
            
            
            cleanWs() 
        }
        
        success {
            echo '✅ Pipeline completado exitosamente!'
            script {
                def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                def emailSubject = "✅ Pipeline Exitoso - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                def emailBody = """
<h2>🎉 Pipeline Completado Exitosamente</h2>

<h3>📋 Información del Build:</h3>
<ul>
    <li><strong>Job:</strong> ${env.JOB_NAME}</li>
    <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
    <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
    <li><strong>Versión:</strong> v${env.SEMANTIC_VERSION ?: 'N/A'}</li>
    <li><strong>Commit:</strong> ${env.GIT_COMMIT?.take(8)}</li>
    <li><strong>Duración:</strong> ${currentBuild.durationString}</li>
</ul>

<h3>🚀 Microservicios Procesados:</h3>
<ul>
    ${servicesToBuild.collect { "<li>${it}</li>" }.join('')}
</ul>

<h3>📦 Artefactos Generados:</h3>
<ul>
    ${servicesToBuild.collect { "<li>Docker: j2loop/${it}:v${env.SEMANTIC_VERSION ?: 'latest'}</li>" }.join('')}
</ul>

${env.IS_PRODUCTION_DEPLOY == 'true' ? "<h3>✅ Despliegue a Producción:</h3><p><strong>Aprobado por:</strong> ${env.DEPLOYMENT_APPROVER ?: 'N/A'}</p>" : ''}

<h3>🛡️ Verificaciones de Seguridad:</h3>
${fileExists('trivy-metrics.properties') ? 
    readProperties(file: 'trivy-metrics.properties').collect { k, v -> 
        "<li><strong>${k.replace('_', ' ').toLowerCase().capitalize()}:</strong> ${v}</li>" 
    }.join('') : '<li>No hay métricas de seguridad disponibles</li>'}

<hr>
<p><small>Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></small></p>
                """
                
                emailext (
                    subject: emailSubject,
                    body: emailBody,
                    mimeType: 'text/html',
                    to: env.EMAIL_RECIPIENTS
                )
                
                // Enviar notificación con métricas de seguridad si existen
                if (fileExists('trivy-metrics.properties')) {
                    def props = readProperties file: 'trivy-metrics.properties'
                    echo "📊 Resumen final de seguridad:"
                    echo "   - Servicios escaneados: ${props.SCANNED_SERVICES ?: 0}"
                    echo "   - Total vulnerabilidades críticas: ${props.TOTAL_CRITICAL_VULNS ?: 0}"
                    echo "   - Total vulnerabilidades altas: ${props.TOTAL_HIGH_VULNS ?: 0}"
                }
            }
        }
        
        failure {
            echo '❌ El pipeline falló'
            script {
                def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                def emailSubject = "❌ Pipeline Fallido - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                def emailBody = """
<h2>🚨 Pipeline Fallido</h2>

<h3>📋 Información del Build:</h3>
<ul>
    <li><strong>Job:</strong> ${env.JOB_NAME}</li>
    <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
    <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
    <li><strong>Versión:</strong> v${env.SEMANTIC_VERSION ?: 'N/A'}</li>
    <li><strong>Commit:</strong> ${env.GIT_COMMIT?.take(8)}</li>
    <li><strong>Duración:</strong> ${currentBuild.durationString}</li>
    <li><strong>Stage Fallido:</strong> ${env.STAGE_NAME ?: 'Desconocido'}</li>
</ul>

<h3>🚀 Microservicios en Proceso:</h3>
<ul>
    ${servicesToBuild.collect { "<li>${it}</li>" }.join('')}
</ul>

<h3>🔍 Acciones Requeridas:</h3>
<ul>
    <li>Revisar los logs del build para identificar el error</li>
    <li>Verificar configuración de dependencias</li>
    <li>Validar tests y análisis de calidad</li>
    <li>Revisar configuración de seguridad</li>
</ul>

<hr>
<p><strong>🔗 Enlaces útiles:</strong></p>
<ul>
    <li><a href="${env.BUILD_URL}">Ver logs del build</a></li>
    <li><a href="${env.BUILD_URL}/console">Consola completa</a></li>
</ul>
                """
                
                emailext (
                    subject: emailSubject,
                    body: emailBody,
                    mimeType: 'text/html',
                    to: env.EMAIL_RECIPIENTS
                )
            }
        }
        
        unstable {
            echo '⚠️  Pipeline completado con advertencias de seguridad'
            script {
                def servicesToBuild = env.SERVICES_TO_BUILD?.split(',') ?: []
                def emailSubject = "⚠️ Pipeline Inestable - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                def emailBody = """
<h2>⚠️ Pipeline Completado con Advertencias</h2>

<h3>📋 Información del Build:</h3>
<ul>
    <li><strong>Job:</strong> ${env.JOB_NAME}</li>
    <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
    <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
    <li><strong>Versión:</strong> v${env.SEMANTIC_VERSION ?: 'N/A'}</li>
    <li><strong>Commit:</strong> ${env.GIT_COMMIT?.take(8)}</li>
    <li><strong>Duración:</strong> ${currentBuild.durationString}</li>
</ul>

<h3>🚀 Microservicios Procesados:</h3>
<ul>
    ${servicesToBuild.collect { "<li>${it}</li>" }.join('')}
</ul>

<h3>⚠️ Advertencias de Seguridad:</h3>
${fileExists('trivy-metrics.properties') ? 
    readProperties(file: 'trivy-metrics.properties').collect { k, v -> 
        "<li><strong>${k.replace('_', ' ').toLowerCase().capitalize()}:</strong> ${v}</li>" 
    }.join('') : '<li>No hay métricas de seguridad disponibles</li>'}

<h3>🔧 Acciones Recomendadas:</h3>
<ul>
    <li>Revisar y solucionar vulnerabilidades de seguridad detectadas</li>
    <li>Actualizar dependencias con vulnerabilidades</li>
    <li>Verificar configuración de seguridad</li>
</ul>

<hr>
<p><strong>🔗 Enlaces útiles:</strong></p>
<ul>
    <li><a href="${env.BUILD_URL}">Ver detalles del build</a></li>
    <li><a href="${env.BUILD_URL}/Trivy_Security_Report_(Local)/">Ver reporte de seguridad</a></li>
</ul>
                """
                
                emailext (
                    subject: emailSubject,
                    body: emailBody,
                    mimeType: 'text/html',
                    to: env.EMAIL_RECIPIENTS
                )
            }
        }
        
        aborted {
            echo '🛑 Pipeline cancelado/abortado'
            script {
                def emailSubject = "🛑 Pipeline Cancelado - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                def emailBody = """
<h2>🛑 Pipeline Cancelado</h2>

<h3>📋 Información del Build:</h3>
<ul>
    <li><strong>Job:</strong> ${env.JOB_NAME}</li>
    <li><strong>Build:</strong> #${env.BUILD_NUMBER}</li>
    <li><strong>Branch:</strong> ${env.BRANCH_NAME}</li>
    <li><strong>Duración:</strong> ${currentBuild.durationString}</li>
    <li><strong>Razón:</strong> ${env.IS_PRODUCTION_DEPLOY == 'true' ? 'Aprobación de producción rechazada o timeout' : 'Cancelado manualmente'}</li>
</ul>

${env.IS_PRODUCTION_DEPLOY == 'true' ? '<p><strong>⚠️ Nota:</strong> El despliegue a producción fue rechazado o no se recibió aprobación a tiempo.</p>' : ''}

<hr>
<p><a href="${env.BUILD_URL}">Ver detalles del build</a></p>
                """
                
                emailext (
                    subject: emailSubject,
                    body: emailBody,
                    mimeType: 'text/html',
                    to: env.EMAIL_RECIPIENTS
                )
            }
        }
    }
}