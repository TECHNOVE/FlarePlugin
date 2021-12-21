# Flare for Bukkit

Downloads can be found under releases.

## Setup

There are two primary things that should be setup in order to optimally use Flare, however they are not required.

### 1. Add the Java flags

```
-XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
```

These two flags will ensure that you are getting the most accurate data in your Flares.

### 2. Use a JVM with debug symbols

Most JDK distributions include debug symbols for Java 16+, but some distributions do not.
You can test with the following command:

```bash
gdb $JAVA_HOME/lib/server/libjvm.so -ex 'info address UseG1GC'
```

If the UseG1GC symbol is found, then the debug symbols are present and you can use memory profiling.


### 3. Add Flare token

Once you subscribe to [the Patreon](https://www.patreon.com/airplane) you can find your token [here](https://auth.airplane.gg).
Put this inside `plugins/FlarePlugin/config.yml` like so:

```yaml
flare:
  url: https://flare.airplane.gg
  token: "mytokenhere"
```

Finally, you can run `/flare` in-game to start profiling!


## License

MIT
