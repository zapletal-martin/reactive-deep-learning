import org.apache.spark.SparkContext

case class Person(name: String, age: Int)

object CatalystMain extends App {
  override def main (args: Array[String]) {
      val sc = new SparkContext("local[4]", "Catalyst")
      val sqlContext = new org.apache.spark.sql.SQLContext(sc)

      val data = List(
        Person("John", 25),
        Person("Mark", 30),
        Person("Josh", 35),
        Person("Harry", 40),
        Person("Fred", 45))

      import sqlContext.implicits._

      val people = sc.parallelize(data)
      //val filtered = people.filter(p => p.age >= 20 && p.age <= 35).map(_.name).filter(_ == "John")

      val peopleDF = people.toDF()
      peopleDF.schema.printTreeString()
      println(peopleDF.schema)
      val filtered2 = peopleDF.select(peopleDF("name"), peopleDF("age")).where(peopleDF("age") >= 20 && peopleDF("age") <= 35).where(peopleDF("name") === "John")

    //val typeCheck = peopleDF.where(peopleDF("age") === "John").select(peopleDF("name") * 2)

    /*peopleDF.registerTempTable("people")
    val filtered3 = sqlContext.sql("SELECT name FROM (SELECT age, name FROM people) p WHERE p.age >= 20 AND p.age <= 35 AND p.name = \"John\"")*/

    //val filtered3 = sqlContext.sql("SELECT name FROM people WHERE age >= 20 AND age <= 35")

    //typeCheck.foreach(println)
    /*println("START")
    println(filtered3.queryExecution)
    println("STOP")*/
    //filtered3.explain(true)
    //filtered3.foreach(println)
    filtered2.foreach(println)
    //filtered3.foreach(println)
  }
}
