#!/bin/bash
# Tells KSM to run! 

echo 1 | sudo tee /sys/kernel/mm/ksm/run
