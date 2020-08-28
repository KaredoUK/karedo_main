package api

import core.{FailureResponse, SuccessResponse, ResponseWithFailure}
import core.common.RequestValidationChaining
import org.specs2.mutable.Specification

class RequestValidationChainingSpec extends Specification {

  class Validator( validations: (String => Option[String])* )extends RequestValidationChaining {
    def validate(input: String): ResponseWithFailure[String, String] = withValidations(input)(validations: _*) { SuccessResponse(_) }
  }

  class StringToValidate(value: String) {
    def using(validations: (String => Option[String])*) = (new Validator(validations:_*)).validate(value)
  }

  def validate( value: String ) = new StringToValidate(value)

  "RequestValidationChaining" should {
    "Execute successful response if all validations are returning None" in {
      validate("a") using(
        { value => if(value.length > 0) None else Some("Not an empty string") },
        { value => if(value.contains("a")) None else Some("string should contain a letter 'a'") }
      )  shouldEqual SuccessResponse("a")
    }

    "Fail if any validation is unsuccessful" in {
      validate("b") using(
        { value => if(value.length > 0) None else Some("Not an empty string") },
        { value => if(value.contains("a")) None else Some("string should contain a letter 'a'") }
        )  shouldEqual FailureResponse("string should contain a letter 'a'")
    }

    "Fail at FIRST unsuccessful validation" in {
      var calledValidations = Set[String]()
      validate("b") using(
        { value =>
          calledValidations += "NotEmpty"
          if(value.length > 0) None else Some("Not an empty string")
        },
        {
          value =>
            calledValidations += "With A"
            if(value.contains("a")) None else Some("string should contain a letter 'a'")
        },
        { _ =>
          calledValidations += "AlwaysFailing"
          Some("Always failing")
        }
       )  shouldEqual FailureResponse("string should contain a letter 'a'")
    }

    "Not call validations after failing one " in {
      var calledValidations = Set[String]()
      validate("b") using(
        { value =>
          calledValidations += "NotEmpty"
          if(value.length > 0) None else Some("Not an empty string")
        },
        {
          value =>
            calledValidations += "With A"
            if(value.contains("a")) None else Some("string should contain a letter 'a'")
        },
        { _ =>
          calledValidations += "AlwaysFailing"
          Some("Always failing")
        }
      )

      calledValidations shouldEqual Set("NotEmpty", "With A")
    }
  }
}
