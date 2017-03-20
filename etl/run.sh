#!/usr/bin/env bash

javac -cp "lib/*" *.java
java -cp ".:lib/*" IoT
