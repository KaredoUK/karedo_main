package rules

import com.parallelai.wallet.datamanager.data.UserBrandInteraction

object AddPoints {

  def GetInteractionPoints(interaction: UserBrandInteraction) = {
    interaction.interaction.toLowerCase match {
      case "share" => 10
      case "like" => 5
      case "unlike" => -1
      case "view" => 0
      case "detail" => 1
    }
  }

}
