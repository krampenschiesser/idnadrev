#!/bin/bash
if [ ! -f /tmp/.X99-lock ]
then
    mkdir -p /tmp/xvfb_test
    Xvfb :99 -fbdir /tmp/xvfb_test -ac -extension GLX -extension "Generic Events" &
fi
export DISPLAY=:99.0
gradle clean build
