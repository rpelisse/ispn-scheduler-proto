#!/bin/bash

#export SCALA_OPTS="-J-Xdebug -J-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"
export SCALA_OPTS="-J-Dinfinispan.debugDependencies=true -J-Dactors.corePoolSize=60 -J-Dactors.maxPoolSize=5000"

export SCALA_HOME="${HOME}/Products/tools/scala-2.10.3/"
export INFINISPAN_HOME="${INFINISPAN_HOME:-"${HOME}/Repositories/perso/articles/query-ispn.git/infinispan-6.0.0.Final-all/"}"

for jar in ${INFINISPAN_HOME}/lib/*.jar
do
  if [ $(echo "${jar}" | grep -e 'scala' -c ) -eq 0 ]; then
    CLASSPATH=${CLASSPATH}:${jar}
  fi
done
export CLASSPATH=.:${CLASSPATH}
${SCALA_HOME}/bin/scala ${SCALA_OPTS} -i "$@"
