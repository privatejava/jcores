## Replacing content in files ##

We used this _script_ to convert and clean a number of files for an experiment.

```
CoreString text = $("file.txt").file().text();

text = text.replace("<i>(.*?)</i>", "$1");
text = text.replace("<b>(.*?)</b>", "$1");
text = text.replace("<a (.*?)>(.*?)</a>", "$2");
text = text.replace("<sup (.*?)>(.*?)</sup>", "");
text = text.replace("„", "\"");
text = text.replace("“", "\"");

$("converted.txt").file().append(text.get(0));
```



## Make sure an object is not null ##

Sometimes you want to make sure you a parameter or object you got is not null. In this case, when name is `null`, `get(default)` tries to return the object at position `0` (name in our example), or `default` when that object is null.

```
name = $(name).get("Unknown User") // jCores way, same as 'name = name == null ? "Unknown User" : name;'
```



## Get the median from a dataset ##

Once we wanted to obtain the median of a larger dataset. A simple chain of commands (and a comparator) is all that is required. First the dataset is wrapped and it is ensured that all elements are unique (we wanted to sort out elements occurring several times). Then the core is sorted (using the default comparator) and the middle value is returned.

```
$(dataset).unique().sort().get(0.5)
```



## Listing directories / copying files ##

Some things in Java could have really been easier. Common file operations for example. Look at the example below to see how you can list a directory's content and copy files. Copying, of course, also works with directories and recursively.

```
$("some/path").file().dir().print()
$("source.txt").file().copy("target.txt")
```




## Play a WAV file ##

This is also something I had to look up again and again - how to play a WAV file. Using jCores it boils down to a few simple commands:

```
$("sound.wav").file().audio().play()
```



## Process a number of images and store the results ##

How to load a number of images, extract data, and store the results in a text file. First all files in a given directory are listed. Then the files are filtered for the extension `png`. Now each image is loaded and some content extracted, which is stored in a list of `ReadRange`s. These lists are expanded and converted to strings, which are eventually joined into a single string. In the end the file called `results.txt` is deleted and then newly created with the previously generated output.

```
String output = $("images/").file().dir().filter(new F1Object2Bool<File>() {
    public boolean f(File i) {
        return i.getAbsolutePath().endsWith(".png");
    }
}).map(new F1<File, List<ReadRange>>() {
    public List<ReadRange> f(File f) {
        final String id = $(f.getAbsolutePath()).split("/").get(-1);
        final BufferedImage read = ImageIO.read(f);

        // Perform some magic ...
    }
}).expand(ReadRange.class).map(new F1<ReadRange, String>() {
    public String f(ReadRange x) {
        // Convert range to string format ...
    }
}).as(CoreString.class).join("\n");

$("result.txt").file().delete().append(output);
```


## Read a ZIP located in the classpath ##

Sometimes you have data stored within the classpath for easier resource handling, like in this case a ZIP file. Accessing it and reading its data is straightforward. This example opens a ZIP file which is stored in the same package as `Resources.java` is, opens it, and reads its entry `results.txt`.

```
InputStream stream = $(Resources.class.getResourceAsStream("data.zip")).zipstream().get("results.txt");
```



## Randomly select 6 uniques numbers from 1 to 49 ##

Sometimes you want to randomly sub-sample from a larger set of object, for example numbers. A combination of `$.range()` and `object.random()` can help:

```
$.range(1, 50).random(6).string().print();
```



## Download a file (and check its MD5) ##

In case there is a file on the web you wish to verify try the code below. It opens an URI, downloads it to the temporary directory and returns a set of file handles. These are then opened for input, and their hash values (MD5 in this case) computed.

```
$("http://jcores.net/index.html").uri().download().input().hash().print();
```


## Perform the regex-DNA Task ##

This is an adaption of the [regex-DNA](http://shootout.alioth.debian.org/u32q/performance.php?test=regexdna) task from the [Computer Language Benchmark Game](http://shootout.alioth.debian.org/). The neat thing is, it requires less code than the single-threaded version and is faster than the multithreaded version (mt-version: ~41.7ms, jcores: ~34.4ms counting avg. of last 15 out of 20 runs) in the original game. The code below is an excerpt, the full code is in the repository:

```
final String s = $(inputStream).text().get(0);
final String sequence = $(s).replace(">.*\n|\n", "").get(0);

// Little trick so we can print our results in an ordered way
final Map<String, Integer> result = new ConcurrentHashMap<String, Integer>();

// Produce counts
$("agggtaaa|tttaccct", 
  "[cgt]gggtaaa|tttaccc[acg]", 
  "a[act]ggtaaa|tttacc[agt]t",
  "ag[act]gtaaa|tttac[agt]ct", 
  "agg[act]taaa|ttta[agt]cct", 
  "aggg[acg]aaa|ttt[cgt]ccct",
  "agggt[cgt]aa|tt[acg]accct", 
  "agggta[cgt]a|t[acg]taccct", 
  "agggtaa[cgt]|[acg]ttaccct")
  .map(new F1<String, String>() {
    public String f(String x) {
        int count = 0;
        Matcher m = Pattern.compile(x).matcher(sequence);
        while (m.find())
            count++;
        result.put(x, count);
        return x;
    }
}).forEach(new F1<String, Void>() {
    public Void f(String x) {
        console.append(x + " " + result.get(x) + "\n");
        return null;
    }
});

// Rewrite sequence
final StringBuffer dst = new StringBuffer();
final Pattern pattern = Pattern.compile("[WYKMSRBDVHN]");
final Matcher matcher = pattern.matcher(sequence);
while (matcher.find()) {
    matcher.appendReplacement(dst, "");
    dst.append(replacements.get(matcher.group(0)));
}
matcher.appendTail(dst);
```


## Download all your favorite comics ##

I used (a slight modification of) this script to download a number of comics along with their rating. First it creates a CoreNumber object, containing the numbers from 1 to 1531, then it grabs each comic info-page, extracts the score and the image-file name, and downloads each image to a file in the "dl" folder.

```
$.range(1, 1532).forEach(new F1<Number, Void>() {
    public Void f(Number id) {
        final String text = $("http://my.comicsite.com/index.php?pic=" + id).uri().download().text().get(0);
	final String score = extractrating(text);
	final String image = extractimage(text);
        
        // Download file
        final File file = $("http://my.comicsite.com/cartoons/strip_" + image + ".jpg").uri().download("dl").get(0);
        if(file == null) return null;
        
        file.renameTo(new File("dl/" + image + " (" + score + ").jpg"));
        
        return null;
    }
});
```



## Construct an Object (without try/catch) ##

Sometimes you want to construct an object whose constructor throws an exception, but you know that in the specific case it won't, or it does not matter. To unclutter your code (and avoid an ugly try/catch block), you can let jCores swallow the exception handling for you. In case something went wrong, null would be returned (as one might expect it). The following example creates a new Robot object.

```
$(Robot.class).spawn().get(0)
```


## Retrieving the loop index for map() and forEach() ##

Admittedly, this is rather a FAQ entry, but it can also serve as an example of some of the small details jCores offeres. One of them is access to the current index in map() and forEach() operations.

While the it would have been possible to add several interfaces that deal with the different possible handler interfaces (element only, index only, element & index, ...) this would have introduced a whole tree of these functions in the end that needed separate code.

Thus we decided to keep the interfaces simple and consistent, and instead make indexing optional (in most cases people are more interested in the elements anyways, than the actual loop index). So, in order to actually access the current index within a handler write:

```
final Indexer i = Indexer.NEW();        
$(objects).map(new F1<String, Void>() { public Void f(String file) {
    System.out.println("Object " + file + " has index " + i.i()); 
}}, i);
```




## Background Tasks & Performing a GET Request ##

Sometimes you want to do an asynchronous task, running in the background, and collect the result at a later time, if at all. With the `async()` method this should be straightforward now, since it will make sure that the passed function is being executed as a daemon background task that won't block you application from exiting.

```
$.async(new F0R<String>() {
    public String f() {
        return server.getMessage();
    }
});
```

The `async` method (and the corresponding `Async` object) are being used at a number of places, for example also in `$.net.get()`, which you can use to perform an async get request to some web service. The snippet below will contact a web service and submit the given data. Once a result is available `onNext` will be executed:

```
Async<String> async = $.net.get("http://server/api", data);
async.onNext(new F1V<String>() {
    public void fV(String x) {
        System.out.println(x);
    }
});
```

The last thing you might want to know is this respect are _kill switches_. A kill switch is an object which can be given to any asynchronous operation and provides a trigger method. Once triggered all the requests to which it was given will cease operation at the next best opportunity. In the example below we will use an automatically triggered kill switch that will activate after 500ms:

```
$.async(f, KillSwitch.TIMED(500));
```



## Printing a status report ##

If something went wrong and you want to get more detailed information just print a status report. You will also be notified if there is a newer version available that might contain bug fixes or new features.

```
$.report()
```


## _Scripting_ ##

Sometimes you have a small snippet of code which you would like to transform into a self-contained Java application, including all its dependencies. Luckily there's a call for that. Just put this line at the very beginning of your main method, and you will receive a fully self contained JAR that executes  from the command line and from the GUI (scripting extension required).

```
JCoresScript.SCRIPT("MyApp", args).console().pack();
```


Check out the [scripting page](Scripting.md) for more details.

<br />
# Still more questions? #

Then we recommend to start exploring the [JavaDoc API](http://jcores.net/api). Especially the [CoreObject](http://jcores.net/api/net/jcores/jre/cores/CoreObject.html) reference is a good entry point. To see what the future holds (Java 8 and later), have a look at the page [Lambda Expressions](LambdaExpressions.md). Also, feel free to drop a comment, or <a href='http://groups.google.com/group/jcores'>ask in the forum</a>. Happy _scripting_ :-)