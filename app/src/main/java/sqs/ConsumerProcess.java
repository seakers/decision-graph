package sqs;

import app.Runs;

public class ConsumerProcess {
    public static void main(String[] args) {
        if (args.length > 0) {
            Runs.createRunGroup();
            Runs.initRun();

            int argument = Integer.parseInt(args[0]);  // get the argument we passed
            try{
                Consumer consumer = new Consumer.Builder(argument).build();
                consumer.run();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
