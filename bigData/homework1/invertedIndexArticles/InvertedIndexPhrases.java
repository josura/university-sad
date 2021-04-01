
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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.util.HashSet;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.net.URI;


public class InvertedIndexPhrases extends Configured implements Tool {

    private InvertedIndexPhrases() {

    }

    public static class TestMapper extends Mapper<LongWritable, Text, Text, Text> {
        HashSet<String> features=null;

        HashSet<String> geni=null;

        public void setup(Context context)throws IOException,InterruptedException{
            geni = new HashSet<String>();
            features = new HashSet<String>();

            features.add("AB  -");
            features.add("TI  -");

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
                                geni.add(words[i].toUpperCase()); //add the words to ArrayList
                            
                        }
                            
                    }
                    //System.out.println("number of genes findable is "+geni.size());
                    
                
                    
                }catch(Exception e) {
                    System.out.println("Unable to read the file");
                    System.err.println("Unable to read the file");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String article = value.toString();//.replace("\n    ", " ");
            StringTokenizer itr = new StringTokenizer(article,"\n");
            String TI="",AB="";
            boolean hasTI=false,hasAB=false,finishedParsing=false;
            while (itr.hasMoreTokens() && !finishedParsing) {
                String record = itr.nextToken();
                if(!record.isEmpty()){
                    String feature = record.substring(0,5);
                    //String featurevalue = record.substring(6);
                    //Text chiave = new Text(key.toString());
                    //if(features.contains(feature)){
                    //if(feature.contains("AB") || feature.contains("TI")){
                    //    finalString+=
                    //}
                    if(feature.contains("TI")){
                        hasTI = true;
                        TI = record.substring(5,record.length()-1) ;
                    } else if(feature.contains("AB")){
                        hasAB = true;
                        AB = record.substring(5,record.length()-1);                        
                    } else if(feature.contains("    ") ){
                        if(hasTI && !hasAB)
                            TI += record.substring(5,record.length()-1);
                        else if(hasAB)
                            AB += record.substring(5);
                    } else if(hasAB && hasTI)finishedParsing=true;
                }
            }
            if(hasTI && hasAB){
                //Returning the two things directly
                //Text testo = new Text(TI + "---DEL---" + AB);
                //context.write(new Text(key.toString()), testo);
                try{

                    //TESTING
                    // if(AB.contains("YAP1")){
                    //     System.out.println("YAP1 found");
                    //     for(int i=0;i<words.length;i++)
                    //     {   
                    //         if(words[i].toUpperCase().contains("YAP1"))
                    //         {
                    //             System.out.println(words[i] + "YAP1 found");
                    //             if(geni.contains("YAP1"))System.out.println("geni contains YAP1");
                    //             if(geni.contains(words[i].toUpperCase()))System.out.println("geni contains the word");
                                

                    //         }
                    //         if(geni.contains(words[i].toUpperCase()))
                    //         {
                    //             System.out.println("YAP1 in genes");
                    //         }
                    //     }
                    // }
                    String[] phrases = AB.split("\n");
                    for(int k = 0; k<phrases.length; k++){

                        String []words = phrases[k].split("[ /,;:()-]");
                        SortedSet<String> ts = new TreeSet<String>();
                        
                        //System.out.println("number of genes findable is "+geni.size());        
                        for(int i=0;i<words.length;i++)
                        {   
                            if(geni.contains(words[i].toUpperCase()))
                            {
                                ts.add(words[i].toUpperCase()); //add the genes
                            }
                        }


                        String[] articlesGenes = ts.stream().toArray(String[]::new);
                        //System.out.println("number of genes in article "+TI+ "is "+ts.size());
                        int numArticleGenes = ts.size();
        
                        // subsets of genes in article
                        // for (int i = 1; i < (1<<numArticleGenes); i++)
                        // {
                        //     String partSet="<<<";
                        //     // Print current subset
                        //     for (int j = 0; j < numArticleGenes; j++)
                        //     {
                        //         if ((i & (1 << j)) > 0)
                        //             partSet += articlesGenes[j] + ",";
                        //     }
                        //     partSet += "<<<";
                        //     context.write(new Text(partSet),new Text(TI));
                        // }

                        //all genes in abstract
                        String genesString="";
                        for(int i = 0;i<numArticleGenes;i++){
                            genesString += articlesGenes[i] + ",";
                        }
                        //genesString += articlesGenes[numArticleGenes-1];
                        if(numArticleGenes>=1)
                            context.write(new Text(genesString.substring(0, genesString.length() - 1)),new Text(TI));
                    }

                } catch(Exception e) {
                    System.err.println("Exception during construction of parts in map");
                    System.out.println("Exception during construction of parts in map");
                    System.out.println("number of genes to find" + geni.size());
                    System.err.println("number of genes to find" + geni.size());
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public static class TestReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String titles="";
            for (Text t : values) {
                if(!t.toString().isEmpty())
                    titles += t.toString() + "||||";
            }
            context.write(key,new Text(titles.substring(0, titles.length() - 4)));
            
        }
    }

    public static class GenesMapper extends Mapper<Object, Text, Text, Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String record = value.toString();
            String[] parts = record.split("\t", -1);
            context.write(new Text(parts[1]), new Text(""));
        }
    }

    public static class GenesReducer extends Reducer<Text, Text, Text, Text> {

        public static Text emptyText = new Text(""); 

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            
            context.write(key,emptyText);

        }
    }

    /*public static class ArticleMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        HashSet<String> features=null;

        public void setup(Context context)throws IOException,InterruptedException{
            features = new HashSet<String>();

            features.add("AB  -");
            features.add("TI  -");
            
        }

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String record = value.toString();
            if(!record.isEmpty()){
                String feature = record.substring(0,5);
                String featurevalue = record.substring(6);
                //Text chiave = new Text(key.toString());
                if(features.contains(feature)){
                //if(feature.contains("AB") || feature.contains("TI")){
                    
                    context.write(key, new Text(feature + featurevalue));
                }
            }
        }
    }

    public static class ArticleReducer extends Reducer<LongWritable, Text, LongWritable, Text> {

        public static Text emptyText = new Text(""); 

        public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text t : values) {
                context.write(key,t);
            }
        }
    }*/

    

    public static class FinalMapper extends Mapper<LongWritable, Text, Text, Text> {
         HashSet<String> features=null;

        HashSet<String> geni=null;

        public void setup(Context context)throws IOException,InterruptedException{
            geni = new HashSet<String>();
            features = new HashSet<String>();

            features.add("AB  -");
            features.add("TI  -");

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
                                geni.add(words[i].toUpperCase()); //add the words to ArrayList
                            
                        }
                            
                    }
                    
                
                    
                }catch(Exception e) {
                    System.out.println("Unable to read the file");
                    System.err.println("Unable to read the file");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String article = value.toString();
            StringTokenizer itr = new StringTokenizer(article,"\n");
            String TI="",AB="";
            boolean hasTI=false,hasAB=false,finishedParsing=false;
            while (itr.hasMoreTokens() && !finishedParsing) {
                String record = itr.nextToken();
                if(!record.isEmpty()){
                    String feature = record.substring(0,5);
                    if(feature.contains("TI")){
                        hasTI = true;
                        TI = record.substring(5,record.length()-1) ;
                    } else if(feature.contains("AB")){
                        hasAB = true;
                        AB = record.substring(5,record.length()-1);                        
                    } else if(feature.contains("    ") ){
                        if(hasTI && !hasAB)
                            TI += record.substring(5,record.length()-1);
                        else if(hasAB)
                            AB += record.substring(5);
                    } else if(hasAB && hasTI)finishedParsing=true;
                }
            }
            if(hasTI && hasAB){

                try{
                    String[] phrases = AB.split("\n");
                    for(int k = 0; k<phrases.length; k++){
                        String []words = phrases[k].split("[ /,;:()-]");
                        SortedSet<String> ts = new TreeSet<String>();
                            
                        for(int i=0;i<words.length;i++)
                        {   
                            if(geni.contains(words[i]))//.toUpperCase()))
                            {
                                ts.add(words[i].toUpperCase()); //add the genes
                            }
                        }


                        String[] articlesGenes = ts.stream().toArray(String[]::new);
                        int numArticleGenes = ts.size();

                        //all genes in abstract
                        String genesString="";
                        for(int i = 0;i<numArticleGenes;i++){
                            genesString += articlesGenes[i] + ",";
                        }
                        if(numArticleGenes>=1)
                            context.write(new Text(genesString.substring(0,genesString.length() - 1)),new Text(TI));
                    }

                } catch(Exception e) {
                    System.err.println("Exception during construction of parts in map");
                    System.out.println("Exception during construction of parts in map");
                    System.out.println("number of genes to find" + geni.size());
                    System.err.println("number of genes to find" + geni.size());
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    public static class FinalReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String titles="";
            for (Text t : values) {
                if(!t.toString().isEmpty())
                    titles += t.toString() + "||||";
            }
            context.write(key,new Text(titles.substring(0, titles.length() - 4)));
            
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
        if (args.length != 3) {
            System.err.println("Usage: InvertedIndex <ArticleFile> <GeneFile> <OutputFile>");
            System.exit(2);
        }
        Configuration confGenes = getConf();
        //GENES MAPPER
        Job jobGenes = Job.getInstance(confGenes, "GenesMapper");

        // jobGenes.addCacheFile(new Path(args[1]).toUri());
        jobGenes.setJarByClass(InvertedIndexPhrases.class);
        jobGenes.setMapperClass(GenesMapper.class);
        jobGenes.setCombinerClass(GenesReducer.class);
        jobGenes.setReducerClass(GenesReducer.class);
        jobGenes.setInputFormatClass(TextInputFormat.class);
        jobGenes.setOutputKeyClass(Text.class);
        jobGenes.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(jobGenes, new Path(args[1]));//args[0] for first input of articles, args[1] for metadata

        Path genesOutputPath = new Path("/outputs/genesList");
        deleteOutputPath(genesOutputPath);
        FileOutputFormat.setOutputPath(jobGenes, genesOutputPath);

        jobGenes.waitForCompletion(true);

        //FINAL MAPPER
        Configuration conf = getConf();
        conf.set("textinputformat.record.delimiter","\r\n\r\n");
        Job job = Job.getInstance(conf, "FinalMapper");
        try{
            Path genesTruePath = new Path("/outputs/genesList/part-r-00000");
            job.addCacheFile(genesTruePath.toUri());
        }catch(Exception e){
            System.out.println("File to cache not found");
            System.exit(1);
        }
        job.setJarByClass(InvertedIndexPhrases.class);
        job.setMapperClass(FinalMapper.class);
        job.setCombinerClass(FinalReducer.class);
        job.setReducerClass(FinalReducer.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));//args[0] for first input of articles, args[1] for metadata

        Path outputPath = new Path(args[2]);
        deleteOutputPath(outputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        return job.waitForCompletion(true) ? 0 : 1;

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new InvertedIndexPhrases(), args);
        System.exit(res);
    }

}
