package com.company2;

import org.jcsp.lang.*;

import java.util.LinkedList;

public class Buffer implements CSProcess {

    /***********
     *
     * 0 - request channel - customer
     * 1 - request channel - producer
     *
     * 0 - answers channel - not available to take part
     * 1 - answers channel - available to take part
     */


    private final LinkedList<One2OneChannelInt> requestConsumer = new LinkedList<>();
    private final LinkedList<One2OneChannelInt> requestProducer = new LinkedList<>();

    private One2OneChannelInt requestBufferChannelLeft = null;
    private One2OneChannelInt requestBufferChannelRight = null;
    private Guard[] allGuards;

    private int value = 0;
    private final int maxValue;
    private final int BUFFER_IDX;

    public Buffer(int maxValue, int BUFFER_IDX) {
        this.maxValue = maxValue;
        this.BUFFER_IDX = BUFFER_IDX;
    }

    public void run () {

        prepareSets();

        final Alternative allAlt = new Alternative(allGuards);

        while(true){

            checkLeftBuffer();
            int index = allAlt.select();
            if(index == requestProducer.size() + requestConsumer.size()) continue;

            if(index < requestProducer.size()){
                if(!(value + 1 <= maxValue)) { // cannot modify buffer

                    if (askRightBuffer(0) == 0)
                        producerActionDenied(index);
                    else
                        producerActionAccepted(index);
                }
                else
                    producerActionAccepted(index);
            }
            else if(index < requestProducer.size() + requestConsumer.size()){
                index -= requestProducer.size();
                if(!(value - 1 >= 0)) { // cannot modify buffer
                    if (askRightBuffer(1) == 0)
                        consumerActionDenied(index);
                    else
                        consumerActionAccepted(index);
                }
                else
                 consumerActionAccepted(index);
            }
        }
    }

    private int checkLeftBuffer(){

        final Alternative alt = new Alternative(new Guard[]{requestBufferChannelLeft.in(), new Skip()});
        int idx = alt.priSelect();
        if (idx == 1) return 0;

        int val = requestBufferChannelLeft.in().read();

        if(val == 0 && (value + 1 <= maxValue)){
            value += 1;
            System.out.println("Take from neighbour, bufor " + BUFFER_IDX + " : " + value);
            requestBufferChannelLeft.out().write(1);
            return 1;
        }

        if(val == 1 && (value - 1 >= 0)){
            value -= 1;
            System.out.println("Give to neighbour, bufor " + BUFFER_IDX + " : " + value);
            requestBufferChannelLeft.out().write(1);
            return 1;
        }

        requestBufferChannelLeft.out().write(0);
        return 0;
    }

    private int askRightBuffer(int reqVal){

        final Alternative alt = new Alternative(new Guard[]{requestBufferChannelLeft.in(), new Skip()});
        int idx = alt.priSelect();
        if (idx == 0){
            checkLeftBuffer();
            return 0;
        }

        requestBufferChannelRight.out().write(reqVal);
        int answer = requestBufferChannelRight.in().read();

        if(answer == 1){
            if(reqVal == 1){
                value += 1;
                System.out.println("Added, value from neighbour, bufor " + BUFFER_IDX + " : " + value);
            }

            else if (reqVal == 0){
                value -= 1;
                System.out.println("Substracted, value from neighbour, bufor " + BUFFER_IDX + " : " + value);
            }
            return 1;
        }

        return 0;
    }

    private void prepareSets(){

        allGuards = new Guard[requestProducer.size() + requestConsumer.size() + 1];

        for(int i=0; i<requestProducer.size(); i++)
            allGuards[i] = requestProducer.get(i).in();

        for(int i=0; i<requestConsumer.size(); i++)
            allGuards[i + requestProducer.size()] = requestConsumer.get(i).in();

        allGuards[requestProducer.size() + requestConsumer.size()] = new Skip();
    }

    private void producerActionAccepted(int idx) {
        requestProducer.get(idx).in().read();
        value += 1;
        System.out.println("Added, bufor " + BUFFER_IDX + " : " + value);
        requestProducer.get(idx).out().write(1);
    }

    private void producerActionDenied(int idx) {
        requestProducer.get(idx).in().read();
        System.out.println("bufor " + BUFFER_IDX + "producent odrzucony");
        requestProducer.get(idx).out().write(0);
    }

    private void consumerActionAccepted(int idx) {
        requestConsumer.get(idx).in().read();
        value -= 1;
        System.out.println("Substracted, bufor " + BUFFER_IDX + " : " + value);
        requestConsumer.get(idx).out().write(1);
    }

    private void consumerActionDenied(int idx) {
        requestConsumer.get(idx).in().read();
        System.out.println("bufor " + BUFFER_IDX + "konsument odrzucony");
        requestConsumer.get(idx).out().write(0);
    }


    void setLeft(One2OneChannelInt request){
        this.requestBufferChannelLeft = request;
    }

    void setRight(One2OneChannelInt request){
        this.requestBufferChannelRight = request;
    }

    void addProducer(One2OneChannelInt request){
        requestProducer.add(request);
    }

    void addConsumer(One2OneChannelInt request) {
        requestConsumer.add(request);
    }

}
