## Eclipse Integration ##

You can speed up your development using jCores and Eclipse a bit more, if you add the `$` symbol to your favorites. Do it like this (in the global preferences, search for **`favorites`**, add **`New Member`**, enter **`net.jcores.jre.CoreKeeper.$`**).

Now `$` should be available as a content assist right away. Type **`$`** in the Java editor and **then press `CTRL-SPACE`** to get the import. Afterwards you can continue with the core's arguments, e.g: Type ... `$` ... `CTRL-SPACE` ... `("a", "b", "c")`. Doing this will auto-import the CoreKeeper and provide you with code completion when you close the parentheses.


![http://data.xeoh.net/jcoreseclipse.png](http://data.xeoh.net/jcoreseclipse.png)