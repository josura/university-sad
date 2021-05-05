package testSpark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.util.Try


object DataframeMain extends SparkSessionWrapper{
    def main(){
        val textFile = spark.read.format("com.databricks.spark.csv").
                option("delimiter", "\t").
                load("data/e_admat_dgc.txt").
                drop("_c0")    //loading the data in a dataframe

            import spark.implicits._   //For getting the single rows as Strings

            val castedDF = textFile.columns.foldLeft(textFile)((current, c) => current.withColumn(c, col(c).cast("Double")))



            println("reading data file in ")
            textFile show 10

            val dfFinal = textFile.
            map(f=>{                                                             //A map is done to trasform the string dataframe in a dataframe of 2 columns
                val elements = f.getString(0).split(" - - |GET |POST | HTTP")    //Other than GET requests, even POST request are analyzed as they contribute to the number of visit for a user
                (elements(0),elements(2))                                        //two new rows are returned, that is the visitor and the route of the visit
            })
    }
  
}
