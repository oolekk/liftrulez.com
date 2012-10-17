#! /bin/sh
# by: olekswirski@gmail.com
# This script quickly updates jar file initially created by
# assembly sbt plugin. Most of that jar contents are libs
# which do not change most of the time. Assembly takes long
# time to run. Use 7z instead to quickly update frequently
# changing files within jar without the need for full jar
# recreation. Indicate which files to update with commandline
# options like so:
USAGE="Usage: `basename $0` [-vpbcwhsj]\n
-p -> update props/*\n
-b -> update bootstrap/*\n
-c -> update top package classes\n
-w -> update webapp/*\n
-h -> update webapp/*.html\n
-s -> update webapp/*.css\n
-j -> update webapp/*.js\n"
# WARN: You must have 7z installed and a jar generated
# by assembly exist in the target directory.

PATH=/bin:/usr/bin
# important -> set this up
SCALA_VERSION=2.9.2
# dir name is assumed to be project name

PROJECT_DIR=$(pwd)
#infere project name from dir name
PROJECT_NAME=${PROJECT_DIR##*/}
echo PROJECT_NAME=$PROJECT_NAME
TOP_PACKAGE=${PROJECT_NAME##*.}
echo TOP_PACKAGE=$TOP_PACKAGE
TARGET_DIR=$PROJECT_DIR/target
JAR_NAME=$PROJECT_NAME-lift.jar
echo JAR_NAME=$JAR_NAME
JAR_FILE=$TARGET_DIR/$JAR_NAME

if [ ! -r "$JAR_FILE" ] ;then
  echo Could not read $JAR_FILE
  exit 1
fi

CLASSES_DIR=$TARGET_DIR/scala-$SCALA_VERSION/classes
MAIN_DIR=$PROJECT_DIR/src/main
WEBAPP_DIR=$MAIN_DIR/webapp
RESOURCES_DIR=$MAIN_DIR/resources
PROPS_DIR=$RESOURCES_DIR/props

jarProps() {
  echo About to jar props ...
  cd "$RESOURCES_DIR"
  7z a -r "$JAR_FILE" "props"
}

jarBoot() {
  echo About to jar classes ...
  cd "$CLASSES_DIR"
  7z a -r "$JAR_FILE" "bootstrap"
}

jarClasses() {
  echo About to jar classes ...
  cd "$CLASSES_DIR"
  7z a -r "$JAR_FILE" "$TOP_PACKAGE"
}

jarWebapp() {
  echo About to jar webapp ...
  cd "$MAIN_DIR"
  7z a -r "$JAR_FILE" "webapp"
  exit
}

jarStyles() {
  echo About to jar styles ...
  cd "$MAIN_DIR"
  7z a -r "$JAR_FILE" "webapp/**.css"
}

jarJS() {
  echo About to jar JS ...
  cd "$MAIN_DIR"
  7z a -r "$JAR_FILE" "webapp/**.js"
}

jarHTML() {
  echo About to jar HTML ...
  cd "$MAIN_DIR"
  7z a -r "$JAR_FILE" "webapp/**.html"
}

# Parse command line options.
while getopts :vpbcwhsj OPT; do
  case "$OPT" in
    v)
      echo "`basename $0` version 0.1"
      exit 0
      ;;
    p)
      jarProps
      ;;
    b)
      jarBoot
      ;;
    c)
      jarClasses
      ;;
    w)
      jarWebapp
      ;;
    h)
      jarHTML
      ;;
    s)
      jarStyles
      ;;
    j)
      jarJS
      ;;
    \?)
      # getopts issues an error message
      echo -n  $USAGE >&2
      exit 1
      ;;
  esac
done

# EOF
