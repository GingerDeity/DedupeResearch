This is the readme for the tutorial. In your environment, run `./ksminit.sh`. This should output to console
```
500000
1
2
```
Let's see what happens when we run KSM in the system as-is. For this, you'll want 2 vertical windows as shown in the upcoming screenshot. 
  
Now KSM is ready to be used. In one tmux window, run `./testKSM` to allocate 100 mergeable pages, don't free the memory yet! In the other tmux window, run `./ksmstart.sh`, then `./ksmwatch.sh` to start and observe KSM levels. This is what mine looks like:  
![screenshot](../Images/testKSM.png)  
  
Once you're able to approximately replicate this, feel free to hit Crtl+C in the window running `./ksmwatch.sh`, then run `./ksmend.sh`. Now, the two fields to pay attention to in `ksmwatch` are `pages_shared` and `pages_sharing`. You'll notice in the screenshot that `pages_shared` is 1 and `pages_sharing` is 99, which means that one page has been deduplicated 99 times, which adds up to our 100 mergeable pages!  

Now, let's see what happens when we run `./testZERO`, which is nearly the same as `./testKSM`, except 50 of the pages it allocates are zero-pages. Here's my results:  
![screenshot](../Images/testZERO.png)  

Again, once you've approximately replicated this, hit Crtl+C in the window running `./ksmwatch.sh`, then run `./ksmend.sh` to end KSM. This time, there are three fields to pay attention to in `ksmwatch`: `ksm_zero_pages`, `pages_shared`, and `pages_sharing`. You'll notice in the screenshot that `ksm_zero_pages` is 50 (due to the 50 zero-pages we allocated), `pages_shared` is still 1, and `pages_sharing` is now 49!  

