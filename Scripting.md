# Overview #

Starting with version 0.9, _scripting_ was introduced. A script is just another name for a small Java application that you might want to export painlessly as a fully self-contained JAR. Consider you wrote this small script that lists all files below the current directory:

```
$(".").file().dir().print();
```

By using jCores scripting, you can export this application as a fully self contained JAR that you can copy and execute on any Java platform by adding just a single line, as you would add to a, say, Bash script:


```
JCoresScript.SCRIPT("FindFiles", args).pack();
$(".").file().dir().print();
```

When you run the application again (from Eclipse or any other IDE), a small JAR will be created called "FindFiles.jar", that contains all required dependencies and should be executable on any computer with an installed Java version.