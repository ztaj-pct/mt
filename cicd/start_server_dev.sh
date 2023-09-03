
#!/bin/bash

systemctl start discovery gateway auth device organisation installer
systemctl start tomcat
sleep 30s
