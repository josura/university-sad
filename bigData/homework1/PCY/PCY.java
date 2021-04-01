
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

public class PCY extends Configured implements Tool {

    private PCY() {

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
                        //System.err.println("Final Key in map of 1st mapper normal:" + genes[i]);
                        context.write(new Text(genes[i]),new Text("1"+titles[j]));
                    }
                    //creating cache
                    long cacheDim = Long.parseLong(conf.get("k"));

                    for(int j = i+1; j<genes.length; j++){
                        String outKey = "cache:::" + hash(genes[i],genes[j],cacheDim);
                        //System.err.println("Final Key in map of 1st mapper for cache:" + outKey);

                        for(int k = 0; k < titles.length;k++){
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
                    mos.write("hashCacheFrequent", new Text(key.toString().substring(8)),new Text("") );   
                } else {
                    int count = 0;
                    String titles = "";
                    for (Text t : values) {        
                        count += Long.parseLong(t.toString().substring(0,1));
                        titles += t.toString().substring(1) + "||||";
                    }
                    if(count>=param){

                        //System.err.println("writing to file in reducer :"+key.toString() + " & " + 1 );
                        mos.write("singletFrequent", key, new Text(titles.substring(0,titles.length() - 4)));//((count>= param) ? new Text("" + count + ) : zero));
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
                    String linegene ="";
                    FileSystem fsgenes = FileSystem.get(context.getConfiguration());
                    Path getFilePathgene = new Path(cacheFiles[1].toString());  
                    
                    BufferedReader readergene = new BufferedReader(new InputStreamReader(fsgenes.open(getFilePathgene)));
                    
                    while((linegene = readergene.readLine())!=null)
                    {
                        String []words = linegene.split("[\t]");
                        
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
                for(int i=0;i<genes.length;i++){
                    for(int j = i+1; j<genes.length; j++){
                        long hash = hash(genes[i],genes[j],cacheDim);                
                        //System.err.println("hash :" + hash + " for genes" + genes[i]+","+genes[j]);

                        if(cache2ndPass.contains(hash) && frequentItemset1.contains(genes[i]) && frequentItemset1.contains(genes[j])){
                            String geniRecord = genes[i]+","+genes[j];

                            //System.err.println("dentro con :" + hash + " for genes " + geniRecord);

                            for(int k = 0; k < titles.length;k++){
                                String outValue = "1" + titles[k];
                                //System.err.println("Final Value in map of 2nd mapper: " + outValue);
                                context.write(new Text(geniRecord), new Text(outValue) );
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
            //System.err.println("Value of key in reducer of second pass is "+key.toString());
            try{
                int count = 0;
                String titles = "";
                for (Text t : values) {
                    count += Long.parseLong(t.toString().substring(0,1));
                    titles += t.toString().substring(1) + "||||";
                }
                if(count>=param){

                    //System.err.println("writing to file in reducer :"+key.toString() + " & " + 1 );
                    context.write(key, new Text(titles.substring(0,titles.length() - 4)));//((count>= param) ? new Text("" + count + ) : zero));
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

    

    public static class NthMapper extends Mapper<Text, Text, Text, Text> {
        HashSet<String> FIPrevious = null;

        public void setup(Context context)throws IOException,InterruptedException{
            FIPrevious = new HashSet<String>();

            URI []cacheFiles = context.getCacheFiles();
		
            if(cacheFiles!=null && cacheFiles.length>0)
            {
                try
                {
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
                                FIPrevious.add(words[i]); //add the words to ArrayList
                            
                        }
                            
                    }
                    
                
                    
                }catch(Exception e) {
                    System.out.println("Unable to read the file in");
                    System.err.println("Unable to read the file");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        private String[] subsets(String[] words,long subsetSize){
            ArrayList<String> retArr = new ArrayList<String>();
            int counter = 0;
            long arrSize = words.length;
            for (int i = 0; i < (1<<arrSize); i++)
            {
                counter = 0;
                String partSet="";
                // Print current subset
                for (int j = 0; j < arrSize; j++)
                {
                    if ((i & (1 << j)) > 0){
                        counter++;
                        partSet += words[j] + ",";
                    }
                }
                if(partSet.length()>1 &&  counter == subsetSize){
                    partSet = partSet.substring(0,partSet.length()-1);
                    retArr.add(partSet);
                }

            }
            Object[] tempArr = retArr.toArray();
            return Arrays.copyOf(tempArr,tempArr.length,String[].class);

        }

        private String[] subsetsWithControlFI(String[] words,long subsetSize){
            ArrayList<String> retArr = new ArrayList<String>();
            long arrSize = words.length;
            for (int i = 0; i < (1<<arrSize); i++)
            {
                String partSet="";
                int counter = 0;
                // Print current subset
                for (int j = 0; j < arrSize; j++)
                {
                    if ((i & (1 << j)) > 0){
                        counter++;
                        partSet += words[j] + ",";
                    }
                }
                if(partSet.length()>1 && counter == subsetSize){
                    partSet = partSet.substring(0,partSet.length()-1);
                    String[] underGenes = partSet.split(",");
                    //System.err.println("subset of length 3: " + partSet);
                    String[] subsetsMinors = subsets(underGenes,subsetSize-1);
                    boolean goodmatch=true;
                    for(int k = 0 ; k < subsetsMinors.length && goodmatch;k++){
                        if(!FIPrevious.contains(subsetsMinors[k]))goodmatch=false;
                    }
                    if(goodmatch){
                        //System.err.println("subset of length 3 and good: " + partSet);
                        retArr.add(partSet);
                    }
                }

            }
            Object[] tempArr = retArr.toArray();
            return Arrays.copyOf(tempArr,tempArr.length,String[].class);
        }

        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            String[] genes = key.toString().split(",");
            String[] titles = value.toString().split("\\|\\|\\|\\|");
            Configuration conf = context.getConfiguration();
            long FIsubsetSize = Long.parseLong(conf.get("FIsubsetSize"));
            if(FIsubsetSize>genes.length)return;
            try{
                String[] candidates = subsetsWithControlFI(genes,FIsubsetSize);
                //System.err.println("Genes in map of nth mapper: " + key.toString());
                //System.err.println("candidates in map of nth mapper: " + candidates.length);


                for(int i = 0; i < candidates.length; i++){
                    for(int k = 0; k < titles.length;k++){
                        String outValue = "1"+ titles[k];
                        //System.err.println("Final Value in map of nth mapper: " + outValue);
                        context.write(new Text(candidates[i]), new Text(outValue) );
                    }
                }

            } catch(Exception e) {
                System.err.println("Exception during construction of parts in map in Nth pass");
                System.out.println("Exception during construction of parts in map in Nth pass");
                System.out.println("number of n-1th " + FIPrevious.size());
                System.err.println("number of n-1th " + FIPrevious.size());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static class NthReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            
            Configuration conf = context.getConfiguration();
            long param = Long.parseLong(conf.get("support"));
            //System.err.println("Value of key in reducer of second pass is "+key.toString());
            try{
                int count = 0;
                String titles = "";
                for (Text t : values) {
                    //System.err.println("Value of one of the values in reducer of second pass is "+t.toString());
                    count += Long.parseLong(t.toString().substring(0,1));
                    titles += t.toString().substring(1) + "||||";
                   
                }
                if(count>=param){

                    //System.err.println("writing to file in reducer :"+key.toString() + " & " + 1 );
                    context.write(key, new Text(titles.substring(0,titles.length() - 4)));//((count>= param) ? new Text("" + count + ) : zero));
                }
                
            }catch(Exception e){
                System.err.println("Exception during construction of parts in reduce of nth reducer");
                System.out.println("Exception during construction of parts in reduce of nth reducer");
                System.err.println("Value of key is "+key.toString());
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
        Job job1 = Job.getInstance(conf, "PCY-first");

        job1.setJarByClass(PCY.class);
        job1.setMapperClass(FirstPassMapper.class);
        job1.setCombinerClass(FirstPassReducer.class);
        job1.setReducerClass(FirstPassReducer.class);
        job1.setInputFormatClass(KeyValueTextInputFormat.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        
        Path genesOutputPath = new Path(args[1] + "/firstpass");
        deleteOutputPath(genesOutputPath);
        MultipleOutputs.addNamedOutput(job1, "singletFrequent", TextOutputFormat.class, Text.class, Text.class);
        MultipleOutputs.addNamedOutput(job1, "hashCacheFrequent", TextOutputFormat.class, Text.class, Text.class);
        FileOutputFormat.setOutputPath(job1, genesOutputPath);
        
        job1.waitForCompletion(true);
        

        //SECOND PASS
        Job job2 = Job.getInstance(conf, "secondPass");
        try{
            Path FI1 = new Path(args[1] + "/firstpass/singletFrequent-m-00000");
            Path cacheSecondPass = new Path(args[1] + "/firstpass/hashCacheFrequent-m-00000");
            job2.addCacheFile(cacheSecondPass.toUri());
            job2.addCacheFile(FI1.toUri());
        }catch(Exception e){
            System.out.println("File to cache not found");
            System.exit(1);
        }
        job2.setJarByClass(PCY.class);
        job2.setMapperClass(SecondPassMapper.class);
        job2.setReducerClass(SecondPassReducer.class);
        job2.setInputFormatClass(KeyValueTextInputFormat.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);
        job2.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job2, new Path(args[0]));//args[0] for first input of articles, args[1] for metadata

        Path outputPath2ndPass = new Path(args[1] + "/FrequentItemset2");
        deleteOutputPath(outputPath2ndPass);
        FileOutputFormat.setOutputPath(job2, outputPath2ndPass);
        
        job2.waitForCompletion(true);
        int i = 3; //first step for iteration
        boolean notFinished = true;
        //NTH PASS
        while(notFinished){
            String subsetsize = "" + i;
            conf.set("FIsubsetSize",subsetsize);
            Job jobN = Job.getInstance(conf, "FrequentItemset" + i);
            try{
                Path FIn = new Path(args[1] + "/FrequentItemset"+ (i-1) + "/part-r-00000");  //where to take the previous step?
                jobN.addCacheFile(FIn.toUri());
            }catch(Exception e){
                System.err.println("File to cache not found at pass " + i);
                System.exit(1);
            }
            jobN.setJarByClass(PCY.class);
            jobN.setMapperClass(NthMapper.class);
            jobN.setReducerClass(NthReducer.class);
            jobN.setInputFormatClass(KeyValueTextInputFormat.class);
            jobN.setOutputKeyClass(Text.class);
            jobN.setOutputValueClass(Text.class);
            jobN.setOutputFormatClass(TextOutputFormat.class);

            FileInputFormat.addInputPath(jobN, new Path(args[0]));//args[0] for first input of articles, args[1] for metadata

            Path outputPathN = new Path(args[1] + "/FrequentItemset" + i);
            deleteOutputPath(outputPathN);
            FileOutputFormat.setOutputPath(jobN, outputPathN);
            
            jobN.waitForCompletion(true);

            //CONTROL IF THE OUTPUTS ARE EMPTY
            String currentFIfilename = args[1] + "/FrequentItemset"+ i + "/part-r-00000";

            Path filePath = new Path(currentFIfilename);
            if(fileIsEmpty(filePath)){
                System.out.println("Frequent Itemsets of " + i + " elements are not in any transaction");
                System.out.println("Stopping execution");
                deleteOutputPath(outputPathN);
                notFinished=false;
            } else{
                i++;    
            }
        }

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new PCY(), args);
        System.exit(res);
    }

}
