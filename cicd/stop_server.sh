#!/bin/bash

systemctl stop discovery gateway auth device organisation device-command installer device-version
systemctl stop tomcat
sleep 30s
