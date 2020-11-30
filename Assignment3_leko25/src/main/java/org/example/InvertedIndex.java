package org.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class InvertedIndex {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        if (args.length != 2) {
            System.err.println("Usage: Inverted Index <input path> <output path>");
            System.exit(-1);
        }
        Configuration config = new Configuration();
        Job job = Job.getInstance(config, "Inverted Index");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(WordCountMap.class);
        job.setReducerClass(WordCountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class WordCountMap extends Mapper<Object, Text, Text, Text> {
        private Text word = new Text();

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
//            super.map(key, value, context);

            // Convert all words to lower case
            String line = value.toString().toLowerCase();
            String[] lines = line.split("\\t", 2);
            Text documentId = new Text(lines[0]);

            // Replace all non-letter words empty quotes
            String text = lines[1].replaceAll("[^a-zA-Z]+", " ");
            StringTokenizer tokenizer = new StringTokenizer(text);

            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, documentId);
            }
        }
    }

    public static class WordCountReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
//            super.reduce(key, values, context);
            HashMap<String, Integer> map = new HashMap<>();
            for (Text v : values) {
                String value = v.toString();
                if (!map.containsKey(value)) {
                    map.put(value, 1);
                } else {
                    int count = map.get(value);
                    map.put(value, count + 1);
                }
            }

            StringBuilder s = new StringBuilder();
            for (String k: map.keySet()) {
                s.append(k).append(":").append(map.get(k)).append(" ");
            }
            context.write(key, new Text(s.toString()));
        }
    }
}
