#!/usr/bin/env bash
echo "Building microservice JARs through the modules"
echo ""
cd ../
mvn install

echo ""

echo "Copying fat JARs to be Dockerized"

echo ""

echo "Copying project-006 fat JAR to project folder"
cd project-006
cp target/project-006-1.0-SNAPSHOT.jar .
echo "Ok"

echo ""

echo "Copying project-007 fat JAR to project folder"
cd ../project-007
cp target/project-007-1.0-SNAPSHOT.jar .
echo "Ok"

echo ""

echo "Deploying microservices to Kubernetes through the YAMLs"

echo ""

echo "Deploying the hello microservice application"
cd ../yaml
kubectl apply -f .


echo ""

echo "Well done!"