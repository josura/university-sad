package testSpark
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD
import scala.util.Try

object Main extends SparkSessionWrapper {

    def parseDouble(s: String): Option[Double] = Try { s.toDouble }.toOption

    def parseText(s:String):Array[Double] = {
        s.split("\t").drop(1).map(rating => {
            val tmpDoub = parseDouble(rating)
            if (tmpDoub.isDefined){
                tmpDoub.get
            } else 0.0
        })
    }

    def myzip(matrix1: RDD[Array[Double]],matrix2: RDD[Array[Double]]): RDD[(Array[Double], Array[Double])] = {
        val matrix1Keyed: RDD[(Long, Array[Double])] = matrix1.zipWithIndex().map { case (n, i) => i -> n }
        val matrix2Keyed: RDD[(Long, Array[Double])] = matrix2.zipWithIndex().map { case (n, i) => i -> n }
        matrix1Keyed.join(matrix2Keyed).sortByKey().map(_._2)
    }

    def invertRDD(matrix:RDD[Array[Double]]):RDD[Array[Double]] = {
        matrix.flatMap(arr => arr.zipWithIndex.map({ case (value:Double,key:Int) =>(key,value)}) ).groupByKey().map({ case (key,iterator) => iterator.toArray})
    }

    def subtractRDD(matrix:RDD[Array[Double]], matrix2:RDD[Array[Double]]):RDD[Array[Double]] = {
        myzip(matrix,matrix2).map(moreArrays => {
            moreArrays._1.zip(moreArrays._2).map(ratingsValues =>ratingsValues._2-ratingsValues._1)
        })
    }

    def multiplyRDD(matrix:RDD[Array[Double]], multiplier:Double):RDD[Array[Double]] = {
        matrix.map(arr => {
            arr.map(x => x * multiplier)
        })
    }
    def gradientRDD(matrix:RDD[Array[Double]], parameters:RDD[Array[Double]], vmat:RDD[Array[Double]]):RDD[Array[Double]]={
        val Rcardinal = matrix.count() * matrix.first().size
        val zippedParVmat = myzip(parameters,vmat).map(moreArr => moreArr._1 zip moreArr._2)
        val estimatedRatings = matrix.zipWithIndex().map({case (rowReviews,key) => {
                    val tmpArr = zippedParVmat.map(arrParVmat =>{
                        arrParVmat.zip(rowReviews).map(x => (x._1._1 * x._1._2 * x._2) ).sum
                    }).collect()
                    (key,tmpArr)
                }
            }).sortByKey().values
        val errorMat = myzip(invertRDD(matrix),estimatedRatings).map(moreArrays => {
            moreArrays._1.zip(moreArrays._2).map(ratingsValues =>ratingsValues._2-ratingsValues._1)
        })

        val denominator = Rcardinal * math.sqrt(errorMat.flatMap(arr=> arr.map(doubValue => doubValue*doubValue)).sum())


        val tmp = matrix.map(arr => {
            errorMat.map(errorArr=>{
                errorArr.zip(arr).map(result => result._1 * result._2).sum
            }).collect()
        })

        myzip(tmp,vmat).map(moreArrays => {
            moreArrays._1.zip(moreArrays._2).map(x => x._1 * x._2 / denominator)
        })


    }

    def modifiedGradientRDD(matrix:RDD[Array[Double]], parameters:RDD[Array[Double]], vmat:RDD[Array[Double]]):(RDD[Array[Double]],Double)={
        val Rcardinal = matrix.count() * matrix.first().size
        val zippedParVmat = myzip(parameters,vmat).map(moreArr => moreArr._1 zip moreArr._2).collect()
        val estimatedRatings = matrix.zipWithIndex().map({case (rowReviews,key) => {
                    val tmpArr = zippedParVmat.map(arrParVmat =>{
                        arrParVmat.zip(rowReviews).map(x => (x._1._1 * x._1._2 * x._2) ).sum
                    })
                    (key,tmpArr)
                }
            }).sortByKey().values

        val errorMat = subtractRDD(estimatedRatings,matrix).collect()

        val denominator = Rcardinal * math.sqrt(errorMat.flatMap(arr=> arr.map(doubValue => doubValue*doubValue)).reduce(_ + _))
        val rmse = (1.0/Rcardinal) * math.sqrt(errorMat.flatMap(arr=> arr.map(doubValue => doubValue*doubValue)).reduce(_ + _))


        val tmp = matrix.map(arr => {
            errorMat.map(errorArr=>{
                errorArr.zip(arr).map(result => result._1 * result._2).sum
            })
        })

        val gradientMat = myzip(tmp,vmat).map(moreArrays => {
            moreArrays._1.zip(moreArrays._2).map(x => x._1 * x._2 / denominator)
        })

        (gradientMat, rmse)
    }

    def RMSE(matrix:RDD[Array[Double]], parameters:RDD[Array[Double]], vmat:RDD[Array[Double]]):Double = {
        val Rcardinal = matrix.count() * matrix.first().size

        val zippedParVmat = parameters.zip(vmat).map(moreArr => moreArr._1 zip moreArr._2)
        val estimatedRatings = matrix.zipWithIndex().map({case (rowReviews,key) => {
                    val tmpArr = zippedParVmat.map(arrParVmat =>{
                        arrParVmat.zip(rowReviews).map(x => (x._1._1 * x._1._2 * x._2) ).sum
                    }).collect()
                    (key,tmpArr)
                }
            }).sortByKey().values
        val errorMat = invertRDD(matrix).zip(estimatedRatings).map(moreArrays => {
            moreArrays._1.zip(moreArrays._2).map(ratingsValues =>ratingsValues._2-ratingsValues._1)
        })

        (1.0/Rcardinal) * math.sqrt(errorMat.flatMap(arr=> arr.map(doubValue => doubValue*doubValue)).sum())
    }

    

    def main(args: Array[String]): Unit = {

        try{
            //val spark = SparkSession.builder().master("local").appName("spark session").getOrCreate()
            val data = spark.sparkContext.textFile("data/e_admat_dgc.txt").
            mapPartitionsWithIndex({
                (idx, iter) => if (idx == 0) iter.drop(1) else iter
            }).map(parseText(_)).cache()

            val degreesUsers = data.flatMap(arr => { //?
                arr.zipWithIndex.map({
                    case (value,key) if value > 0=> (key,1)
                    case (value,key) if value <= 0=> (key,0)
                })
            }).
            reduceByKey(_ + _).sortByKey().values.collect()


            val degreesObjects = data.map(arr => { //?
                arr.count(value => value>0)
            }).collect()


            val collectedData = data.collect 
            
            val vMatrix = data.zipWithIndex().map({case (arrX,key) => {
                    val tmpArr = collectedData.map(arrY =>{
                        arrX.zip(arrY).zip(degreesUsers).map(x => (x._1._1 + x._1._2)/x._2).filter(!_.isNaN()).sum
                    })
                    (key,tmpArr)
                }
            }).sortByKey().values

            var parameters = data
            var rmse = 1.0
            val scheduleCoefficient = 0.000001

            for(i <- 1 to 100){
                val gradientTuple = modifiedGradientRDD(data,parameters,vMatrix)
                rmse = gradientTuple._2
                parameters =subtractRDD(parameters ,multiplyRDD(gradientTuple._1,scheduleCoefficient)) 
                println("finished " + i + " iteration with an RMSE of " + rmse)
            }

            println("RMSE of the estimated ratings after GD is: " + rmse)


            
        } catch {
            case e: Exception => {
                System.err.print("Exception in main")
                e.printStackTrace()

            }
            case _: Throwable => println("Got some other kind of Throwable exception")
        } finally {
            spark.stop()
        }

    }
}
