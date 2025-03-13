#!/bin/bash
# A simple utility function that clears the screen, prints out a header, and lists the contents of your current directory
# This is especially helpful when you are observing multiple VMs at a time and give each VM a distinct header! In fact,
# Each VM I use has a header.txt file that I use for this exact purpose

clear
layer0="     __  __     _____     ______     _______               _______ "
layer1="    / /\/ /\   / ___/\   / __  /\   / ___  /\             / _____/\ "
layer2="   / /_/ / /  / /\__\/  / /\/ / /  / /\_/ / /            / /\____\/ "
layer3="  / __  / /  / ___/\   / /_/ / /  / _____/ /  _____     / ___  /\  "
layer4=" / /\/ / /  / /\__\/  / __  / /  / /\____\/  /____/\   / /\_/ / / "
layer5="/_/ /_/ /  /____/\   /_/\/_/ /  /_/ /        \____\/  /______/ / "
layer6="\_\/\_\/   \____\/   \_\/\_\/   \_\/                  \______\/ "

echo "$layer0"
echo "$layer1"
echo "$layer2"
echo "$layer3"
echo "$layer4"
echo "$layer5"
echo "$layer6"

ls --color=auto
