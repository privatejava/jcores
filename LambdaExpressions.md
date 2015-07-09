# Examples #

The following examples have been successfully tested with the latest (8. September 2011) version of the [Java 8 Lambda Expressions Preview](http://hg.openjdk.java.net/lambda/lambda/langtools). While Java 8 is still some time into the future, it is nonetheless interesting to see what one can expect from it.

### Getting Started ###

The following instructions apply to Mac OS, but things should be similar on other platforms

  1. Install latest [langtools package](https://github.com/AmailP/java-closures-experiments/wiki/Environment-setup-(MacOS-X-10.7)) (see section _Project Lambda langtools_) and Java 7 ([click here for Mac](http://code.google.com/p/openjdk-osx-build/)).
  1. Download latest jCores (see links above)
  1. Set the proper path for `JAVA_HOME` to your Java 7 installation: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/`
  1. Put the following examples into an editor of your choice and compile on the command line with `./langtools/dist/bootstrap/bin/javac Examples.java -cp jcores.jar`
  1. In the directory where you edited, start the compiled application with  `java -cp jcores.jar:. Examples`

### Examples ###

Down here you can see some examples how jCores and lambda expressions will work together nicely, the only downside is that right now Google Code is unable to do the syntax highlighting right ;-)

```
import static net.jcores.jre.CoreKeeper.$;

public class Examples {
    public static void main(String[] args) {
        $("HELLO", "WORLD").map( #{ x -> x.toLowerCase() }).reduce( #{ x, y -> x + y } ).print();
        $.async( #{ $.sys.sleep(2000); return "Yeah :-)"; }).onNext( #{ x -> System.out.println(x) } );
        $.sys.sleep(3000);			
    }
}
```