FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER qian.jiang@cesgroup.com.cn

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

RUN mkdir -p /archive-app-management

WORKDIR /archive-app-management

EXPOSE 10000

ADD ./cescloud-archive-services/archive-app-management/archive-app-management-service/target/archive-app-management-service.jar ./

ADD ./archive-gateway/wait-for-it.sh ./

CMD ./wait-for-it.sh -t 0 archive-gateway:9999 -- java -Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom -jar archive-app-management-service.jar
