#!/bin/bash
# A simple utility function, lets you more quickly identify certain processes under ps aux
ps aux | grep "$1"
