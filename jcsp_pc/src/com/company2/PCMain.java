package com.company2;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.Channel;
import org.jcsp.lang.One2OneChannelInt;
import org.jcsp.lang.Parallel;

import java.util.function.BiConsumer;

public final class PCMain {

    public static void main (String[] args) throws InterruptedException {

        // Create channel object
        int P = 16;
        int C = 16;
        int Bcount = 4;
        int BSize = 10;

        CSProcess[] procList = new CSProcess[P + C + Bcount];
        Buffer[] buffers = new Buffer[Bcount];

        int counter = 0;

        // create buffor
        for (int i = 0; i < Bcount; i++) {
            Buffer buffer = new Buffer(BSize, i);
            buffers[i] = buffer;
            procList[counter] = buffer;
            counter += 1;
        }

        // set a right neighbour
        for (int i = 0; i < Bcount; i++) {
            One2OneChannelInt requests = Channel.one2oneInt();
            buffers[i].setRight(requests);
            buffers[(i + 1) % Bcount].setLeft(requests);
        }

        for (int j = 0; j < P; j++) {
            Producer producer = new Producer();
            procList[counter] = producer;

            for (int i = 0; i < Bcount; i++) {
                One2OneChannelInt requests = Channel.one2oneInt();
                producer.addChannel(requests, i);
                buffers[i].addProducer(requests);
            }
            counter += 1;
        }

        for (int j = 0; j < C; j++) {
            Consumer consumer = new Consumer();
            procList[counter] = consumer;

            for (int i = 0; i < Bcount; i++) {
                One2OneChannelInt requests = Channel.one2oneInt();
                consumer.addChannel(requests, i);
                buffers[i].addConsumer(requests);
            }
            counter += 1;
        }

        //System.out.println("Statystyki: 16 klientów , 16 producentów i 4 cząstki bufora");
        //System.out.println("Statystyki: ilosc pozytywnie zkonczonych wejsc do bufora : wszystkie proby wejscia do bufora ");

        Parallel par = new Parallel(procList); // PAR construct
        par.run(); // Execute processes in parallel

    }

}