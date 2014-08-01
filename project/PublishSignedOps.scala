import sbt._
import sbt.Keys._
import sbtrelease.Utilities._
import com.typesafe.sbt.SbtPgp.PgpKeys._

object PublishSignedOps {
  lazy val publishSignedAction = { state: State =>
    val extracted = state.extract
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, state)
  }
}
