# Based on Dockerfile by Mahdi Lazraq

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    unzip \
    xvfb \
    libxrender1 \
    libxtst6 \
    libxi6 \
    && rm -rf /var/lib/apt/lists/*

ADD https://github.com/jindrapetrik/jpexs-decompiler/releases/download/version25.1.2/ffdec_25.1.2.zip /opt/ffdec.zip
RUN cd /opt && unzip ffdec.zip -d ffdec && rm ffdec.zip

WORKDIR /work

ENTRYPOINT ["java", "-jar", "/opt/ffdec/ffdec.jar"]

