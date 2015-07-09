# General #


  * **How does jCores compare to jQuery?**

> Being seduced by jQuery I asked myself how its Java counterpart might look like. I wanted to have a simple, straightforward _syntax_ for the most common Java operations I was missing. At the same time I wanted it to be as fast and lean as possible, without sacrificing its usability.

> There are, however, natural differences between them. jQuery was created to ease the pain of DOM access and manipulation on the JavaScript side, and the big advantage jQuery has is JavaScript's dynamic. You can add new functions anytime you want, and the simple DOM-object selection is just neat. There is no such thing as a DOM tree in Java, so every object jCores should operate on has to be explicitly delivered by the user. But the big advantage jCores has is true parallelism. Java natively supports multiple threads, and jCores tries to take advantage of them as best as it can.


  * **Is it production ready?**

> Should be. We have been using it for over a year now without major problems, minor bugs are probably in it, but should be fixed quickly. We're also constantly extending our unit tests.



# Performance #

  * **How fast / how slow will my application become when using jCores?**

> Tricky question and hard to answer in detail, but here are some guidelines. The more you are using jCores in the _outside_ (i.e., not deeply nested within the call stack) the more your speed will benefit. The more you are using it on the _inside_, the higher its overhead will be. The overhead for most operations (not marked as _heavyweight_)  should be very low and a simple wrapping also takes only a few operations (two assignments and one object creation).  Also, the more data a core encloses, and the more complex its `map()` / `fold()` operation is, the more efficient jCores can operate.

> In general we recommend to use jCores wherever you can code something faster with it, and remove it wherever a profiler run indicates a performance problem due to overhead (which, however, should be unlikely).

> For a more detailed discussion, please see the [performance page](Performance.md).



# Internal #

  * **How does `map()` know if it should go parallel or not?**

> The first time jCores is being initialized we perform a small profiling run where we measure the average speed it takes to get a 2nd thread started. Now, whenever you call `map()`, we map the first object right away with the function you passed and measure the time that took. Since subsequent calls will likely be faster (due to HotSpot) we check how many objects there are still to process and estimate the maximal time that will take. If this time is less than the time we measured obtaining a new thread would be, we stay single threaded.