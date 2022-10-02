package shuar

import munit.FunSuite

class TestShuar extends FunSuite {
  test("Shuar works") {
    object source extends Source[String] {
      override def open(): Iterable[String] = Iterable("one", "two", "three")
    }
    object mapper extends Mapper[String, String] {
      override def map(item: String): String = item.toUpperCase
    }
    object reducer extends Reducer[String, String] {
      override def reduce(items: Iterable[String]): String = items.mkString(", ")
    }
    object shuar extends Shuar(source, mapper, reducer)
    val result = shuar.mapReduce()
    assert(result == "ONE, TWO, THREE")
  }
}
