#!/bin/sh
cd ../../
docker build -f docker/app/Dockerfile . -t spring
docker run --name Bachelorarbeit_Spring -v C:/dockerVolume:/mnt -p 8080:8080  -p 5432:5432 spring
