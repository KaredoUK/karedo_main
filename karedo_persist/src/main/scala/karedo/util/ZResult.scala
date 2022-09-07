package karedo.util

sealed abstract class ZResult[+A, +B] extends Product with Serializable {


  def isKO: Boolean =
    this match {
      case ZKO(_) => true
      case ZOK(_) => false
    }

  def isOK: Boolean =
    this match {
      case ZKO(_) => false
      case ZOK(_) => true
    }

  /** Catamorphism. Run the first given function if left, otherwise, the second given function. */
  def fold[X](l: A => X, r: B => X): X =
    this match {
      case ZKO(a) => l(a)
      case ZOK(b) => r(b)
    }

  /** Binary functor map on this disjunction. */
  def bimap[C, D](f: A => C, g: B => D): (C ZResult D) =
    this match {
      case ZKO(a) => ZKO(f(a))
      case ZOK(b) => ZOK(g(b))
    }

  /** Run the given function on the left value. */
  def leftMap[C](f: A => C): (C ZResult B) =
    this match {
      case ZKO(a) => ZKO(f(a))
      case b @ ZOK(_) => b
    }


  /** Map on the right of this disjunction. */
  def map[D](g: B => D): (A ZResult D) =
    this match {
      case ZOK(a)     => ZOK(g(a))
      case b @ ZKO(_) => b
    }


  /** Run the side-effect on the right of this disjunction. */
  def foreach(g: B => Unit): Unit =
    bimap(_ => (), g)


  /** Bind through the right of this disjunction. */
  def flatMap[AA >: A, D](g: B => (AA ZResult D)): (AA ZResult D) =
    this match {
      case a @ ZKO(_) => a
      case ZOK(b) => g(b)
    }

  /** Fold on the right of this disjunction. */
  def foldRight[Z](z: => Z)(f: (B, => Z) => Z): Z =
    this match {
      case ZKO(_) => z
      case ZOK(a) => f(a, z)
    }

  /** Return an empty list or list with one element on the right of this disjunction. */
  def toList: List[B] =
    this match {
      case ZKO(_) => Nil
      case ZOK(b) => b :: Nil
    }

  /** Return an empty stream or stream with one element on the right of this disjunction. */
  def toStream: Stream[B] =
    this match {
      case ZKO(_) => Stream()
      case ZOK(b) => Stream(b)
    }

  /** Return an empty option or option with one element on the right of this disjunction. Useful to sweep errors under the carpet. */
  def toOption: Option[B] =
    this match {
      case ZKO(_) => None
      case ZOK(b) => Some(b)
    }

  /** Return the right value of this disjunction or the given default if left. Alias for `|` */
  def getOrElse[BB >: B](x: => BB): BB =
    this match {
      case ZKO(_) => x
      case ZOK(b) => b
    }

  def get[BB >: B]: BB =
    this match {
      case ZKO(_) => throw new IndexOutOfBoundsException
      case ZOK(b) => b
    }

  def err[AA >: A]: AA =
    this match {
      case ZKO(a) => a
      case ZOK(_) => throw new IndexOutOfBoundsException
    }

  /** Return the right value of this disjunction or run the given function on the left. */
  def valueOr[BB >: B](x: A => BB): BB =
    this match {
      case ZKO(a) => x(a)
      case ZOK(b) => b
    }

  /** Return this if it is a right, otherwise, return the given value. Alias for `|||` */
  def orElse[C, BB >: B](x: => C ZResult BB): C ZResult BB =
    this match {
      case ZKO(_) => x
      case right@ ZOK(_) => right
    }



  /** Run the given function on the left and return right with the result. */
  def recover[BB >: B](pf: PartialFunction[A, BB]): (A ZResult BB) = this match {
    case ZKO(a) if (pf isDefinedAt a) => ZOK(pf(a))
    case _ => this
  }

  /** Run the given function on the left and return the result. */
  def recoverWith[AA >: A, BB >: B](pf: PartialFunction[AA, AA ZResult BB]): (AA ZResult BB) = this match {
    case ZKO(a) if (pf isDefinedAt a) => pf(a)
    case _ => this
  }

}
/** A left disjunction
  *
  * Often used to represent the failure case of a result
  */
final case class ZKO[+A](a: A) extends (A ZResult Nothing)

/** A right disjunction
  *
  * Often used to represent the success case of a result
  */
final case class ZOK[+B](b: B) extends (Nothing ZResult B)

object ZResult  {

  /** Construct a left disjunction value. */
  def left[A, B]: A => A ZResult B =
    ZKO(_)

  /** Construct a right disjunction value. */
  def right[A, B]: B => A ZResult B =
    ZOK(_)


}
