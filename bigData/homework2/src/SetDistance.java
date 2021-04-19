
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.stream.*;

import java.io.IOException;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.net.URI;

//dependencies to determine if two files have same content
import org.apache.commons.io.FileUtils;
import java.io.File;

public class SetDistance extends Configured implements Tool {

    private SetDistance() {

    }

    public static class DistanceMapper extends Mapper<Text, Text, Text, DoubleWritable> {

        ArrayList<String> pointIds=null;
        ArrayList<ArrayList<Double>> pointCoordinates=null;

        public double pointDistance(ArrayList<Double> point1,ArrayList<Double> point2){
            double diff_square_sum = 0.0;
            for (int i = 0; i < point1.size(); i++) {
                diff_square_sum += (point1.get(i) - point2.get(i)) * (point1.get(i) - point2.get(i));
            }
            return Math.sqrt(diff_square_sum);
        }


        public void setup(Context context)throws IOException, InterruptedException{
            pointIds = new ArrayList<String>();
            pointCoordinates = new ArrayList<ArrayList<Double>>();
            URI []cacheFiles = context.getCacheFiles();
		
            if(cacheFiles!=null && cacheFiles.length>0)
            {
                try
                {
                    //lists of identifiers and coordinates in cache 
                    String linePoint ="";
                    FileSystem fsPoints = FileSystem.get(context.getConfiguration());
                    Path getFilePathPoints = new Path(cacheFiles[0].toString());  
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fsPoints.open(getFilePathPoints)));
                    
                    while((linePoint = reader.readLine())!=null)
                    {
                        String []words = linePoint.split("[,]");
                        
                        if(!words[0].isEmpty()){
                            String pId = words[0].substring(1);
                            pointIds.add(pId);
                            String[] strCoords = words[1].substring(0,words[1].length()-1).split(" ");

                            pointCoordinates.add(new ArrayList<Double>(Arrays.stream(strCoords).map(Double::parseDouble).collect(Collectors.toList()))); 
                        }
                            
                    }

                    
                }catch(Exception e) {
                    System.out.println("Unable to read the file in mapper (setup)");
                    System.err.println("Unable to read the file in mapper (setup)");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

            String pointCordsStr = value.toString().replaceAll("[()]","");

            try{
                if(!pointCordsStr.isEmpty()){
                    String[] strCoords = pointCordsStr.split(" ");
                    Text resultkey = new Text();
                    DoubleWritable resultvalue = new DoubleWritable();

                    ArrayList<Double> tmpPointCoord = new ArrayList<Double>(Arrays.stream(strCoords).map(Double::parseDouble).collect(Collectors.toList()));
                    for(int i=0;i<pointCoordinates.size();i++){
                        double tmpDist = pointDistance(tmpPointCoord,pointCoordinates.get(i));
                        resultkey.set(pointIds.get(i));
                        resultvalue.set(tmpDist);
                        
                        //Test
                        // System.err.println("point P to point S : P" + key.toString() +" S"+ pointIds.get(i) + " Dist"+ tmpDist);
                        // System.out.println("point P to point S : P" + key.toString() +" S"+ pointIds.get(i) + " Dist"+ tmpDist);

                        context.write(resultkey, resultvalue);
                    }
                }
            } catch(Exception e){
                System.err.println("Exception during construction of parts in map ");
                System.out.println("Exception during construction of parts in map ");
                System.err.println("Value of key in mapper is ("+key.toString());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static class DistanceReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            //System.err.println("Value of key in reducer is "+key.toString());
            try{
                DoubleWritable result = new DoubleWritable();
                double minimumDist=Double.MAX_VALUE;
                for (DoubleWritable val : values) {
                    if(minimumDist>val.get())
                        minimumDist = val.get();
                }
                result.set(minimumDist);
                context.write(key, result);
                
            }catch(Exception e){
                System.err.println("Exception during construction of parts in reduce ");
                System.out.println("Exception during construction of parts in reduce ");
                System.err.println("Value of key in reducer is " + key.toString());
                e.printStackTrace();
                System.exit(1);
            }

        }
    }


    private void deleteOutputPath(Path output) throws IOException {
        FileSystem fs = FileSystem.get(getConf());
        if (fs.exists(output)) {
            fs.delete(output, true);
        }
    }

    private boolean fileIsEmpty(Path file)throws IOException{
        FileSystem fs = FileSystem.get(getConf());
            if (fs.exists(file) && (fs.getFileStatus(file).getLen() > 0 ) ) {
                return false;
            }
            else {return true;}
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: SetDistance <Set1> <Set2> <outputPath>");
            System.err.println("example Usage: SetDistance /inputs/P.txt /inputs/S.txt /outputs");
            System.err.println("input files should have the following structure: (pointID,pointCoordinates)");
            System.err.println("with pointID as a String, and pointCoordinates as a list of values, an example of pointCoordinates is the following:");
            System.err.println("\t 0.4 0.5 0.5 0.1 12.7");
            System.err.println("all points should have the same number of dimensions-coordinates");
            System.err.println("\n----------------------------------------------------------------\n");
            System.err.println("This program will get the distance from Set1 to every point in Set2\nthe distance is computed as \n\td(y,P)=min(d(y,x))\nthat is the distance from a point in S to Set P is computed by taking the minimum of the distances of the point in S and every point in P");
            System.exit(2);
        }
        Configuration conf = getConf();
        conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", ",");
        

        Job job = Job.getInstance(conf, "Setdistance");
        try{
            Path set2Path = new Path(args[1]);
            job.addCacheFile(set2Path.toUri());
        }catch(Exception e){
            System.out.println("File to cache not found");
            System.exit(1);
        }
        job.setJarByClass(SetDistance.class);
        job.setMapperClass(DistanceMapper.class);
        //job.setCombinerClass(DistanceReducer.class);
        job.setReducerClass(DistanceReducer.class);
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));//args[0] for first input of articles, args[1] for metadata

        Path outputPath = new Path(args[2]);
        deleteOutputPath(outputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
        
        job.waitForCompletion(true);

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new SetDistance(), args);
        System.exit(res);
    }

}
