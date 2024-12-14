FROM public.ecr.aws/amazoncorretto/amazoncorretto:23-amd64
EXPOSE 8899
COPY ./target/no-geeks-brewing-api.jar no-geeks-brewing-api.jar
ENTRYPOINT ["java","-jar","no-geeks-brewing-api.jar"]