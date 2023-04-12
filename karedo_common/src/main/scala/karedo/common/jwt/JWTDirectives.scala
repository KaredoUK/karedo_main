package karedo.common.jwt

/**
  * Created by charaj on 20/05/2017.
  */
import java.time.Instant
import java.util.Date

import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import com.nimbusds.jose.crypto.{MACSigner, MACVerifier}
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader, JWSObject, Payload}
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTClaimsSet.Builder
import net.minidev.json.JSONObject

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.util.Try

/**
  * Provides utilities for signing and verification by the JSON Web Token (JWT).
  */
trait JwtDirectives {

  import akka.http.scaladsl.server.Directives._

  /**
    * An `AsyncAuthenticator` which returns a JWS object.
    *
    * Useful if combined with `BasicAuth` and an `authenticate` directive.
    * An inner route of an `authenticate` directive will receive a JWS object
    * (`JWSObject`) built by `claimBuilder` and signed by `signer`.
    *
    * @param authenticator
    * The `AsyncAuthenticator` which authenticates a given pair of a user
    * and a password.
    * @param claimBuilder
    * Builds a claim set from a result of `authenticator`.
    * @param signer
    * Signs a result of `claimBuilder`.
    * @param executionContext
    * The execution context to run a `Future` returned from `authenticator`.
    */
  def jwtAuthenticator[T](authenticator: AsyncAuthenticator[T])
                         (implicit claimBuilder: JwtClaimBuilder.SubjectExtrator[T],
                          signer: JWTClaimsSet => Option[JWSObject],
                          executionContext: ExecutionContext): AsyncAuthenticator[JWSObject] =
    authenticator(_) map {
      case Some(t) => claimBuilder(t) flatMap signer
      case None => None
    }

  /**
    * Verifies a token sent with an HTTP request.
    *
    * A token should be sent through the `Authorization` header like,
    * {{{
    * Authorization: Bearer JWT
    * }}}
    *
    * Thanks to [[JwtAuthorizationMagnet]], this directive will end up
    * the following form,
    * {{{
    * authorizeToken[T](privilege: JWTClaimsSet => Option[T])
    *   (implicit verifier: JWSObject => Option[JWTClaimsSet]): Directive1[T]
    * }}}
    *
    * And will
    * 1. Obtain the value associated with "Authorization" header.
    * 1. Extract a JWT from the "Authorization" header value.
    * 1. Verify the JWT with `verifier` and extract a claim set.
    * 1. Apply `privilege` to the claim set.
    * 1. Supply the result from `privilege` to the inner route.
    *
    * Will reject,
    * - if no "Authorization" header is specified,
    * - or if the "Authorization" header does not specify a JWT,
    * - or if `verifier` cannot verify the JWT,
    * - or if `privilege` rejects the claims set.
    *
    */
  def authorizeToken[T](magnet: JwtAuthorizationMagnet[T]): Directive1[T] = {
    val prefix = "Bearer "

    val extractJwt = (value: String) => if (value.startsWith(prefix)) {
      Try(Some(JWSObject.parse(value.substring(prefix.length)))) getOrElse None
    } else {
      None
    }

    optionalHeaderValueByName("Authorization") flatMap { optionalHeader =>
      (for {
        header <- optionalHeader
        jws <- extractJwt(header)
        claimsSet <- magnet.verifier(jws)
        privileged <- magnet.privilege(claimsSet)
      } yield provide(privileged)) getOrElse reject(AuthorizationFailedRejection)
    }
  }
}

object JwtDirectives extends JwtDirectives

/**
  * Magnet which attracts parameters necessary for the `authorizeToken`
  * directive.
  */
case class JwtAuthorizationMagnet[T](privilege: JWTClaimsSet => Option[T])(implicit val verifier: JWSObject => Option[JWTClaimsSet])

object JwtAuthorizationMagnet {
  /**
    * Implicitly converts a given privilege function into
    * a [[JwtAuthorizationMagnet]].
    *
    * @param privilege
    * Returns a context dependent object if a given claim set has
    * a privilege otherwise `None`.
    */
  implicit def fromPrivilege[T](privilege: JWTClaimsSet => Option[T])(implicit verifier: JWSObject => Option[JWTClaimsSet]): JwtAuthorizationMagnet[T] =
    JwtAuthorizationMagnet(privilege)
}

/**
  * Provides signature signer and verifier for JWS.
  *
  * @param algorithm
  * The name of the signature algorithm.
  * @param secret
  * The secret key for signature.
  */
case class JwtSignature(algorithm: JWSAlgorithm, secret: String) {
  /** The common header of JWS objects. */
  private[this] val header = new JWSHeader(algorithm)

  /** The common signer for JWS objects. */
  private[this] val signer = new MACSigner(secret.getBytes)

  /** The common verifier for JWS objects. */
  private[this] val verifier = new MACVerifier(secret.getBytes)

  /**
    * The implicit signer for JWS objects.
    *
    * Signs a given claim set and returns a signed JWS object.
    */
  implicit def jwtSigner(claim: JWTClaimsSet): JWSObject = {
    val jwsObject = new JWSObject(header, new Payload(claim.toJSONObject))
    jwsObject.sign(signer)
    jwsObject
  }

  implicit def jwtSignerOp(claim: JWTClaimsSet): Option[JWSObject] = Some(jwtSigner(claim))

  /**
    * The implicit verifier for JWS objects.
    *
    * Verifies a given JWS object and returns a contained claim set.
    */
  implicit def jwtVerifier(token: JWSObject): Option[JWTClaimsSet] = if (token.verify(verifier)) {
    Try(Some(JWTClaimsSet.parse(token.getPayload.toJSONObject))) getOrElse None
  } else None
}

/**
  * A claim builder.
  *
  * You can chain multiple claim builders by `&&` operator.
  */
trait JwtClaimBuilder[T] extends JwtClaimBuilder.SubjectExtrator[T] {
  self =>

  import JwtClaimBuilder.SubjectExtrator

  /**
    * Builds a claim.
    *
    * @param input
    * The input for the claim builder.
    * Usually an output from an authenticator.
    * @return
    * The claim build from `input`.
    */
  def apply(input: T): Option[JWTClaimsSet]

  /**
    * Chains a specified claim builder function after this claim builder.
    *
    * Claims appended by `after` have precedence over the claims built by this
    * claim builder.
    *
    * @param after
    * The claim builder which appends claims after this claim builder.
    * @return
    * A new claim builder which builds a claim set by this claim builder and
    * `after`.
    */
  def &&(after: SubjectExtrator[T]): SubjectExtrator[T] = input => mergeClaims(self(input), after(input))

  /**
    * Merges specified two claim sets.
    *
    * Claims in `second` have precedence over claims in `first`.
    *
    * @param first
    * The first claim set.
    * @param second
    * The second claim set.
    * @return
    * A new claim set which has claims in both `first` and `second`.
    * `None` if `first` or `second` is `None`.
    */
  private def mergeClaims(first: Option[JWTClaimsSet], second: Option[JWTClaimsSet]) =
    for {
      claims1 <- first
      claims2 <- second
    } yield {
      val newClaims = new JSONObject(claims1.toJSONObject)
      newClaims.merge(claims2.toJSONObject)
      JWTClaimsSet.parse(newClaims)
    }
}

object JwtClaimBuilder {

  import scala.concurrent.duration.Duration

  type SubjectExtrator[T] = T => Option[JWTClaimsSet]

  /**
    * Returns a claim builder which sets the "exp" field to an expiration time.
    *
    * @param duration
    * The valid duration of a JWT.
    * The minimum resolution is one minute.
    */
  def claimExpiration[T](duration: Duration): SubjectExtrator[T] = input => {
    val validUntil = new Date(Instant.now().plusSeconds(duration.toSeconds).toEpochMilli)
    Some(new Builder().expirationTime(validUntil).build())
  }

  /**
    * Returns a claim builder which sets the "iss" field to a specified string.
    *
    * @param issuer
    * The issuer of a JWT.
    */
  def claimIssuer[T](issuer: String): SubjectExtrator[T] = input => Some(new Builder().issuer(issuer).build())

  /**
    * Returns a claim builder which sets the "sub" field.
    *
    * @param subject
    * A function which extracts the subject from an input.
    */
  def claimSubject[T](subject: T => String): SubjectExtrator[T] = input => Some(new Builder().subject(subject(input)).build())

  /**
    * Implicitly converts a claim builder function into a [[JwtClaimBuilder]].
    */
  implicit def toJwtClaimBuilder[T](f: SubjectExtrator[T]): JwtClaimBuilder[T] =
    new JwtClaimBuilder[T] {
      override def apply(input: T): Option[JWTClaimsSet] = f(input)
    }
}

/**
  * A privilege which verifies a claim set.
  *
  * Instance of this trait can be passed as a `privilege` argument of the
  * `authorizeToken` directive.
  */
trait JwtClaimVerifier extends JwtClaimVerifier.PrivilegeFunction {
  self =>

  import JwtClaimVerifier.PrivilegeFunction

  /**
    * Verifies a specified claim set.
    *
    * @param claims
    * The claim set to be verified.
    * @return
    * The verified claim set. `None` if `claim` is not verified.
    */
  def apply(claims: JWTClaimsSet): Option[JWTClaimsSet]

  /**
    * Chains a specified privilege function after this claim verifier.
    *
    * `after` will not be applied if this claim verifier fails.
    *
    * @param after
    * The privilege function to be applied after this claim verifier.
    * @return
    * A new privilege which applies this claim verifier and then `after`.
    */
  def &&[T](after: PrivilegeFunction): PrivilegeFunction = claims =>
    for {
      first <- self(claims)
      second <- after(first)
    } yield second
}

/** Companion object of [[JwtClaimVerifier]]. */
object JwtClaimVerifier {
  type PrivilegeFunction = JWTClaimsSet => Option[JWTClaimsSet]

  /**
    * Returns a privileging function which verifies the expiration time.
    *
    * If a specified claim set does not have "exp" field, verification of it
    * fails; i.e., returns `None`.
    */
  def verifyNotExpired: PrivilegeFunction = claims => {
    val isValid = (until: Date) => until.toInstant.isAfter(Instant.now())

    Option(claims.getExpirationTime) filter isValid map (_ => claims) orElse None
  }

  /** Implicitly converts a claim verifier into a [[JwtClaimVerifier]]. */
  implicit def toJwtClaimVerifier(f: PrivilegeFunction): JwtClaimVerifier =
    new JwtClaimVerifier {
      override def apply(claims: JWTClaimsSet): Option[JWTClaimsSet] = f(claims)
    }
}

