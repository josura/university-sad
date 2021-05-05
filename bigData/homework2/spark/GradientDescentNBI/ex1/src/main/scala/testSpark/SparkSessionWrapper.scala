package testSpark

import org.apache.spark.sql.SparkSession

trait SparkSessionWrapper extends Serializable {

  lazy val spark: SparkSession = {
    val sp = SparkSession.builder().master("local[*]").appName("spark session").getOrCreate()
    sp.sparkContext.setLogLevel("ERROR")
    sp
  }

}