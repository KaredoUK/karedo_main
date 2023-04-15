#!/usr/bin/env bash
export SBT=sbt/bin/sbt
$SBT clean compile karedo_routes/assembly
karedo_feeder/bin/activator dist
