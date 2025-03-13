#!/bin/bash
# This code should be run from the perspective of the host machine. 
# This sets up one of 2 level-1 VMs, which I call KSM-B1. You'll have to 
# ssh into it using the connect-ksm-b.sh script!

EXE=qemu-system-x86_64
IMG=QemuKVM/IMGs/ubuntu18_250G_2.img
#CDROM=QemuKVM/CentOS-7-x86_64-Minimal-2009.iso
NUMCPUS=16
MEMSIZE=32G
#MEMSIZE=32G,slots=2,maxmem=42G
MONITOR='telnet:127.0.0.1:1237,server,nowait'
NET='user,hostfwd=tcp::10026-:22'

LOG=./running_ppcm.log
#HUGE_PAGES=/huge

date >> ${LOG}
# hostname >> ${LOG}
${EXE} \
    -hda ${IMG} \
    -m ${MEMSIZE} \
    -smp ${NUMCPUS} \
    -enable-kvm \
    -monitor ${MONITOR} \
    -display curses \
    -boot menu=on \
    -net ${NET} \
    -net nic \
    -cpu host 
