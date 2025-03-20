#!/bin/bash
# Sets up KSM-A2a, alter IMG, NUMCPUS, and MEMSIZE as needed
EXE=qemu-system-x86_64
IMG=QemuKVM/ubuntu20_80G.img
#CDROM=QemuKVM/CentOS-7-x86_64-Minimal-2009.iso
NUMCPUS=8
MEMSIZE=15G
#MEMSIZE=32G,slots=2,maxmem=42G
MONITOR='telnet:127.0.0.1:1235,server,nowait'
NET='user,hostfwd=tcp::10024-:22'
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
    -curses \
    -boot menu=on \
    -net ${NET} \
    -net nic \
    -cpu host 
