#!/bin/sh
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APPDIR="`pwd -P`"
cd "$SAVED" >/dev/null

if [ -f "$APPDIR/.mvn_bin/apache-maven-3.9.9/bin/mvn" ]; then
  exec "$APPDIR/.mvn_bin/apache-maven-3.9.9/bin/mvn" "$@"
else
  exec mvn "$@"
fi
