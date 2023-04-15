#!/usr/bin/env bash
export SBT="java -jar sbt/bin/sbt-launch.jar"
$SBT clean compile karedo_routes/assembly
karedo_feeder/bin/activator dist
