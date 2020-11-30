import java.io.IOException;
import java.util.StringTokenizer;
import java.io.*;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedWordIndex {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{

    private Text word = new Text();
    private Text docID = new Text();
    private Text content = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

      String line = value.toString();
      String allContent[]  = line.split("\\t",2);
      docID.set(allContent[0]);
      content.set((allContent[1].replaceAll("[^a-zA-Z]"," ")).toLowerCase());
      StringTokenizer itr = new StringTokenizer(content.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken());
        context.write(word, docID);
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    public void reduce(Text key, Iterable<Text> values,Context context) throws IOException, InterruptedException {
HashMap<String, Integer> map = new HashMap<String,Integer>();
      for (Text val : values) {
        if(map.containsKey(val.toString())) { 
          // found in map
          map.put(val.toString(),map.get(val.toString()) + 1);
        } else { 
          // not there in the map
          map.put(val.toString(),1);
        }
      }
      context.write(key,new Text(map.toString()));
    }
  }


  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "word count");
    job.setJarByClass(InvertedWordIndex.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
