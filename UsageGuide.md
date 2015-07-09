This page is a bit outdated. Will be updated soon.

## General Usage Guide ##

jCores works best as a static import. Do it like this:

```
import static net.jcores.CoreKeeper.$;
```


## Code Examples ##

In here you will find a number of example what you can do with the API. Please note that most of these calls are executed in parallel, giving you significant speed ups for large operations. Calls to small cores (containing only a single object) are executed directly.

```

// Log a string to syslog 
$("Hello world").log();


// Filter all words that don't contain "ish" and print them
$("documentation/TODO.txt").file().text().split("\n").split(" ").filter(".*ish.*").print();


// Create an executor that works parallel on multiple instances
F0 f[] = new F0[] { new F0Impl(), new F0Impl(), new F0Impl()};
F0 f0 = $(f).each(F0.class)
f0.f()


// Load plugins and execute their functions
$(f).as(ExtensionCore.class).yoyo();
$(f).as(CoreSerializer.class).store("test.xml");


// Register an implementor for given interface
$(F0.class).implementor(F0Impl.class);
for(int i = 0; i< 100000; i++) {
    // And create lots of instances
    F0 spawn = $(F0.class).spawn();
}

       
// Map strings in to list of strings in parallel and expand them.
CoreObject<String> expand = $("hello world", "how are you").map(new F1<String, String[]>() {
    public String[] f(String x) {
        return x.split(" ");
    }
}).expand(String.class).as(CoreString.class);


// Delete two files and re-create them with new content
$("debug1.txt", "debug2.txt").file().delete().append("Back again");
  
  
// Print what went wrong the previous operations
$.report();
```



## Benchmarks ##

Benchmarking Java code is tricky. While the numbers we give down here should avoid the most serious flaws, the methodology by which they were obtained was by no means perfect. They should, however, give a rough estimate of the order of magnitude of the call overhead and possible speedup in some situations.

All benchmarks were performed on a MBP 2.4 GHz, Java 6.20 (x64). Each function was executed on an array size of 100000, ten times. The timings given were the results of the tenth pass. You can get your own numbers by running _SimpleSpeedTest_ which is included in the repository.



### $() Speed-Up ###

jCores performs best when when you perform complex operations. As soon as more than one element is contained in a core, all elements are processed in parallel (using all available processor-cores). The speedup obviously differs according to the core's size and the operation complexity, but to give you at least some numbers, we operated on a core of 10k Strings and performed _.toLowerCase()_ for a number of times.

```
// Performing toLowerCase for a number of times on 100k strings using a loop - 251ms
for (int i = 0; i < strings.length; i++) {
    strings[i] = strings[i].toLowerCase() ... .toLowerCase();
}

// Performing toLowerCase for a number of times on 100k strings using jCore's map() - 168ms
$(strings).map(new F1<String, String>() { public String f(String x) {
    return x.toLowerCase(). ... .toLowerCase();   
}});
```

As noted, the speedup will vary greatly with different data-structures, core-sizes and number of processors. However, jCores is does not only help you to speed up your computations in a functionesque way, it also has a very low overhead compared to simple object creations. This means in most parts of your code you don't have to be afraid to use jCores extensively.


### $() Overhead ###

Wrapping objects into a core only takes ~3 times the time of a simple Object() creation, making it very lightweight for every-day usage.

```
// Creating 100k Objects and performing a simple operation - 839µs
cnt += new Object().equals(o) ? 1 : 0;
// Wrapping 100k Objects and performing a simple operation - 2782µs
cnt += $(o).equals(o) ? 1 : 0;
```



### $() Object Creation ###

Creating objects using spawn() is only ~10 times slower than new, but enables you to decouple code in a beautifully  short-handed way.

```
// Creating 100k Objects using new() - 1221µs
new F0Impl()
// Creating 100k Objects using $().spawn - 11685µs
$(F0.class).spawn()
```