These files are used for testing deduplication on processes that use a JVM.

jvm1
- A simple hello world program followed up with an infinite loop so you have time
  to call gcore and whatnot.

jvm2
- Code that stresses the heap by spawning multiple objects all contianing the same data.
  It also calls gcore on itself.

jvm3
- A copy of my DedupeCheck code, simple as that