//import java.util.UUID
//
//import com.escalatesoft.subcut.inject.BindingModule
//import com.escalatesoft.subcut.inject.NewBindingModule._
//import com.parallelai.wallet.datamanager.data._
//import controllers.MainController._
//import parallelai.wallet.config.AppConfigPropertySource
//import api.DataManagerRestClient
//
//object Main extends App {
//
//  println("Testing scaffold")
//
//  private val source: AppConfigPropertySource = AppConfigPropertySource()
//  //val bindingModule: BindingModule = newBindingModuleWithConfig(source)
//  implicit val _ = bindingModule
//  val client=new DataManagerRestClient()
//
//  val registration = new RegistrationRequest(UUID.randomUUID(), Some("331"), Some("name@gmail.com"))
//
//  println(client.register(registration))
// 
//}