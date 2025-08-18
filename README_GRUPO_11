
## ¿Por qué el grupo escogió este repositorio?

#¿Cúal es la utilidad que tiene Zap?
Su utilidad se encuentra en la automatización de la detección de vulnerabilidades emergentes como inyección SQL, XSS y configuraciones inseguras, mientras se desarrollan o testean las aplicaciones. 
Además, ofrece funciones robustas para análisis manual gracias a su proxy interceptador, proporcionando flexibilidad y profundidad en el testeo de seguridad.
Su modelo colaborativo como parte de la comunidad OWASP y su presencia en GitHub como un proyecto activo y accesible para contribuciones permiten mantener una evolución continua del código. Esto lo convierte en una herramienta técnica eficaz, tanto en entornos educativos como en equipos profesionales de seguridad que buscan una solución flexible, extendible y con respaldo comunitario.
(https://github.com/zaproxy/zaproxy/blob/main/README.md)

# ¿Por qué el grupo escogió este repositorio?

El grupo seleccionó el repositorio Zaproxy (Zed Attack Proxy) porque constituye una herramienta madura y reconocida en el ámbito de la ciberseguridad, especialmente en pruebas de aplicaciones web. ZAP se distingue por su capacidad de realizar escaneos activos y pasivos, lo que permite identificar vulnerabilidades críticas como inyección SQL, cross-site scripting (XSS) y fallos de control de acceso. 

Estas capacidades se alinean directamente con los riesgos descritos en el OWASP Top 10, uno de los estándares más relevantes para la industria del software seguro (OWASP, 2021). Su uso en múltiples proyectos de seguridad y su continua evolución respaldan su elección como objeto de estudio.

Asimismo, el proyecto se ajusta a tendencias modernas en DevSecOps al ofrecer integración mediante API REST, compatibilidad con Docker y automatización en pipelines CI/CD. Estos atributos lo convierten en una herramienta flexible, capaz de incorporarse de forma ágil en entornos de desarrollo que requieren seguridad continua. La incorporación de pruebas de seguridad dinámicas (DAST) en fases tempranas del ciclo de vida del software es esencial para reducir costes y riesgos, lo que refuerza la pertinencia de ZAP como repositorio de referencia para el trabajo.

Finalmente, la selección de este repositorio se justifica también en la validación académica e industrial que ha recibido. Investigaciones recientes como Benchmarking OWASP ZAP v2.12.0 vs v2.13.0 (Dumont et al., 2022) demuestran mejoras en la capacidad de detección de vulnerabilidades, mientras que estudios como XSS for the Masses (Dumont et al., 2022) evidencian su utilidad en entornos educativos al reducir fallos de seguridad en proyectos estudiantiles.

Dumont, T., Fong, E., & Seitz, J. (2022). XSS for the Masses: Integrating Web Security into Programming Courses. arXiv.(https://arxiv.org/abs/2204.12416)

Li, X., Wang, J., & Patel, R. (2025). Benchmarking OWASP ZAP v2.12.0 vs v2.13.0. arXiv. (https://arxiv.org/abs/2501.05907)

OWASP. (2021). OWASP Top 10: The Ten Most Critical Web Application Security Risks. OWASP Foundation. (https://owasp.org/Top10)

### Proyecto OWASP ZAP – Dev Build

Este proyecto documenta el proceso de levantamiento, configuración y ejecución de OWASP ZAP desde el código fuente utilizando Gradle, incluyendo la instalación de complementos y la ejecución de un escaneo automatizado sobre una aplicación web de prueba.

## 📌 Objetivo

Compilar OWASP ZAP desde el código fuente con Gradle.
Instalar complementos necesarios para la ejecución.
Ejecutar un escaneo automatizado de un sitio de prueba.
Documentar el proceso y resultados obtenidos.

## 🛠️ Preparación del Entorno

Instalar JDK 11 o 17 (Eclipse Adoptium recomendado).
Configurar la variable de entorno JAVA_HOME:
```
C:\Program Files\Eclipse Adoptium\jdk-11.0.28.6-hotspot\bin
```

## 🔹 Paso 2 – Compilación con Gradle

En la raíz del proyecto se ejecutó:
```
.\gradlew :zap:run -x test
```

Inicialmente, el build fallaba por falta del add-on callhome.
Se solucionó instalando los complementos requeridos desde el Marketplace de ZAP.

## 🔹 Paso 3 – Instalación de Complementos

Desde el menú Herramientas → Administrar Complementos, se instalaron los módulos esenciales:
```
callhome
spider
ajax spider
report generation
```
Otros módulos recomendados.
Con esto el proyecto pudo iniciarse correctamente.

### Ejecución del Escaneo Automatizado

Ir a la pestaña Inicio Rápido.

Ingresar la URL a atacar:

http://pescalimentos.com


## Configurar:

Spider tradicional: ✅

Spider AJAX: Sí es Moderno con navegador Edge.

Presionar Atacar.

📸 Captura 2 – Configuración de escaneo automatizado


## 🔍 Progreso del Escaneo

El spider recorrió el sitio y detectó:

612 URLs encontradas

473 nodos ingresados

Se procesaron múltiples recursos externos (GitHub, YouTube, CDNs, etc.).

📸 Captura 3 – Escaneo en progreso


### 📊 Resultados

Se generaron múltiples peticiones HTTP registradas.

Sitio procesado con spider tradicional + AJAX.

Alertas y posibles vulnerabilidades listadas en panel de resultados.

📸 Captura 4 – Resultados del escaneo y tráfico HTTP


### 📤 Exportar Informe

Ir a Informe → Generar Informe.

Seleccionar formato deseado (HTML recomendado).

Guardar archivo para documentación.

### ✅ Conclusión

OWASP ZAP fue levantado exitosamente desde código fuente con Gradle.
Se resolvieron dependencias faltantes instalando complementos.
Se ejecutó un escaneo automatizado contra un sitio de prueba.
Se documentaron los resultados con capturas e instrucciones claras.