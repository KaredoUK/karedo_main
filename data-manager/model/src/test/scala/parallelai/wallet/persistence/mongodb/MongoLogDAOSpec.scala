package parallelai.wallet.persistence.mongodb

import java.util.UUID

import org.specs2.mutable.{Before, Specification}
import parallelai.wallet.entity.KaredoLog



class MongoLogDAOSpec
  extends Specification
  with TestWithLocalMongoDb
  with Before
  with UUIDMatcher
{
  def before = clearAll()

  sequential

  val uid = UUID.randomUUID()
  def logWithOnlyText: KaredoLog = KaredoLog(text="Hello log")
  def logWithUser = KaredoLog(user = Some(uid),text="hi")

  "LogMongoDAO" should {
    "create a log with only text" in {
      val r1=createTextLog
      r1.id.toString must beMatching(UUIDre)
      r1.text === logWithOnlyText.text
      r1.user === None
    }

    "create a log with a specific user" in {
      val r2=createUserLog
      r2.id.toString must beMatching(UUIDre)
      r2.user must beSome(uid)
      r2.text === logWithUser.text
    }
    "verify dates are consecutive" in {
      val r1=createTextLog
      val r2=createUserLog
      r2.ts.toInstant.getMillis must be_> (r1.ts.toInstant.getMillis)
    }

  }

  def createUserLog: KaredoLog = {
    val i2 = logDAO.addLog(logWithUser).get
    val r2 = logDAO.getById(i2).get

    println("r2: " + r2)

    r2
  }

  def createTextLog: KaredoLog = {
    val i1 = logDAO.addLog(logWithOnlyText).get
    val r1 = logDAO.getById(i1).get
    println("r1: " + r1)

    r1
  }
}
