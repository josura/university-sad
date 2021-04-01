
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

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

public class PCYarticle extends Configured implements Tool {

    private PCYarticle() {

    }

    public static class FirstPassMapper extends Mapper<Text, Text, Text, Text> {

        public void setup(Context context)throws IOException,InterruptedException{

            
        }

        private long hash(String stri, String strj, long k) {
            int i = stri.hashCode();
            int j = strj.hashCode();
            return Math.abs(i ^ j) % k;
        }

        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            //String genesAndTitles = value.toString();
            //String[] genesAndTitlesArray = genesAndTitles.split("\t");

            String[] genes = key.toString().split(",");
            String[] titles = value.toString().split("\\|\\|\\|\\|");

            Configuration conf = context.getConfiguration();
            try{
                for(int i=0;i<genes.length;i++){
                    for(int j = 0; j<titles.length; j++){
                        context.write(new Text(titles[j]),new Text("1"+genes[i]));
                    }
                    //creating cache
                    long cacheDim = Long.parseLong(conf.get("k"));

                    for(int j = i+1; j<titles.length; j++){
                        String outKey = "cache:::" + hash(titles[i],titles[j],cacheDim);
                        //System.err.println("Final Key in map of 1st mapper for cache:" + outKey);

                        for(int k = 0; k < genes.length;k++){
                        //System.err.println("Final Value in map of 1st mapper cache:" + "1:::"+titles[k]);
                            context.write(new Text(outKey),new Text("1"));
                        }
                    }
                }
            } catch(Exception e) {
                System.err.println("Exception during construction of parts in map of 1st mapper");
                System.out.println("Exception during construction of parts in map of 1st mapper");
                System.err.println("Key in map of 1st mapper :" + key.toString());
                System.out.println("Key in map of 1st mapper :" + key.toString());
                System.err.println("Value in map of 1st mapper :" + value.toString());
                System.out.println("Value in map of 1st mapper :" + value.toString());
                System.out.println("number of genes to find" + genes.length);
                System.err.println("number of genes to find" + genes.length);
                e.printStackTrace();
                System.exit(1);
            }
        }

        
    }

    public static class FirstPassReducer extends Reducer<Text, Text, Text, Text> {

        private final static Text one = new Text("1");
        private final static Text zero = new Text("0");

        private MultipleOutputs<Text,Text> mos;
        
        @Override
        public void setup(Context context) throws IOException, InterruptedException {
                    mos = new MultipleOutputs(context);
        }

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            long param = Long.parseLong(conf.get("support"));
            //System.err.println("Value of key in reducer is ("+key.toString());
            try{
                if( key.toString().contains("cache::")){
                    long count = 0;
                    for (Text t : values) {        
                        count += Long.parseLong(t.toString());    
                    }
                    //System.err.println("writing to cache in reducer :"+key.toString() + " & " + ((count>= param) ? 1 : 0));
                    
                    //mos.write("hashCacheFrequent", new Text(key.toString().substring(8)), ((count>= param) ? one : zero));   
                    mos.write("hashCacheFrequentArticles", new Text(key.toString().substring(8)),new Text("") );   
                } else {
                    int count = 0;
                    String genes = "";
                    for (Text t : values) {        
                        count += Long.parseLong(t.toString().substring(0,1));
                        genes += t.toString().substring(1) + ",";
                    }
                    if(count>=param){

                        //System.err.println("writing to file in reducer :"+key.toString() + " & " + 1 );
                        mos.write("singletFrequentArticles", key, new Text(genes.substring(0,genes.length() - 1)));//((count>= param) ? new Text("" + count + ) : zero));
                    }
                }
            }catch(Exception e){
                System.err.println("Exception during construction of parts in map of 1st mapper");
                System.out.println("Exception during construction of parts in map of 1st reducer");
                System.err.println("Value of key is ("+key.toString());
                e.printStackTrace();
                System.exit(1);
            }
            

            
        }
    }

    public static class SecondPassMapper extends Mapper<Object, Text, Text, Text> {

        HashSet<Long> cache2ndPass=null;
        HashSet<String> frequentItemset1=null;


        //setup to read from cachefile written in 1st pass.
        public void setup(Context context)throws IOException, InterruptedException{
            cache2ndPass = new HashSet<Long>();
            frequentItemset1 = new HashSet<String>();
            URI []cacheFiles = context.getCacheFiles();
		
            if(cacheFiles!=null && cacheFiles.length>0)
            {
                try
                {
                    //cache for 2 items hash
                    String line ="";
                    FileSystem fs = FileSystem.get(context.getConfiguration());
                    Path getFilePath = new Path(cacheFiles[0].toString());  
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(getFilePath)));
                    
                    while((line = reader.readLine())!=null)
                    {
                        String []words = line.split("[\t ]");
                        
                        for(int i=0;i<words.length;i++)
                        {   
                            if(!words[i].isEmpty())
                                cache2ndPass.add(Long.parseLong(words[i])); //add the words to HashSet
                            
                        }
                            
                    }

                    //hashset for frequent itemset of size 1
                    String linetitles ="";
                    FileSystem fstitles = FileSystem.get(context.getConfiguration());
                    Path getFilePathTitles = new Path(cacheFiles[1].toString());  
                    
                    BufferedReader readertitles = new BufferedReader(new InputStreamReader(fstitles.open(getFilePathTitles)));
                    
                    while((linetitles = readertitles.readLine())!=null)
                    {
                        String []words = linetitles.split("[\t]");
                        
                        if(!words[0].isEmpty())
                            frequentItemset1.add(words[0]); //add the single frequent gene to HashSet
                        
                            
                    }

                    
                }catch(Exception e) {
                    System.out.println("Unable to read the file in 2nd mapper (setup)");
                    System.err.println("Unable to read the file in 2nd mapper (setup)");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        private long hash(String stri, String strj, long k) {
            int i = stri.hashCode();
            int j = strj.hashCode();
            return Math.abs(i ^ j) % k;
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String[] genes = key.toString().split(",");
            String[] titles = value.toString().split("\\|\\|\\|\\|");

            //TEST

                // System.err.println("Key in map of 2nd mapper :" + key.toString());
                // System.out.println("Key in map of 2nd mapper :" + key.toString());
                // System.err.println("Value in map of 2nd mapper :" + value.toString());
                // System.out.println("Value in map of 2nd mapper :" + value.toString());

            Configuration conf = context.getConfiguration();
            long cacheDim = Long.parseLong(conf.get("k"));
            try{
                //subsets of length 2 and control over cache and frequent itemset of size 1
                for(int i=0;i<titles.length;i++){
                    for(int j = i+1; j<titles.length; j++){
                        long hash = hash(titles[i],titles[j],cacheDim);                
                        //System.err.println("hash :" + hash + " for titles" + titles[i]+","+titles[j]);

                        if(cache2ndPass.contains(hash) && frequentItemset1.contains(titles[i]) && frequentItemset1.contains(titles[j])){
                            String titlesRecord = titles[i]+"||||"+titles[j];

                            //System.err.println("dentro con :" + hash + " for titles " + titlesRecord);

                            for(int k = 0; k < genes.length;k++){
                                String outValue = "1" + genes[k];
                                //System.err.println("Final Value in map of 2nd mapper: " + outValue);
                                context.write(new Text(titlesRecord), new Text(outValue) );
                            }
                        }
                    }
                }
            } catch(Exception e){
                System.err.println("Exception during construction of parts in map of 2nd mapper");
                System.out.println("Exception during construction of parts in map of 2nd mapper");
                System.err.println("Value of key in second pass mapper is ("+key.toString());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static class SecondPassReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            
            Configuration conf = context.getConfiguration();
            long param = Long.parseLong(conf.get("support"));
            try{
                int count = 0;
                String genes = "";
                for (Text t : values) {
                    //System.err.println("Value of one of the values in reducer of second pass is "+t.toString());
                    if(true){//t.toString().isEmpty() == false){//substring(0,1) == "1"){
                        count += Long.parseLong(t.toString().substring(0,1));
                        genes += t.toString().substring(1) + ",";
                    }
                }
                if(count>=param){

                    //System.err.println("writing to file in reducer :"+key.toString() + " & " + 1 );
                    context.write(key, new Text(genes.substring(0,genes.length() - 1)));//((count>= param) ? new Text("" + count + ) : zero));
                }
                
            }catch(Exception e){
                System.err.println("Exception during construction of parts in reduce of 2nd reducer");
                System.out.println("Exception during construction of parts in reduce of 2nd reducer");
                System.err.println("Value of key in 2nd pass reducer is "+key.toString());
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

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: PCY <ItemsetsFile> <OutputFile> <#support> <#maxNumberofItemInItemset> [#cacheDimension]");
            System.err.println("example Usage: PCY /outputs/invertedIndex /outputs/PCY 2 8");
            System.exit(2);
        }
        Configuration conf = getConf();
        conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
        conf.set("support", args[2]);
        if (args.length >= 4) {
            conf.set("k", args[3]);
        } else {
            conf.set("k", "1000");
        }
        //FIRST PASS
        Job job1 = Job.getInstance(conf, "PCYarticle-first");

        job1.setJarByClass(PCYarticle.class);
        job1.setMapperClass(FirstPassMapper.class);
        job1.setCombinerClass(FirstPassReducer.class);
        job1.setReducerClass(FirstPassReducer.class);
        job1.setInputFormatClass(KeyValueTextInputFormat.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        
        Path genesOutputPath = new Path(args[1] + "/firstpassarticles");
        deleteOutputPath(genesOutputPath);
        MultipleOutputs.addNamedOutput(job1, "singletFrequentArticles", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job1, "hashCacheFrequentArticles", TextOutputFormat.class, Text.class, Text.class);
        FileOutputFormat.setOutputPath(job1, genesOutputPath);
        
        job1.waitForCompletion(true);
        

        //SECOND PASS
        Job job2 = Job.getInstance(conf, "secondPass");
        try{
            Path FI1 = new Path(args[1] + "/firstpassarticles/singletFrequentArticles-m-00000");
            Path cacheSecondPass = new Path(args[1] + "/firstpassarticles/hashCacheFrequentArticles-m-00000");
            job2.addCacheFile(cacheSecondPass.toUri());
            job2.addCacheFile(FI1.toUri());
        }catch(Exception e){
            System.out.println("File to cache not found");
            System.exit(1);
        }
        job2.setJarByClass(PCYarticle.class);
        job2.setMapperClass(SecondPassMapper.class);
        job2.setReducerClass(SecondPassReducer.class);
        job2.setInputFormatClass(KeyValueTextInputFormat.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job2, new Path(args[0]));//args[0] for first input of articles, args[1] for metadata

        Path outputPath2ndPass = new Path(args[1] + "/FrequentItemset2Articles");
        deleteOutputPath(outputPath2ndPass);
        FileOutputFormat.setOutputPath(job2, outputPath2ndPass);
        
        job2.waitForCompletion(true);
        

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new PCYarticle(), args);
        System.exit(res);
    }

}
