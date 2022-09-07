package karedo.util

// copied from scalaz disjunction
sealed trait NResult[+A,+B] {
  def map[T] (f: B => T): NResult[A, T]

  def isOK: Boolean
  def isKO: Boolean
  def get: B
  def err: A
}
final case class NOK[+A,+B](val content: B) extends NResult[A,B]{
  // maps on the right meaning will evaluate the rest of the function
  def map[T] (f: B => T): NResult[A, T] = NOK( f(content) )

  def isOK = true
  def isKO = false
  def get = content
  def err = throw new Exception("no error found")
}
final case class NKO[+A,+B](val failure: A) extends NResult[A,B]{
  def map[T] (f: B => T): NResult[A, T] = NKO[A,T](failure)

  def isOK = false
  def isKO = true
  def get = throw new Exception("no data found")
  def err = failure
}


