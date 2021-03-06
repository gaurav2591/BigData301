import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Aadhar {
	public static class AaadharMapper extends Mapper<LongWritable,Text,Text,IntWritable>{
		private Text finalKey = new Text();
		private IntWritable value = new IntWritable();
		public void map(LongWritable ofst,Text val,Context ctx) throws IOException, InterruptedException{
			String str = val.toString();
			String[] rec = str.split(",");
			if(!rec[1].equalsIgnoreCase("Enrolment Agency")){
				if(Integer.parseInt(rec[9]) > 0){
					finalKey.set(rec[1]);
					value.set(Integer.parseInt(rec[9]));
				   ctx.write(finalKey,value);	
				}
			}
		}
		
	}
	
    public static class AaadharReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
		
    	Map<String,Integer> map = new HashMap();
    	
		public void reduce(Text key, Iterable<IntWritable> values,Context ctx) throws IOException, InterruptedException{
			int total = 0;
			for(IntWritable value : values){
				total = total + value.get();
			}
			map.put(key.toString(),total);
		}
		
		@Override
		protected void cleanup(Context ctx)
				throws IOException, InterruptedException {
			ctx.write(new Text("Total Num of Agencies rejected"),new IntWritable(map.size()));
			for(Map.Entry<String,Integer> entrySet : map.entrySet()){
				ctx.write(new Text(entrySet.getKey()),new IntWritable(entrySet.getValue()));
			}
		}
	}	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		 
		  Configuration  conf = new Configuration();
		  Job job = Job.getInstance(conf,"Total Num of Agencies rejected");
		  job.setJarByClass(Aadhar.class);
		  job.setMapperClass(AaadharMapper.class);
		  job.setReducerClass(AaadharReducer.class);
		  job.setMapOutputKeyClass(Text.class);
		  job.setMapOutputValueClass(IntWritable.class);
		  job.setOutputKeyClass(Text.class);
		  job.setOutputValueClass(IntWritable.class);
		  FileInputFormat.addInputPath(job,new Path(args[0]));
		  FileOutputFormat.setOutputPath(job,new Path(args[1]));
		  System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
	

}
