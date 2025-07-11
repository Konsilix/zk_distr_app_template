#
# Dockerfile for my Zookeeper application
#
# docker build -t theApp:v1 .
ARG BASE_IMAGE=3.8.4
FROM zookeeper:$BASE_IMAGE
ENV APP="theApp"

RUN /bin/rm -rf /opt

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys A122542AB04F24E3

RUN apt-get update && apt-get update -y

RUN apt-get install gpg

RUN wget -O - https://apt.corretto.aws/corretto.key | gpg --dearmor -o /usr/share/keyrings/corretto-keyring.gpg && echo "deb [signed-by=/usr/share/keyrings/corretto-keyring.gpg] https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list

#RUN echo "deb https://apt.corretto.aws stable main" | tee /etc/apt/sources.list.d/corretto.list

RUN apt-get update; apt-get install -y java-21-amazon-corretto-jdk

# TODO: remove vim, dnsutils, dos2unix for production
RUN apt-get install -y dnsutils net-tools traceroute iputils-ping dos2unix vim

#RUN apt-get autoremove -y

# Copy the application into the container
# COPY $APP-0.0.1-SNAPSHOT.jar /$APP.jar

# Zookeeper configuration
COPY zoo.cfg /conf/zoo.cfg

# Copy the entrypoint script into the container
COPY $APP-entrypoint.sh /$APP-entrypoint.sh
RUN dos2unix /$APP-entrypoint.sh
# Set the entrypoint script to be executable
RUN chmod +x /$APP-entrypoint.sh

# Remove unnecessary packages to increase security and reduce the size of the image
#RUN apt-get remove -y --purge \
#    gpg \
#    wget \
#    apt-transport-https \
#    ca-certificates \
#    gnupg \
#    software-properties-common \
#    dnsutils \
#    dos2unix \
#    && apt-get autoremove -y

# port for ZK admin
EXPOSE 8080
# port for app
EXPOSE 8081
# port for ZK protocol
EXPOSE 2181

# port for spring boot app
EXPOSE 3000
# port for spring boot mgmt
EXPOSE 3001

ENV JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the entrypoint script to be the entrypoint for the container
ENTRYPOINT ["/${APP}-entrypoint.sh"]

# Set the default command to be the Zookeeper application
#CMD ["java", "-jar", "/zkApp.jar"]
#CMD ["/bin/bash"]
#CMD ["/bin/bash","-c","/theApp-entrypoint.sh"]
