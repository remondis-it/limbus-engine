#!/bin/sh
if [ $# -eq 0 ]
  then
    echo "Please specify the PID of the Java process you want to create a heap dump from!"
	exit 1
fi
/usr/lib/jvm/jdk1.8.0_51/bin/jmap -dump:format=b,file=engine-heap.hprof $1