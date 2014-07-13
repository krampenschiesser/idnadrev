#!/bin/bash
killall Xvfb
rm -f /tmp/.X99-lock
mkdir -p /tmp/xvfb_test
Xvfb :99 -fbdir /tmp/xvfb_test -ac -extension GLX -extension "Genic Events" &
export DISPLAY=:99.0
gradle clean build
