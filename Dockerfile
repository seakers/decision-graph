#######################
### Final Container ###
#######################


FROM amazoncorretto:11
# COPY --from=Schema /schema/schema.json /app/src/main/graphql/com/evaluator/schema.json
# COPY --from=BUILD_TOOL /root/.m2 /root/.m2




# -- DEPS --
WORKDIR /installs

RUN yum update -y && \
    yum upgrade -y && \
    yum install git wget unzip tar -y

# -- GRADLE --
RUN wget https://services.gradle.org/distributions/gradle-7.4-bin.zip && \
    unzip gradle-7.4-bin.zip && \
    rm gradle-7.4-bin.zip
ENV PATH="/installs/gradle-7.4/bin:${PATH}"

# -- WORKING DIRECTORY --
WORKDIR /decisions

