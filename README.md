beamly.core.lang
================

The Beamly core lang package.


License
=======

This software is licensed under the Apache 2.0 License.


Dependencies
============

This module only depends on core the core Scala libraries.


Issue Reporting
===============

To report issues with this software, please raise them via the github reporting mechanism.


Contributions
=============

Currently we are not able to accept pull requests, if you wish to contribute please contact us via github.

## Future utilities

### Waiting for a `Future` to complete
* `get` or `get(10 seconds)` awaits the result of a future with the default timeout of 5 seconds (`Await.result`)
* `await` or `await(10 seconds)` awaits the future with the default timeout of 5 seconds (`Await.ready`)

### Handling a `Future[Option[A]]`
Using the variable `aFuture`
* `mapOpt[B](f: A => B)`: allows you to simply map a `Future[Option[A]]` without having to call `aFuture map { _ map f }`
  with the far simpler form `aFuture mapOpt f`
* `flatMapOpt[B](f: A => Future[Option[B]])`: allows you to simply flatMap a `Future[Option[A]]` without having to call
  `aFuture flatMap { _ map f getOrElse futureNone }` with the far simpler form `aFuture flatMapOpt f`
* `orElse[B >: A](other: => Future[Option[B]])` provides an alternative `Future[Option[B]]` if this result is `Future(None)`
