package karedo.entity.dao

sealed trait Result[+KOT,+OKT] {
  def map[T] (f: OKT => T): Result[KOT, T]
  def isOK: Boolean
  def isKO: Boolean
  def get: OKT
  def err: KOT
}
final case class OK[+KOT,+OKT](val content: OKT) extends Result[KOT,OKT]{
  def map[T] (f: OKT => T): Result[KOT, T] = OK( f(content) )
  def isOK = true
  def isKO = false
  def get = content
  def err = throw new Exception("no error found")
}
final case class KO[+KOT,+OKT](val failure: KOT) extends Result[KOT,OKT]{
  def map[T] (f: OKT => T): Result[KOT, T] = KO[KOT,T](failure)
  def isOK = false
  def isKO = true
  def get = throw new Exception("no data found")
  def err = failure
}
