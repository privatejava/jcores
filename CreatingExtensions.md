# Overview #
There are two types of extensions. Local extensions which are usually called somewhere in a call chain such as `$(objects).map(f).as(MyCore.class).g()` and global extensions which are accessible directly at top-level, such as `$(Ext.class).server()`.


# Local Extensions #

To create a local extension you just extend the type of Core that comes closest to what you need, most of the times this will be a `CoreObject`. The only other requirement is that you define a constructor that takes the `CommonCore` and an `AbstractAdapter`. Alternatively you can also extend the `LocalExtension`.

```
public class MyCore<MyType> extends CoreObject<MyType> {

    public MyCore(CommonCore supercore, AbstractAdapter<MyType> adapter) {
        super(supercore, adapter);
    }

    /** Your function here, e.g. */
    public boolean checksize() {
        return size() == compact().size();
    }
}
```

### Usage ###

```
$(objects).map(f).as(MyCore.class).checksize()
```


# Global Extensions #

To create a global extension extend the `GlobalExtension`. Your class must have a no-args constructor and should do all heavyweight initialization on `init()` only.

```
public class Ext extends GlobalExtension {
    String uniqueID = $.sys.uniqueID();
    
    public String random() {
        return this.uniqueID;
    }
}
```

### Usage ###

```
$(Ext.class).random();
```


# Project Template #

There is a plugin template  for [download in the downloads section](http://code.google.com/p/jcores/downloads/list).