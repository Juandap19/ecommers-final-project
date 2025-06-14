
# Documentación del Proyecto

## 1. Metodología Ágil

### 📌 Descripción

Explica brevemente qué metodología ágil se está utilizando (Scrum, Kanban, SAFe, etc.), los roles principales (Scrum Master, Product Owner, equipo de desarrollo), y la frecuencia de las ceremonias.

👉 [Ver metodología completa](./METHODOLOGY.md)

---

## 2. Estrategia de Ramas (Branching Strategy)

### 📌 Descripción

Describe la estrategia utilizada: Git Flow, Trunk-Based Development, GitHub Flow, etc. Incluye el flujo de ramas (main, develop, feature, release, hotfix) y su propósito.

👉 [Ver estrategia de ramas](./BRANCHING_STRATEGY.md)

---

## 3. Sprints

### 📌 Sprint 1

* Objetivos principales
* Entregables alcanzados
* Lecciones aprendidas

# FALTA 

### 📌 Sprint 2

* Objetivos principales
* Entregables alcanzados
* Lecciones aprendidas

# FALTA 

---

## 4. Infraestructura como Código (IaC) con Terraform

### 📌 Descripción

* Descripción del uso de Terraform para gestionar la infraestructura
* Módulos reutilizables creados
* Ejemplos de recursos gestionados (VPC, EC2, S3, RDS, etc.)
* Buenas prácticas aplicadas (remote state, workspaces, etc.)

# FALTA 

---

## 5. Patrones de Diseño

### 📌 Descripción General

Lista y explicación de los patrones aplicados en el sistema.

### ✅ Patrones Implementados

#### Resiliencia

# FALTA 

#### Configuración

* **External Configuration**: Implementado en Kubernetes y aplicado también a `favourite-service` sin necesidad de modificar código fuente.

> **Ejemplo de External Configuration en Java:**

```java
public static final String USER_SERVICE_HOST = "http://USER-SERVICE/user-service";
```

#### Otros posibles patrones

* Service Discovery
* API Gateway
* Adapter Pattern
* Strategy Pattern

# OPCIONAL PONER LO DE ABAJO 

> ✍️ **Documentar para cada uno**:

* Qué patrón se implementó
* Propósito y beneficios
* Servicios afectados
* Código ejemplo o enlace a módulo relevante

---

## 6. CI/CD Avanzado

### 📌 Descripción

* Herramientas utilizadas (GitHub Actions, GitLab CI, Jenkins, etc.)
* Pipelines para cada tipo de entorno (dev, staging, prod)
* Integración con testing y despliegues automáticos
* Validaciones previas (lint, scan, test)

# FALTA 

---

## 7. Pruebas del Proyecto

### 📌 Tipos de pruebas

* ✅ **Unitarias**: Detallar qué servicios fueron cubiertos y qué frameworks se usaron (JUnit, Mockito, etc.)
* ✅ **Integración**: Mencionar qué módulos interactúan y cómo se testea
* ✅ **E2E**: Describir herramientas (como Cypress, Selenium) y cobertura
* ✅ **Performance con Locust**: Explicar casos de prueba y métricas

PARA ESTAS PRUEBAS QUE YA FUERON IMPLEMENTADAS Y PROBADAS ANTERIORMENTE POR FAVOR MIRAR EL DOCUMENTO [Ver estrategia de ramas](./README-PROJECT-DOCUMENTACION.md)]


* ❌ **Seguridad (OWASP)**: **No implementadas aún**, dejar como pendiente o en backlog

### 📊 Informes de Cobertura

* Herramientas utilizadas (JaCoCo, SonarQube, etc.)
* Porcentaje de cobertura general y por módulo

# FALTA 

---

## 8. Gestión de Cambios y Release Notes

### 📌 Proceso de Change Management

* ¿Cómo se aprueban los cambios?
* ¿Quién autoriza qué y cuándo?
* Herramientas utilizadas (JIRA, Confluence, etc.)
# FALTA 

### 📌 Planes de Rollback

* Estrategias definidas para revertir versiones
* Automatización (si aplica)

# FALTA 

---

## 9. Observabilidad y Monitoreo

### 📌 Stack de Observabilidad

#### Prometheus y Grafana

* Integración con servicios
* Dashboards por servicio
* Métricas recolectadas

# FALTA 

#### ELK Stack (Elasticsearch, Logstash, Kibana)

* Configuración por entorno
* Retención de logs
* Dashboards creados

# FALTA 

#### Dashboards relevantes

* Visualización de métricas clave (latencia, errores, tráfico, etc.)

# FALTA 

#### Alertas

* Reglas configuradas
* Notificaciones por canal (Slack, email, etc.)

# FALTA 

#### Tracing distribuido

* Herramienta utilizada (Jaeger, Zipkin)
* Ejemplo de trazabilidad completa de una transacción

# FALTA 

#### Health Checks

* Configuración de liveness y readiness probes en Kubernetes

# FALTA 

#### Métricas de negocio

* Tiempos de compra, conversiones, órdenes por día, etc.
* Cómo se recolectan y visualizan

# FALTA 

---

## 10. Seguridad

### 📌 Escaneo de vulnerabilidades

* Herramientas utilizadas (Dependabot, Snyk, Trivy, etc.)
* Integración en el pipeline

# FALTA 

### 📌 Gestión segura de secretos

* Uso de herramientas como Vault, AWS Secrets Manager o Sealed Secrets
* Evitar exposición en repositorios

# FALTA 

### 📌 Control de acceso (RBAC)

* Roles definidos
* Qué acceso tiene cada uno en Kubernetes o en la nube

# FALTA 

### 📌 TLS y tráfico seguro

* Certificados generados (Let’s Encrypt, etc.)
* Configuración de ingress o API Gateway con HTTPS

# FALTA 

