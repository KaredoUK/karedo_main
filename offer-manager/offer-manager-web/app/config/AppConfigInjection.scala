package config

import com.escalatesoft.subcut.inject.{NewBindingModule, BindingModule}

trait AppConfigInjection {
  lazy val bindingModule : BindingModule = NewBindingModule.newBindingModuleWithConfig(AppConfigPropertySource())

}
