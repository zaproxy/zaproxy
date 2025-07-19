# readme_GRUPO_9.md

## 🔍 Resumen del proyecto seleccionado: OWASP ZAP

OWASP ZAP (Zed Attack Proxy) es una herramienta de código abierto para la evaluación de seguridad de aplicaciones web. Desarrollada por la Fundación OWASP (Open Web Application Security Project), ZAP actúa como un proxy que intercepta el tráfico HTTP/HTTPS entre el navegador y la aplicación web, permitiendo identificar vulnerabilidades tales como inyecciones SQL, XSS (Cross-Site Scripting), fallas de autenticación, exposiciones de datos sensibles, entre otras.

ZAP ofrece escaneos tanto automáticos como manuales, lo que permite su uso por parte de expertos en ciberseguridad y estudiantes en formación. Incluye funcionalidades como spiders, fuzzers, escaneo pasivo, generación de informes y una API REST que permite su integración en entornos de desarrollo continuo (CI/CD).

La herramienta es multiplataforma, tiene interfaz gráfica, línea de comandos y es extensible mediante complementos, lo que facilita su adaptación a diferentes entornos de pruebas de penetración.

---

## 🎯 Justificación de la elección del repositorio

Como grupo decidimos trabajar con **OWASP ZAP** por las siguientes razones técnicas:

1. **Reconocimiento internacional y respaldo académico**  
   ZAP es una herramienta respaldada por la Fundación OWASP, organización reconocida a nivel mundial en el ámbito de la seguridad informática. Según el *OWASP Web Security Testing Guide* (2023), ZAP es una de las principales herramientas recomendadas para realizar pruebas de seguridad a aplicaciones web (OWASP, 2023).

2. **Utilización en entornos profesionales y educativos**  
   ZAP es ampliamente utilizada en cursos universitarios, bootcamps y certificaciones como CEH (Certified Ethical Hacker) y OSCP (Offensive Security Certified Professional). Weidman (2014), en su libro *Penetration Testing*, menciona ZAP como una de las herramientas fundamentales para iniciarse en las pruebas de seguridad web.

3. **Soporte técnico y comunidad activa**  
   La herramienta cuenta con documentación oficial extensa, tutoriales, foros y actualizaciones constantes en su [repositorio oficial de GitHub](https://github.com/zaproxy/zaproxy), lo que garantiza su vigencia y aplicabilidad.

4. **Facilidad de uso e integración**  
   A diferencia de herramientas más complejas como Burp Suite o Metasploit, ZAP ofrece una curva de aprendizaje más amigable. Además, permite automatizar escaneos mediante su API, lo que la hace ideal para integrarse a pipelines de desarrollo seguro (DevSecOps).

5. **Enfoque en vulnerabilidades críticas**  
   ZAP está alineada con el top 10 de OWASP, abordando vulnerabilidades como inyecciones, control de acceso roto, exposición de datos sensibles y más. Esto refuerza su utilidad en entornos reales de desarrollo seguro.

---

## 📚 Referencias

- OWASP (2023). *OWASP Web Security Testing Guide (WSTG)*. Recuperado de: https://owasp.org/www-project-web-security-testing-guide/
- Weidman, G. (2014). *Penetration Testing: A Hands-On Introduction to Hacking*. No Starch Press.
- ZAP Project Documentation. https://www.zaproxy.org/docs/
- OWASP ZAP GitHub Repository: https://github.com/zaproxy/zaproxy
- Security Boulevard. (2022). "Top Open Source Security Testing Tools for 2022". Recuperado de: https://securityboulevard.com/2022/03/top-open-source-security-testing-tools-for-2022/
