# Current Performance Figures Explained #

The results below were generated on a MacBook Pro (Late 2008 running Lion), 2.4 GHz, 2 CPU cores, 8 GB RAM, Java 6.26 with jCores version 0.9. The relative performance numbers of Java 7 (using OpenJDK) look similar, but are generally faster.

The benchmark consists of a number of **main tests** (tasks) we use to check jCores' performance. Each test is usually conducted in a number of **variants** (each variant is one different way of implementing the problem of the main task), each variant is executed a number of times (**20 times**) and each variant run consists of several **thousands of actual invocations** of the method to measure. In order to reduce the influence of the HotSpot warmup time we compute the final results as the **median of the last 10 runs**, thus ignoring the first 10 runs.

<br />
**IMPORTANT NOTE** - This benchmark was specifically designed to profile some _problematic regions_ of jCores (e.g., where we have to use reflection, execution analysis or educated guessing) and many tasks and variants were selected with the express purpose of assessing, understanding, and eventually addressing our worst-case performance. While it would have been no problem to design scenarios where we excel with flying colors, we instead ask you to read these results observantly in the context of your own usage behavior.
<br />



The main tests are:

  * **Simple Task** - This measures the overhead it takes to wrap a single object inside a core. In the results below (for the setup mentioned above), the average time to wrap a single object 10k times is approx. 9µs (in contrast to 0-1µs it takes to create a simple Java `Object`. This benchmark shows that while there is a slight overhead in using jCores in general, it is, in absolute values, very low.

  * **Cloning Task** - Measures the overhead it takes to clone a `Clonable`object, one time with jCores (which uses _mild reflection_, one time by explicitly calling the `clone()` method. Using the jCores general clone method has approximately the overhead of 2-3 explicit clone invocations.

  * **Mapping & Looping (Simple)** - In here we measure the time it takes to convert and process a (variable) number of objects with a simple method (one which executes very fast). Like the two examples above this is, in terms of performance, one of the hardest scenarios for jCores, since even the slightest overhead we cause will have a relatively big impact in contrast to a simple method invocation. The benchmark consist of a 3x3 invocation, were we test a simple operation on 1, 5 and 10k objects, with vanilla Java code (`for`-loop), `jCores.map()` and `jCores.forEach()`. In this example the sets with size `1` disfavor jCores the most due to the overhead in contrast to one single execution, sets with a size of `5` disfavor `map()` the most, since on each execution it has to measure and decide if it should go parallel or stay single threaded (which adds some overhead measuring time). It can be seen that the bigger the set gets, the less impacting our overhead becomes.

  * **Mapping & Looping (Complex)** - This is basically the same example as above, this time with a more complex processor function. It can be seen that for the smaller sets the execution times are almost the same, while for the big set jCores eventually decided to go parallel, effectively reducing execution time to 50%. Note that the s5.`*` tests only appear to be faster, actually the s1.`*` tests were, due to the nature of the object, even more complex.

  * **RegExDNA** - We reimplemented the `regexdna`task from the Language Shootout game with jCores. The plain variants are the single threaded (st) and multi threaded (mt) originals found on the web page (the assisted use some jCores code).


<br />
### Performance Conclusion ###

Given the background information above and the numbers below we conclude that jCores has, like any library, a small performance overhead which is naturally caused due to the necessary object creation that happens upon the wrapping of objects. The results also show that the more complex the processing function becomes (or the more object are being processed) the less impact this overhead has.

In general jCores should always give a speed net-benefit when there are at least 3 independent objects to process, 2 CPUs available and the processing function takes significantly longer than the time to spawn a new thread. In other cases the performance might be similar or somewhat slower, but in these cases the absolute time will be nonetheless very small (since we're talking about times which are below the order of magnitude a thread creation takes).

Of course jCores should virtually always give you at least a coding benefit, since you can usually express more functionality with less code to type.

<br />
### Test Results ###


You can also run these benchmarks on your own machine, just start `benchmarks.BenchmarkMain` (from the `core/jre/test` folder in your IDE)


```
Simple Task
    s1.overhead.plain:   0µs (180048µs 959µs 937µs 934µs 2703µs 16µs 1µs 0µs 0µs 1µs 11µs 1µs 0µs 0µs 1µs 0µs 1µs 0µs 1µs 0µs)
    s1.overhead.jcores:  9µs (12485µs 3007µs 21µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 9µs 8µs 8µs 8µs)
Cloning Task
    clone.plain:        12645µs (37582µs 16726µs 15768µs 16601µs 15187µs 15188µs 14328µs 15374µs 14834µs 16323µs 15434µs 15933µs 19453µs 16038µs 21103µs 15656µs 12786µs 12645µs 13521µs 12832µs)
    clone.jcores:       30976µs (52744µs 33088µs 30964µs 29374µs 49455µs 36767µs 30677µs 31443µs 32111µs 31056µs 32542µs 31827µs 29785µs 29351µs 32028µs 44303µs 45874µs 30976µs 30246µs 31988µs)
Mapping and Looping (Simple Operations)
    s1.lowercase.plain:     2862µs (28803µs 6412µs 4452µs 3014µs 2867µs 2873µs 2864µs 2986µs 3093µs 2870µs 2872µs 2897µs 2866µs 2868µs 2862µs 2885µs 2871µs 2862µs 2861µs 2882µs)
    s1.lowercase.forEach:   4080µs (394681µs 14782µs 4080µs 4847µs 3897µs 3967µs 3940µs 3896µs 3950µs 4061µs 4734µs 3895µs 4029µs 4691µs 3885µs 3970µs 3921µs 4080µs 4564µs 3900µs)
    s1.lowercase.map:       4493µs (26988µs 20748µs 6232µs 6009µs 4111µs 3660µs 3660µs 5137µs 3686µs 3727µs 3682µs 4885µs 4932µs 3751µs 3876µs 4466µs 3647µs 4493µs 3699µs 3689µs)
    s5.lowercase.plain:     1465µs (10560µs 2591µs 1756µs 1748µs 1851µs 1725µs 1485µs 1464µs 1464µs 1472µs 1528µs 1468µs 1464µs 1464µs 1467µs 1464µs 1467µs 1465µs 1481µs 1464µs)
    s5.lowercase.forEach:   3482µs (17146µs 11562µs 6951µs 3524µs 3541µs 5271µs 3878µs 3752µs 3487µs 3478µs 3652µs 4117µs 4510µs 5012µs 3476µs 3585µs 3618µs 3482µs 3573µs 4341µs)
    s5.lowercase.map:       4528µs (76017µs 15426µs 4956µs 4479µs 6159µs 4930µs 5881µs 4894µs 4910µs 4823µs 4463µs 4512µs 4469µs 4621µs 5921µs 4869µs 4501µs 4528µs 4571µs 4456µs)
    sn.lowercase.plain:     6939µs (15806µs 7482µs 6754µs 6559µs 7172µs 7246µs 7999µs 7309µs 6829µs 7099µs 7177µs 6623µs 6875µs 7336µs 7908µs 6696µs 6698µs 6939µs 6775µs 7443µs)
    sn.lowercase.forEach:   8457µs (15934µs 8475µs 8505µs 8506µs 8426µs 11626µs 10282µs 8890µs 9253µs 9030µs 9069µs 9655µs 8785µs 9587µs 9548µs 9900µs 8451µs 8457µs 8520µs 8564µs)
    sn.lowercase.map:       5735µs (19868µs 8832µs 5903µs 5606µs 5717µs 6088µs 9554µs 11745µs 5889µs 6046µs 6208µs 5636µs 5756µs 18006µs 7443µs 5890µs 6214µs 5735µs 6027µs 5587µs)
Mapping and Looping (Complex Operations)
    s1.lowercase.plain:     58669µs (81505µs 60614µs 56850µs 57716µs 59677µs 59405µs 57959µs 57610µs 57080µs 57295µs 58904µs 59156µs 59762µs 59787µs 59119µs 58156µs 57321µs 58669µs 56619µs 56016µs)
    s1.lowercase.forEach:   58820µs (85371µs 60637µs 60254µs 59738µs 57442µs 56541µs 59655µs 59987µs 60322µs 59632µs 63062µs 56245µs 60441µs 59562µs 58835µs 59514µs 60197µs 58820µs 60326µs 57360µs)
    s1.lowercase.map:       58528µs (66055µs 58759µs 57820µs 57724µs 60172µs 60057µs 60018µs 59098µs 58636µs 65267µs 59077µs 61032µs 58197µs 58012µs 57523µs 56825µs 60347µs 58528µs 56403µs 60430µs)
    s5.lowercase.plain:      8022µs (9598µs 10156µs 9631µs 7819µs 7754µs 7868µs 15875µs 7921µs 10110µs 7964µs 9561µs 8099µs 8869µs 9861µs 8174µs 7805µs 8007µs 8022µs 9581µs 7732µs)
    s5.lowercase.forEach:    8048µs (11933µs 19298µs 8973µs 8100µs 8202µs 8193µs 8693µs 7975µs 7937µs 7950µs 7955µs 8935µs 8167µs 8058µs 7978µs 7992µs 8002µs 8048µs 8609µs 8053µs)
    s5.lowercase.map:        8404µs (11400µs 10369µs 8795µs 8115µs 8004µs 8469µs 8204µs 8783µs 8076µs 8457µs 8738µs 7752µs 8117µs 7981µs 7753µs 7993µs 8182µs 8404µs 7756µs 7724µs)
    sn.lowercase.plain:    397855µs (404804µs 389324µs 380841µs 383039µs 395004µs 393685µs 385683µs 381115µs 381890µs 386061µs 385803µs 382126µs 401921µs 388158µs 383910µs 387408µs 384385µs 397855µs 391977µs 396368µs)
    sn.lowercase.forEach:  382895µs (400579µs 407663µs 422799µs 395812µs 402653µs 393470µs 392960µs 398815µs 386120µs 390763µs 393195µs 395828µs 397003µs 395536µs 385093µs 388875µs 381294µs 382895µs 386925µs 394526µs)
    sn.lowercase.map:      202749µs (235398µs 196392µs 202577µs 198394µs 211471µs 202328µs 223015µs 205779µs 196672µs 200225µs 197395µs 193966µs 202273µs 198092µs 196637µs 203391µs 232028µs 202749µs 201020µs 193830µs)
RegExDNA
    plain.shootout.st:          56787µs (101019µs 91902µs 66879µs 61476µs 59273µs 60844µs 59354µs 59779µs 57561µs 60560µs 59479µs 58135µs 57360µs 57849µs 57068µs 57654µs 59773µs 56787µs 55769µs 56821µs)
    plain.shootout.mt:          58629µs (86806µs 73625µs 71666µs 71146µs 56752µs 69153µs 46995µs 58166µs 57095µs 47186µs 49855µs 59140µs 46376µs 47840µs 52251µs 64775µs 73564µs 58629µs 58172µs 59671µs)
    plain.shootout.mtassisted:  49739µs (48330µs 89059µs 131284µs 56405µs 63036µs 43702µs 46184µs 47074µs 45397µs 47526µs 48620µs 45459µs 46274µs 48586µs 41542µs 47021µs 43722µs 49739µs 47551µs 47724µs)
    jcores.2:                   35216µs (39839µs 41913µs 96331µs 61331µs 56657µs 63173µs 39419µs 68371µs 33429µs 44995µs 34184µs 59199µs 65344µs 56453µs 40681µs 49303µs 35245µs 35216µs 34381µs 35166µs)
```