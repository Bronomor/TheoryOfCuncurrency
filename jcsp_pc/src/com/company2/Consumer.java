package com.company2;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannelInt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Consumer implements CSProcess {

    private final HashMap<Integer, One2OneChannelInt> buffersRequest = new HashMap<>();

    private int visits = 0;
    private int successesVisits = 0;

    public Consumer () { }

    public void run (){
        while(true) {
            Random rand = new Random();
            int index = rand.nextInt(buffersRequest.size());
            buffersRequest.get(index).out().write(0);
            visits += 1;
            int val = buffersRequest.get(index).in().read();
            if(val == 1) successesVisits += 1;

            try {
                Thread.sleep((long) (Math.random() * 10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //if(visits == 1000)
            //    getStatistic();
        }

    } // run

    public void addChannel(One2OneChannelInt request, int bufferIDX){
        buffersRequest.put(bufferIDX, request);
    }

    public void getStatistic(){
        System.out.println("Konsument: " + successesVisits + " : " + visits);
    }

} // class Consumer

