#!/bin/bash

echo 500000 | sudo tee /sys/kernel/mm/ksm/pages_to_scan
echo 1 | sudo tee /sys/kernel/mm/ksm/use_zero_pages
