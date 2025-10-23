# 🚀 tool-sharepoint-restserver

**Conector backend Java Spring Boot para integración entre SDx y WhereOil**  
Automatiza operaciones, pruebas de conectividad y consumo de servicios REST.

---

## 📋 Tabla de Contenidos

- [📝 Descripción](#-descripción)
- [🛠️ Tecnologías](#-tecnologías)
- [⚙️ Requisitos previos](#-requisitos-previos)
- [⬇️ Instalación](#️-instalación)
- [📦 Instalación de dependencias locales](#-instalación-de-dependencias-locales)
- [▶️ Ejecución](#️-ejecución)
- [🗂️ Estructura del proyecto](#️-estructura-del-proyecto)
- [💡 Comandos útiles](#-comandos-útiles)
- [🔖 Notas y consideraciones](#-notas-y-consideraciones)
- [📄 Licencia](#-licencia)

---

## 📝 Descripción

**tool-sharepoint-restserver** es un conector backend en Java con Spring Boot, diseñado para facilitar la integración y automatización entre los sistemas SDx y WhereOil.  
Ideal para pruebas de conectividad, autenticación y consumo de servicios REST personalizados.

---

## 🛠️ Tecnologías

- **Java 11**
- **Spring Boot 2.0.4**
- **Maven**
- Dependencias adicionales (ver [`pom.xml`](tool-sharepoint-restserver/pom.xml))

---

## ⚙️ Requisitos previos

- [Java 11+](https://adoptium.net/)
- [Maven 3.6+](https://maven.apache.org/)
- (Opcional) IDE Java (IntelliJ IDEA, Eclipse, VSCode)
- Acceso y credenciales válidas para SDx y WhereOil

---

## ⬇️ Instalación

1. **Clona el repositorio**
    ```bash
    git clone https://github.com/XXXXXX/tool-sharepoint-restserver.git
    cd tool-sharepoint-restserver
    ```

2. **Instala las dependencias Maven**
    ```bash
    mvn clean install
    ```

3. **Configura el archivo de propiedades**
    - Edita `src/main/resources/application.properties` con tus endpoints y credenciales.

---

## 📦 Instalación de dependencias locales

> **Importante:**  
> Este proyecto usa varios JARs privados/locales.  
> Debes instalarlos manualmente en tu repositorio Maven local.

1. **Guarda el siguiente script como `install_libs.sh` en la raíz del proyecto:**

    ```bash
    #!/bin/bash

    # Instalar JARs locales en el repositorio Maven local

    # whereoil-tool-common
    mvn install:install-file \
      -Dfile=lib/whereoil-tool-common-5.34.jar \
      -DgroupId=com.kadme \
      -DartifactId=whereoil-tool-common \
      -Dversion=5.34 \
      -Dpackaging=jar

    # whereoil-rest-client
    mvn install:install-file \
      -Dfile=lib/whereoil-rest-client-5.2.0-fs.jar \
      -DgroupId=com.kadme \
      -DartifactId=whereoil-rest-client \
      -Dversion=5.2.0-fs \
      -Dpackaging=jar

    # whereoil-tool-utils
    mvn install:install-file \
      -Dfile=lib/whereoil-tool-utils-5.25.jar \
      -DgroupId=com.kadme \
      -DartifactId=whereoil-tool-utils \
      -Dversion=5.25 \
      -Dpackaging=jar

    # whereoil-rest-client-common
    mvn install:install-file \
      -Dfile=lib/whereoil-rest-client-common-5.2.0-fs.jar \
      -DgroupId=com.kadme \
      -DartifactId=whereoil-rest-client-common \
      -Dversion=5.2.0-fs \
      -Dpackaging=jar

    # whereoil-model
    mvn install:install-file \
      -Dfile=lib/whereoil-model-5.2.0-fs.jar \
      -DgroupId=com.kadme \
      -DartifactId=whereoil-model \
      -Dversion=5.2.0-fs \
      -Dpackaging=jar      

    # api-all (org.alfresco)
    mvn install:install-file \
      -Dfile=lib/api-all-2.0.0.jar \
      -DgroupId=org.alfresco \
      -DartifactId=api-all \
      -Dversion=2.0.0 \
      -Dpackaging=jar

    # content-model
    mvn install:install-file \
      -Dfile=lib/content-model-1.0.jar \
      -DgroupId=com.kadme \
      -DartifactId=content-model \
      -Dversion=1.0 \
      -Dpackaging=jar

    # htmllexer
    mvn install:install-file \
      -Dfile=lib/htmllexer-2.1.jar \
      -DgroupId=net.htmlparser.jericho \
      -DartifactId=htmllexer \
      -Dversion=2.1 \
      -Dpackaging=jar

    # htmlparser
    mvn install:install-file \
      -Dfile=lib/htmlparser-2.1.jar \
      -DgroupId=net.htmlparser.jericho \
      -DartifactId=htmlparser \
      -Dversion=2.1 \
      -Dpackaging=jar

    # ont-kmeta
    mvn install:install-file \
      -Dfile=lib/ont-kmeta-2.22.jar \
      -DgroupId=com.kadme \
      -DartifactId=ont-kmeta \
      -Dversion=2.22 \
      -Dpackaging=jar

    # ont-wrslog
    mvn install:install-file \
      -Dfile=lib/ont-wrslog-4.0.jar \
      -DgroupId=com.kadme \
      -DartifactId=ont-wrslog \
      -Dversion=4.0 \
      -Dpackaging=jar

    # ont-wrstools
    mvn install:install-file \
      -Dfile=lib/ont-wrstools-4.4.jar \
      -DgroupId=com.kadme \
      -DartifactId=ont-wrstools \
      -Dversion=4.4 \
      -Dpackaging=jar

    echo "Todos los JARs han sido instalados localmente en Maven."
    ```

2. **Dale permisos de ejecución y ejecútalo:**

    ```bash
    chmod +x install_libs.sh
    ./install_libs.sh
    ```

---

## ▶️ Ejecución

- **Ejecuta la aplicación localmente**
    ```bash
    mvn spring-boot:run
    ```
    o genera el `.jar` y ejecútalo:
    ```bash
    mvn package
    java -jar target/tool-sharepoint-restserver-<versión>.jar
    ```

- **Verifica los logs** para confirmar conexión con SDx y WhereOil.

---

## 🗂️ Estructura del proyecto

```shell
tool-sharepoint-restserver/
├── install_libs.sh
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/        # Código fuente Java
│   │   └── resources/   # Configuración y propiedades
│   └── test/            # Pruebas unitarias
├── lib/                 # Dependencias locales adicionales (.jar)
├── target/              # Archivos generados (build)
