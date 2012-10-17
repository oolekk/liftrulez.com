#! /bin/sh
PATH=/bin:/usr/bin
SCALA_VERSION=2.9.2

USAGE="Usage: `basename $0` [-hv] [-o arg] args"

UTIL_DIR=$(pwd)
PROJECT_DIR=${UTIL_DIR%/*}
PROJECT_NAME=${PROJECT_DIR##*/}
echo PROJECT_NAME=$PROJECT_NAME
TARGET_DIR=$PROJECT_DIR/target
JAR_NAME=$PROJECT_NAME-lift.jar
echo JAR_NAME=$JAR_NAME
JAR_FILE=$TARGET_DIR/$JAR_NAME

if [ ! -r "$JAR_FILE" ] ;then
  echo Could not read $JAR_FILE
  return 1
fi

CLASSES_DIR=$TARGET_DIR/scala-$SCALA_VERSION/classes
MAIN_DIR=$PROJECT_DIR/src/main
WEBAPP_DIR=$MAIN_DIR/webapp
RESOURCES_DIR=$MAIN_DIR/resources
PROPS_DIR=$RESOURCES_DIR/props

jarProps() {
  echo About to jar props ...
  cd "$RESOURCES_DIR"
  $(zip -r "$JAR_FILE" props)
}

jarClasses() {
  echo About to jar classes ...
}

jarWebapp() {
  echo About to jar webapp ...
  exit
}

jarStyles() {
  echo About to jar styles ...
}

jarJS() {
  echo About to jar JS ...
}

# Parse command line options.
while getopts hvpcwsj OPT; do
  case "$OPT" in
    h)
      echo $USAGE
      exit 0
      ;;
    v)
      echo "`basename $0` version 0.1"
      exit 0
      ;;
    p)
      jarProps
      ;;
    c)
      jarClasses
      ;;
    w)
      jarWebapp
      ;;
    s)
      jarStyles
      ;;
    j)
      jarJS
      ;;
    \?)
      # getopts issues an error message
      echo $USAGE >&2
      exit 1
      ;;
  esac
done

# Remove the switches we parsed above.
shift `expr $OPTIND - 1`

# We want at least one non-option argument. 
# Remove this block if you don't need it.
#if [ $# -eq 0 ]; then
#  echo $USAGE >&2
#  exit 1
#fi

# Access additional arguments as usual through 
# variables $@, $*, $1, $2, etc. or using this loop:
for PARAM; do
  echo $PARAM
done

#cd ..
#PROJECT_DIR=$(pwd)

# [ -r $PROJECT_DIR/target/$JAR_NAME ] && echo jar in

#. $UTIL_DIR/boot-merge-jar.sh
#. $UTIL_DIR/class-merge-jar.sh
#. $UTIL_DIR/web-merge-jar.sh

# EOF
