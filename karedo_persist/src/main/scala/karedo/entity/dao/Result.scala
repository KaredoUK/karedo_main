package karedo.entity.dao

sealed trait Result[+KOT,+OKT] {
  def map[T] (f: OKT => T): Result[KOT, T]
}
final case class OK[+KOT,+OKT](val content: OKT) extends Result[KOT,OKT]{
  def map[T] (f: OKT => T): Result[KOT, T] = OK( f(content) )
}
final case class KO[+KOT,+OKT](val failure: KOT) extends Result[KOT,OKT]{
  def map[T] (f: OKT => T): Result[KOT, T] = KO[KOT,T](failure)
}
