package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.UserMobile
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar145_EnterCode_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[Kar145_EnterCode_actor])

  def exec(request:Kar145Req): Result[Error, APIResponse] = {

    logger.info(s"Mobile Verify\nmsisdn: ${request.msisdn}\nsms_code: ${request.sms_code}\nApplicationID: ${request.application_id}")

    val msisdn = request.msisdn
    val sms_code = request.sms_code
    val password = request.password

    dbUserApp.find(request.application_id) match {
      case OK(userApp) => {
        val account_id = userApp.account_id
        dbUserAccount.find(account_id) match {
          case OK(userAccount) => {
            userAccount.mobile.filter(e => e.msisdn == msisdn) match {
              case eh::et => {
                eh.sms_code match {
                  case Some(e_code) => {
                    if( e_code == sms_code ) {
                      // EMail code matches
                      val restMobiles = userAccount.mobile.filter(e => ! e.msisdn.equals(msisdn) )

                      val newMobile = eh.copy(valid = true, ts_validated = Some(now))
                      val newUserAccount = userAccount.copy(
                        mobile = List(newMobile) ++ restMobiles,
                        password = Some(password),
                        temp = false )

                      dbUserAccount.update(newUserAccount) match {
                        case KO(error) => return MAKE_ERROR(error)
                        case _ =>
                      }

                      dbUserMobile.find(msisdn) match {
                        case OK(userMobile) => {
                          if( userMobile.account_id == account_id ) {
                            KO(Error(s"Verification failed. Mobile already verified for $msisdn in $account_id"))
                          } else {
                            KO(Error(s"Verification failed. Mobile $msisdn already registered to ${userMobile.account_id} bu trying for $account_id"))
                          }
                        }
                        case KO(_) => {
                          dbUserMobile.insertNew(UserMobile(msisdn, account_id, true, now, now)) match {
                            case KO(error) => MAKE_ERROR(error)
                            case OK(_) => {
                              // @TODO: Test - All the Sale & Transfer Stuff
                              dbMobileSale.find(msisdn) match {
                                case OK(mobileSale) => {
                                  mobileSale.sale_ids.foreach(sale_id => {
                                    dbSale.find(sale_id) match {
                                      case OK(sale) => {
                                        moveKaredosBetweenAccounts(sale.sender_id, account_id, Some(sale.karedos),
                                          s"Completing Transfer from ${sale.sender_id} to ${account_id} for ${sale.karedos} Karedos")

                                        dbSale.update(sale.copy(receiver_id = account_id, status = TRANS_STATUS_COMPLETE,
                                          ts_updated = now, ts_completed = Some(now)))

                                        sendSMS(sale.sender_id, s"Success. Transfer of ${sale.karedos} Karedos to ${account_id} Completed.")
                                        sendSMS(account_id, s"Success. Transfer of ${sale.karedos} Karedos from ${sale.sender_id} (${sale.sender_name}) Completed.")
                                      }
                                      case KO(error) => logger.error(error)
                                    }
                                  })
                                }
                                case KO(_) =>
                              }

                              OK(APIResponse(Kar145Res(account_id).toJson.toString, HTTP_OK_200))
                            }
                          }
                        }
                      }
                    } else {
                      KO(Error(s"Verification failed. SMS Code does not match $msisdn in $account_id code $sms_code"))
                    }
                  }
                  case None => KO(Error(s"Verification failed. SMS Code not present is $msisdn in account $account_id"))
                }
              }
              case _ => KO(Error(s"Verification failed. Mobile $msisdn not registered to account $account_id"))
            }
          }
          case KO(error) => KO(Error(s"Verification failed. UserAccount not found for $account_id"))
        }
      }
      case KO(error) => MAKE_ERROR(error)
    }
  }
}