#!/bin/bash

systemctl start discovery gateway auth device organisation device-command installer device-version
systemctl start tomcat
sleep 30s
