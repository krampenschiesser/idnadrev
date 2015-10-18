#!/bin/bash
echo pass=test123
#keytool -list -keystore keystore.jks
keytool -selfcert -v -alias domain -validity 3650 -keystore keystore.jks
