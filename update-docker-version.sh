#!/bin/bash

docker build --no-cache -t "maxwai/skripte-server-backup:$1" .
docker build -t maxwai/skripte-server-backup:latest .
docker push "maxwai/skripte-server-backup:$1"
docker push maxwai/skripte-server-backup:latest