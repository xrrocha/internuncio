package shuar

import shuar.Shuar.close

import scala.util.Try

trait Source[I]:
  def open(): Iterator[I]
end Source

trait Mapper[I, O]:
  def map(item: I): O
end Mapper

trait Reducer[O, R]:
  def reduce(items: Iterator[O]): R
end Reducer

trait Shuar[I, O, R](source: Source[I], mapper: Mapper[I, O], reducer: Reducer[O, R]):
  def mapReduce(): R =
    try {
      reducer.reduce(source.open().map(mapper.map))
    } finally {
      close(source, mapper, reducer)
    }
end Shuar
object Shuar:
  private def close(objs: Any*): Unit =
    objs
      .filter(_.isInstanceOf[AutoCloseable])
      .map(_.asInstanceOf[AutoCloseable])
      .foreach(c => Try(c.close()))
end Shuar


