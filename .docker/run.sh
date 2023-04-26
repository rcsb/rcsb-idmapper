#!/bin/bash

# elastic-apm requires java agent to be present in the local filesystem
#-javaagent:/app/elastic-apm-agent-1.32.0.jar \
readonly JDK_JAVA_OPTIONS="\
-Xmx58g"

# readonly USER="`whoami`"

#       -v `pwd`/elastic-apm-agent-1.32.0.jar:/app/elastic-apm-agent-1.32.0.jar:ro \
docker run --rm -d \
       --pull always \
       -p 8080:8080 \
       -p 7000:7000 \
       -e JDK_JAVA_OPTIONS="$JDK_JAVA_OPTIONS" \
       -e MONGODB_URI="mongodb://updater:w31teQuerie5@10.20.3.153:27017/dw?authSource=admin&connectTimeoutMS=3000000&socketTimeoutMS=3000000" \
       nexus3.rcsb.org/rcsb/rcsb-idmapper:latest