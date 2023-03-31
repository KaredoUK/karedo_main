package test.specs4_login

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import common.AllTests
import karedo.persist.entity._
import karedo.route.util._
import karedo.common.misc.Util.now
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach, Ignore}
import org.scalatest.junit.JUnitRunner


/**
  * Created by pakkio on 10/21/16.
  */
@Ignore
@RunWith(classOf[JUnitRunner])
class Kar_LoginSequence_Test extends AllTests with BeforeAndAfterEach {

  // Known Set Up
  val known_acctId = "KNOWN_ACCT_ID"
  val known_appId = "KNOWN_APP_ID"
  val known_msisdn = "+447711060452"
  val known_email = "chandan.rajah@gmail.com"
  val known_f_name = "Chandan"
  val known_l_name = "Rajah"

  // Unknonw Set Up
  val unknown_acctId = "UNKNOWN_ACCT_ID"
  val unknown_appId = "UNKNOWN_APP_ID"
  val unknown_msisdn = "+447825010428"
  val unknown_email = "neetasha@gmail.com"
  val unknown_f_name = "Neetasha"
  val unknown_l_name = "Rozario"

  // Conflict Set Up
  val conflict_acctId = "CONFLICT_ACCT_ID"
  val conflict_appId = "CONFLICT_APP_ID"
  val conflict_msisdn = "+447092878808"
  val conflict_email = "crajah@karedo.co.uk"
  val conflict_f_name = "Storm"
  val conflict_l_name = "Roman"

  def deleteAllRelevantTables = {
    // Delete All
    dbUserAccount.deleteAll()
    dbUserApp.deleteAll()
    dbUserKaredos.deleteAll()
    dbUserProfile.deleteAll()
    dbUserMobile.deleteAll()
    dbUserEmail.deleteAll()
  }

  def setUpTestEnvironment = {
    // Set Up Knowns
    dbUserAccount.insertNew(UserAccount(id = known_acctId, userType = "CUSTOMER", temp = true, ts_created = now
//      , mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now)))
//      , email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )
    ))

    dbUserApp.insertNew(UserApp(id = known_appId, account_id = known_acctId, mobile_linked = true, email_linked = true, ts = now))
    dbUserKaredos.insertNew(UserKaredos(id = known_acctId, karedos = 500, ts = now))
    dbUserProfile.insertNew(UserProfile(id = known_acctId, first_name = Some(known_f_name), last_name = Some(known_l_name)))
    dbUserMobile.insertNew(UserMobile(id = known_msisdn, account_id = known_acctId, active = true, ts_created = now, ts_updated = now))
    dbUserEmail.insertNew(UserEmail(id = known_email, account_id = known_acctId, active = true, ts_created = now, ts_updated = now))

    // Set Up COnflict
    dbUserAccount.insertNew(UserAccount(id = conflict_acctId, userType = "CUSTOMER", temp = false, ts_created = now,
      mobile = List(Mobile(msisdn = conflict_msisdn, valid = true, ts_created = now, ts_validated = Some(now))),
      email = List(Email(address = conflict_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

    dbUserApp.insertNew(UserApp(id = conflict_appId, account_id = conflict_acctId, mobile_linked = true, email_linked = true, ts = now))
    dbUserKaredos.insertNew(UserKaredos(id = conflict_acctId, karedos = 500, ts = now))
    dbUserProfile.insertNew(UserProfile(id = conflict_acctId, first_name = Some(conflict_f_name), last_name = Some(conflict_l_name)))
    dbUserMobile.insertNew(UserMobile(id = conflict_msisdn, account_id = conflict_acctId, active = true, ts_created = now, ts_updated = now))
    dbUserEmail.insertNew(UserEmail(id = conflict_email, account_id = conflict_acctId, active = true, ts_created = now, ts_updated = now))
  }

  override def beforeEach {
    deleteAllRelevantTables
    setUpTestEnvironment
  }

  override def afterEach {
    deleteAllRelevantTables
  }

  "Kar141 - POST /account with Application UNKNOWN" should {
    "* Mobile = UNKNOWN, Email = UNKNOWN" in {

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = unknown_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual false
      userApp.email_linked shouldEqual false

      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [KO[_]]
      userAcct.findActiveEmail shouldBe a [KO[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual false

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = UNKNOWN, Email = KNOWN" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual false
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [KO[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual false

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Email = UNKNOWN" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = unknown_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(known_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual false

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [KO[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Email = KNOWN, Account NOT TEMP" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)))
        ))

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(known_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Email = KNOWN, Account IS TEMP" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now)))
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)))
        ))

      //@TODO: Make conflict Account false.

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Email = KNOWN, WITH CONFLICT" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)))
        ))

      val reqAppId = unknown_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = conflict_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[ErrorRes]
          status.intValue() shouldEqual (HTTP_CONFLICT_409)
          res.error_code shouldEqual HTTP_CONFLICT_409
        }

    }
  }

  "Kar141 - POST /account with Application KNOWN" should {
    "* Mobile = UNKNOWN, Email = UNKNOWN" in {

      val reqAppId = known_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = unknown_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [KO[_]]
      userAcct.findActiveEmail shouldBe a [KO[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual false

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = UNKNOWN, Email = KNOWN, Account( Application = Email)" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      val reqAppId = known_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [KO[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual false

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = UNKNOWN, Email = KNOWN, Account( Application != Email) & UserApp.email_linked = TRUE" in {

      val reqAppId = known_appId
      val reqFName = unknown_f_name
      val reqLName = unknown_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual true
      userAcct.findActiveMobile shouldBe a [KO[_]]
      userAcct.findActiveEmail shouldBe a [KO[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual false

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = UNKNOWN, Email = KNOWN, Account( Application != Email) & UserApp.email_linked = FALSE" in {
      dbUserApp.update(
        dbUserApp.find(known_appId).get.copy(
          email_linked = false
        )
      )

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = unknown_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual false
          res.account_id shouldEqual None
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual conflict_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == conflict_msisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == conflict_email)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Account( Application = Mobile), Email = UNKNOWN" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = unknown_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(known_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [KO[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Account( Application = Mobile), Email = KNOWN, Account( Application = Email)" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = known_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(known_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Account( Application = Mobile), Email = KNOWN, Account( Application != Email)" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = known_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(known_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual known_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual false

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Account( Application != Mobile), Application.mobile_lined = FALSE, Conflict Account != TEMP" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      dbUserApp.update(
        dbUserApp.find(known_appId).get.copy(
          mobile_linked = false
        )
      )

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = conflict_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[SendCodeResponse]
          res.returning_user shouldEqual true
          res.account_id shouldEqual Some(conflict_acctId)
          status.intValue() shouldEqual (HTTP_OK_200)
        }

      // Check if objects were created.
      val userApp = dbUserApp.find(reqAppId).get
      userApp.id shouldEqual reqAppId
      userApp.mobile_linked shouldEqual true
      userApp.email_linked shouldEqual true

      userApp.account_id shouldEqual conflict_acctId
      val resAcctId = userApp.account_id

      val userAcct = dbUserAccount.find(resAcctId).get
      userAcct.id shouldEqual resAcctId
      userAcct.temp shouldEqual false
      userAcct.findActiveMobile shouldBe a [OK[_]]
      userAcct.findActiveEmail shouldBe a [OK[_]]

      val resMobileList = userAcct.mobile.filter(x => x.msisdn == reqMsisdn)
      resMobileList should not be Nil
      val resMobile = resMobileList.head
      resMobile.valid shouldEqual true

      val resEmailList = userAcct.email.filter(x => x.address == reqEmail)
      resEmailList should not be Nil
      val resEmail = resEmailList.head
      resEmail.valid shouldEqual true

      val userProfile = dbUserProfile.find(resAcctId).get
      userProfile.first_name shouldEqual Some(reqFName)
      userProfile.last_name shouldEqual Some(reqLName)
    }

    "* Mobile = KNOWN, Account( Application != Mobile), Application.mobile_lined = FALSE, Conflict Account == TEMP" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      dbUserApp.update(
        dbUserApp.find(known_appId).get.copy(
          mobile_linked = false
        )
      )

      dbUserAccount.update(
        dbUserAccount.find(conflict_acctId).get.copy(
          temp = true))

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = conflict_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[ErrorRes]
          res.error_code shouldEqual HTTP_SERVER_ERROR_500
          status.intValue() shouldEqual (HTTP_SERVER_ERROR_500)
        }
    }

    "* Mobile = KNOWN, Account( Application != Mobile), Application.mobile_lined = TRUE" in {

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          mobile = List(Mobile(msisdn = known_msisdn, valid = true, ts_created = now, ts_validated = Some(now))), temp = false
        ))

      dbUserAccount.update(
        dbUserAccount.find(known_acctId).get.copy(
          email = List(Email(address = known_email, valid = true, ts_created = now, ts_validated = Some(now)) )))

      dbUserApp.update(
        dbUserApp.find(known_appId).get.copy(
          mobile_linked = true
        )
      )

      dbUserAccount.update(
        dbUserAccount.find(conflict_acctId).get.copy(
          temp = true))

      val reqAppId = known_appId
      val reqFName = known_f_name
      val reqLName = known_l_name
      val reqMsisdn = conflict_msisdn
      val reqEmail = conflict_email

      val request = post_SendCodeRequest(
        application_id = reqAppId,
        first_name = reqFName,
        last_name = reqLName,
        msisdn = reqMsisdn,
        user_type = "CUSTOMER",
        email = reqEmail

      ).toJson.toString

      Post(s"/account",
        HttpEntity(ContentTypes.`application/json`, request)) ~>
        routesWithLogging ~>
        check {
          val st=status
          val res=responseAs[ErrorRes]
          res.error_code shouldEqual HTTP_CONFLICT_409
          status.intValue() shouldEqual (HTTP_CONFLICT_409)
        }
    }


  }
}
