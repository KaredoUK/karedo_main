package parallelai.wallet.entity



/**
 * Created by pakkio on 18/12/2014.
 */
object Collections  {

  val USER = "UserAccount"
  val BRAND = "Brand"
  val OFFER = "Offer"

}

object InteractionType extends Enumeration {
  type InteractionType = Value
  val Like = Value("Like")
  val Click = Value("Click")
}