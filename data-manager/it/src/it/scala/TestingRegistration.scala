import org.specs2.mutable.Specification


class TestingRegistration
  extends Specification
  with ItEnvironment {

  clearAll()

  sequential

  "Registration" should {
    "be able to complete" in {

      val r = RegisterAccount

      isUUID(r.application.toString)
      isUUID(r.userId.toString)
      isUUID(r.sessionId)
    }
    "be able to Reset application" in {
      val (applicationId, activationCode0, activationCode) = ResetAccount

      activationCode0 must be_!==(activationCode)
    }
  }

}
