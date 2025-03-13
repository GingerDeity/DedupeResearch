#!/bin/bash
# Initializes KSM, vastly increases the default pages to scan before sleeping,
# sets KSM to not consider zero-pages in pages_sharing (zero-pages are instead included
# in the ksm_zero_pages field), and ensures KSM is in the 'terminated' state

echo 500000 | sudo tee /sys/kernel/mm/ksm/pages_to_scan
echo 1 | sudo tee /sys/kernel/mm/ksm/use_zero_pages 
echo 2 | sudo tee /sys/kernel/mm/ksm/run
