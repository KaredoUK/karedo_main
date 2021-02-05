package rules

import com.parallelai.wallet.datamanager.data.{UserOfferInteraction, UserBrandInteraction}

object ComputePoints {

  def GetInteractionPoints(interaction: UserBrandInteraction) = {
    interaction.interaction.toLowerCase match {
      case "share" => 10
      case "like" => 5
      case "unlike" => -1
      case "view" => 0
      case "detail" => 1
    }
  }
  def GetInteractionPoints(interaction: UserOfferInteraction) = {
    interaction.interaction.toLowerCase match {
      case "share" => 15
      case "like" => 6
      case "unlike" => -1
      case "view" => 0
      case "detail" => 2
    }
  }


}
