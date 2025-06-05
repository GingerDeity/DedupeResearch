To intialize and connect via SSH into a VM, here's the general process:
  1) Create and attach to a tmux session using "tmux new -s name", "tmux a"
  2) In one window, call the correct setup-*.sh script
  3) In another window, call the respective connect-*.sh script
  4) Once the VM has finished booting, you'll be able to SSH in!


(Usernames, Passwords):
Heap6 = (atreyum, Heap@24060)
LVL-* VMs = (josh, admin)


VM tree structure:
         Host (Heap6)
        /            \
      KSM-A          KSM-B
     /      \       /     \
  A2a       A2b    B2a    B2b


MONITOR/NET Ports:
Host (Heap6)
|- KSM-A=10023/1235
|  |- KSM-A2a=10024/1235
|  |- KSM-A2b=10025/1236
|
|- KSM-B=10026/1237
|  |- KSM-B2a=10027/1237
|  |- KSM-B2b=10028/1238
