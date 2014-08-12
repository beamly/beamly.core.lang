beamly.core.lang
================
The Beamly core scala language enhancements. This module only depends on the core Scala libraries.

[![Build Status](https://travis-ci.org/beamly/beamly.core.lang.svg?branch=master)](https://travis-ci.org/beamly/beamly.core.lang)

Getting beamly.core.lang
========================
Add this sbt dependency:

`"com.beamly" %% "beamly-core-lang" % "0.5.0"`

or maven dependency:
```xml
<dependency>
    <groupId>com.beamly</groupId>
    <artifactId>beamly-core-lang_${scala.binary}</artifactId>
    <version>0.5.0</version>
</dependency>
```
where the `scala.binary` variable is either 2.10 or 2.11

[Maven Central Repository details](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.beamly%22%20beamly-core-lang)

Maturity
========
We are rapidly approaching 1.0, feel free to provide feedback or raise issues through the github issue tracker.

Quick start
===========

The [scaladocs](http://beamly.github.io/beamly.core.lang/latest/api/ "beamly.core.lang scaladocs") provide full details, but here's a brief summary of some of the key benefits and features.

## String utilities
* `isBlank/nonBlank` methods to check if a string is empty or contains only whitespace
* `toSnakeCase` which converts a string to snake case, eg: 'nextTvEpisode' to 'next_tv_episode'
* `to<PrimitiveType>Option` which returns a primitive type from a string, converting errors into `None`

## Either utilities
* `map/flatMap` maps Right values
* `recover/flatRecover/recoverWith` converts a Left to a derivation of Right

## Option utilities
* `onSome/onNone` runs a side-effecting function and returns the original result; useful for logging

## AtomicReference utilities
* `update(f: A => A)` attempts to rerun a function to update an `AtomicReference` until the update is successful

## PartialFunction utilities
* `andThenWithContext` the same as 'andThen', except the input object is passed as well as the result
* `composePF` the same as 'compose' but returns a PartialFunction

## Map utilities
* `mergeValues` merges 2 maps together, using the provided merge function to create a new map value for a key

## Future utilities
* `promising[A](f: Promise[A] => Any)` creates a promise, uses the provided function to fulfil the promise and then returns the future from the promise
* `get` or `get(10 seconds)` awaits the result of a future with the default timeout of 5 seconds (`Await.result`)
* `await` or `await(10 seconds)` awaits the future with the default timeout of 5 seconds (`Await.ready`)
* `fold/flatFold` maps successful or failed values to a new `Future`
* `join` method on a `Future[Future[A]]`, `Future[Either[Throwable, A]]` or `Future[Try[A]]` flattens the result to a `Future[A]`

### Handling a `Future[Option[A]]`
* `mapOpt[B](f: A => B)`: allows you to simply map a `Future[Option[A]]` without having to call `aFuture map { _ map f }`
  with the far simpler form `aFuture mapOpt f`
* `flatMapOpt[B](f: A => Future[Option[B]])`: allows you to simply flatMap a `Future[Option[A]]` without having to call
  `aFuture flatMap { _ map f getOrElse futureNone }` with the far simpler form `aFuture flatMapOpt f`
* `orElse[B >: A](other: => Future[Option[B]])` provides an alternative `Future[Option[B]]` if this result is `Future(None)`

### Handling a `Future[Traversable[A]]`
* `mapTraversable` maps the values in the collection into a new `Future` without having to call `aFuture map { _ map f }`

License
=======
This software is licensed under the Apache 2.0 License.

Issue Reporting
===============
To report issues with this software, please raise them via the github reporting mechanism.

Contributions
=============
Currently we are not able to accept pull requests, if you wish to contribute please contact us via github.
