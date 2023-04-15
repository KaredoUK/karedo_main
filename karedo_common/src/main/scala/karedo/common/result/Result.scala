package karedo.common.result

sealed abstract class Result[+A, +B] extends Product with Serializable {


  def isKO: Boolean =
    this match {
      case KO(_) => true
      case OK(_) => false
    }

  def isOK: Boolean =
    this match {
      case KO(_) => false
      case OK(_) => true
    }

  /** Catamorphism. Run the first given function if left, otherwise, the second given function. */
  def fold[X](l: A => X, r: B => X): X =
    this match {
      case KO(a) => l(a)
      case OK(b) => r(b)
    }

  /** Binary functor map on this disjunction. */
  def bimap[C, D](f: A => C, g: B => D): (C Result D) =
    this match {
      case KO(a) => KO(f(a))
      case OK(b) => OK(g(b))
    }

  /** Run the given function on the left value. */
  def leftMap[C](f: A => C): (C Result B) =
    this match {
      case KO(a) => KO(f(a))
      case b @ OK(_) => b
    }


  /** Map on the right of this disjunction. */
  def map[D](g: B => D): (A Result D) =
    this match {
      case OK(a)     => OK(g(a))
      case b @ KO(_) => b
    }


  /** Run the side-effect on the right of this disjunction. */
  def foreach(g: B => Unit): Unit =
    bimap(_ => (), g)


  /** Bind through the right of this disjunction. */
  def flatMap[AA >: A, D](g: B => (AA Result D)): (AA Result D) =
    this match {
      case a @ KO(_) => a
      case OK(b) => g(b)
    }

  /** Fold on the right of this disjunction. */
  def foldRight[Z](z: => Z)(f: (B, => Z) => Z): Z =
    this match {
      case KO(_) => z
      case OK(a) => f(a, z)
    }

  /** Return an empty list or list with one element on the right of this disjunction. */
  def toList: List[B] =
    this match {
      case KO(_) => Nil
      case OK(b) => b :: Nil
    }

  /** Return an empty stream or stream with one element on the right of this disjunction. */
  def toStream: Stream[B] =
    this match {
      case KO(_) => Stream()
      case OK(b) => Stream(b)
    }

  /** Return an empty option or option with one element on the right of this disjunction. Useful to sweep errors under the carpet. */
  def toOption: Option[B] =
    this match {
      case KO(_) => None
      case OK(b) => Some(b)
    }

  /** Return the right value of this disjunction or the given default if left. Alias for `|` */
  def getOrElse[BB >: B](x: => BB): BB =
    this match {
      case KO(_) => x
      case OK(b) => b
    }

  def get: B =
    this match {
      case KO(_) => throw new IndexOutOfBoundsException
      case OK(b) => b
    }

  def err: A =
    this match {
      case KO(a) => a
      case OK(_) => throw new IndexOutOfBoundsException
    }

  /** Return the right value of this disjunction or run the given function on the left. */
  def valueOr[BB >: B](x: A => BB): BB =
    this match {
      case KO(a) => x(a)
      case OK(b) => b
    }

  /** Return this if it is a right, otherwise, return the given value. Alias for `|||` */
  def orElse[C, BB >: B](x: => C Result BB): C Result BB =
    this match {
      case KO(_) => x
      case right@ OK(_) => right
    }



  /** Run the given function on the left and return right with the result. */
  def recover[BB >: B](pf: PartialFunction[A, BB]): (A Result BB) = this match {
    case KO(a) if (pf isDefinedAt a) => OK(pf(a))
    case _ => this
  }

  /** Run the given function on the left and return the result. */
  def recoverWith[AA >: A, BB >: B](pf: PartialFunction[AA, AA Result BB]): (AA Result BB) = this match {
    case KO(a) if (pf isDefinedAt a) => pf(a)
    case _ => this
  }

}
/** A left disjunction
  *
  * Often used to represent the failure case of a result
  */
final case class KO[+A](a: A) extends (A Result Nothing)

/** A right disjunction
  *
  * Often used to represent the success case of a result
  */
final case class OK[+B](b: B) extends (Nothing Result B)

object Result  {

  /** Construct a left disjunction value. */
  def left[A, B]: A => A Result B =
    KO(_)

  /** Construct a right disjunction value. */
  def right[A, B]: B => A Result B =
    OK(_)


}
