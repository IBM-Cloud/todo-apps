#!/bin/bash

# update with your Bluemix information
URL=http://127.0.0.1:5984

#USERNAME=
#PASSWORD=

if [ -z $USERNAME ]; then 
curl -X DELETE $URL/bluemix-todo
curl -X PUT $URL/bluemix-todo
curl -X PUT $URL/bluemix-todo/_design/todolist --data-binary @design.json; else
curl -u $USERNAME:$PASSWORD -X DELETE $URL/bluemix-todo
curl -u $USERNAME:$PASSWORD -X PUT $URL/bluemix-todo
curl -u $USERNAME:$PASSWORD -X PUT $URL/bluemix-todo/_design/todolist --data-binary @design.json ; fi