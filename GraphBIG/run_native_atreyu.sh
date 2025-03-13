#!/bin/bash

# This code basically runs GraphBIG! This was originally designed by Muhammad Laghari, 
# and for my purposes all I needed to focus on was where the lines pertaining to 
# benchmarks, dir, dataset, and threadcount. As of writing this comment (3-13-25), 
# the script's pwd is /hdd0/atreyu/GraphBIG/benchmark/

ulimit -s unlimited
ulimit -f unlimited
ulimit -l unlimited
echo 0 > /proc/sys/kernel/nmi_watchdog
echo never > /sys/kernel/mm/transparent_hugepage/enabled
/mnt/data/scripts/set_max_freq.sh

# datagen-8_5-fb (~100GB) [98218880 kB]
# datagen-8_4-fb (~80GB) [79632512 kB]
# datagen-7_9-fb (~24GB) [25390340 kB]
# datagen-7_5-fb (~10GB) [10170564 kB]

#benchmarks=(bfs dc dfs graphcoloring kcore pagerank ) #ORIGINAL
#dir=(bench_BFS bench_degreeCentr bench_DFS bench_graphColoring bench_kCore bench_pageRank ) #ORIGINAL

threadcount=$1
benchmarks=(sssp)
dir=(bench_shortestPath)
dataset=(datagen-7_5-fb)

for i in "${!benchmarks[@]}";
do
    ulimit -s unlimited
    ulimit -l unlimited
    echo Starting "${benchmarks[$i]} native run"
    cd "${dir[$i]}"
    /usr/bin/time -v ./"${benchmarks[$i]}" --dataset /mnt/data/GraphBIG/dataset/${dataset} --separator '" "' --iterations 10 --threadnum $threadcount &> ../"${benchmarks[$i]}"_threads_${threadcount}_${dataset}_native.out
    sync; echo 3 > /proc/sys/vm/drop_caches
    sleep 10
    cd ../
done
 
echo 1 > /proc/sys/kernel/nmi_watchdog
echo "All done!!!"
#perf stat -I 2000 -e instructions,LLC-load-misses,LLC-store-misses,major-faults,minor-faults,faults,dtlb_load_misses.miss_causes_a_walk,cycles,cycle_activity.stalls_l3_miss
#rdtset -v -t 'l3=0x1;cpu=4' -t 'l3=0x2;cpu=5' -t 'l3=0x4;cpu=6' -t 'l3=0x8;cpu=7' -c 4,5,6,7
