# bigdeps

Find and print big dependencies.

## Usage

### As a Tool

```shell
clj -Ttools install dev.kwill/bigdeps '{:mvn/version "1.0.2"}' :as bigdeps
```

```shell
clj -Tbigdeps run :n 5
| :size-mb |                            :lib-name |                    :dependents |
|----------+--------------------------------------+--------------------------------|
|     3.91 |                  org.clojure/clojure |                                |
|     2.67 |               com.google.guava/guava | [org.clojure/tools.deps.alpha] |
|     0.74 | org.apache.httpcomponents/httpclient | [org.clojure/tools.deps.alpha] |
|     0.61 |          org.apache.maven/maven-core | [org.clojure/tools.deps.alpha] |
|     0.61 |               org.clojure/spec.alpha |          [org.clojure/clojure] |
```

### As a Function

```clojure
{...
 :aliases
 {:bigdeps {:replace-deps {dev.kwill/bigdeps {:mvn/version "1.0.2"}}
            :exec-fn      kwill.bigdeps/run}}}
```

```shell
 clj -X:bigdeps :n 2 :deps "'path/to/deps.edn'"
```

## Options

- `:n` Print top N dependencies by size. Defaults to `nil` (i.e., all dependencies).
- `:deps` Path to the deps.edn to use.
