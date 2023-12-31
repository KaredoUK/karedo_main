#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"

while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done

DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cd $DIR

./make_binaries.sh

kill $(ps aux | grep 'routes-assembly-0.0.2-SNAPSHOT.jar' | awk '{print $2}')


nohup java -jar karedo_routes/target/scala-2.11/routes-assembly-0.0.2-SNAPSHOT.jar &

