basePath=$1
profile=$2
if [ "$profile" = "prod"  ] || [ "$profile" = "dev"  ]
then
  sudo su
fi
jps | grep .*Application | awk '{print $1}' > $basePath/pid.out
while IFS= read -r pid; do
    echo "Killing $pid"
      kill -9 "$pid"
done < $basePath/pid.out
echo "Killed running services"
rm -rf $basePath/pid.out
cd $basePath/pct-microservices
git pull https://abi-pat:ExatipTechnologies@github.com/leafn0de/pct-microservices
echo "Pulled latest code from Git"
cd $basePath/pct-microservices/installer-service/frontend
echo "Building frontend now"
if [ "$profile" = "prod"  ]
then
  ng build --configuration=production
elif [ "$profile" = "dev"  ]
then
  ng build --configuration=dev
else
  ng build --configuration=dev
fi
echo "Frontend successfully built"
rm -rf $basePath/pct-microservices/installer-service/src/main/resources/static/
cp -R $basePath/pct-microservices/installer-service/frontend/dist/PCT/* $basepath/pct-microservices/installer-service/src/main/resources/static/
echo "Copied frontend build to static"
cd $basePath/pct-microservices
echo "Building services now"
mvn clean install -DskipTests
echo "Starting service now"
nohup java -jar discovery-service/target/discovery-service-0.0.1-SNAPSHOT.jar &
sleep 5s
nohup java -jar gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar &
sleep 5s
nohup java -jar -Dspring.profiles.active=$profile auth-service/target/auth-service-0.0.1-SNAPSHOT.jar &
sleep 5s
nohup java -jar -Dspring.profiles.active=$profile company-service/target/company-service-0.0.1-SNAPSHOT.jar &
sleep 5s
nohup java -jar -Dspring.profiles.active=$profile device-service/target/device-service-0.0.1-SNAPSHOT.jar &
sleep 5s
nohup java -jar -Dspring.profiles.active=$profile installer-service/target/installer-service-0.0.1-SNAPSHOT.jar &
sleep 5s
echo "All services started. Deployment Complete!"