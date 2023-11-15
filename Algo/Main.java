package Algo;

import java.io.IOException;

/**
 * @Copyright (C), HITSZ
 * @Author Maohua Lyu, Wensheng, gan
 * @since 2023/4/24
 **/
public class Main {
    public static void main(String[] args) throws IOException {
        String[] datasets = new String[]{"ASL_BU_1", "ASL_BU_2", "CONTEXT", "HEPATITIS", "10k200E32L", "20k200E32L"};
        double[] uss = new double[]{0.0023, 0.0024, 0.0025, 0.0026, 0.0027, 0.0028, 0.0029,
                0.0017, 0.00175, 0.0018, 0.00185, 0.0019, 0.00195, 0.002,
                0.15, 0.155, 0.16, 0.165, 0.17, 0.175, 0.18,
                0.004, 0.0045, 0.005, 0.0055, 0.006, 0.0065, 0.007,
                0.000832, 0.000833, 0.000834, 0.000835, 0.000836, 0.000837, 0.000838,
                0.000414, 0.000415, 0.000416, 0.000417, 0.000418, 0.000419, 0.00042};

//        double[] uss = new double[]{0.000414, 0.000415, 0.000416, 0.000417, 0.000418, 0.000419, 0.00042};
//        double[] uus = new double[]{0.000832, 0.000833, 0.000834, 0.000835, 0.000836, 0.000837, 0.000838};
//        int idx = Integer.parseInt(args[0]);

        int idx = 6;

        String dataset = datasets[4];
        double us = uss[idx];

        double confidence = 0.6;
//        String datasetFile = "intervalData/" + dataset + ".txt";
        String input = "intervalData/" + dataset + ".txt";
        String output = dataset + "conf" + confidence + "util" + us + ".txt";


        System.out.println("processing: " + dataset + " threshold: " + us);

        // This parameter let the user specify how many sequences from the input file should be used.
        // For example, it could be used to read only the first 1000 sequences of an input file
        int maximumSequenceCount = Integer.MAX_VALUE;

        //  THESE ARE ADDITIONAL PARAMETERS
        //   THE FIRST PARAMETER IS A CONSTRAINT ON THE MAXIMUM NUMBER OF ITEMS IN THE LEFT SIDE OF RULES
        // For example, we don't want to find rules with more than 9 items in their left side
        int maxAntecedentSize = 9;  // 9

        //   THE SECOND PARAMETER IS A CONSTRAINT ON THE MAXIMUM NUMBER OF ITEMS IN THE RIGHT SIDE OF RULES
        // For example, we don't want to find rules with more than 9 items in their right side
        int maxConsequentSize = 9;  // 9


        Algo algo = new Algo();
//        Algo_nmc algo = new Algo_nmc();
        algo.runAlgo(input, output, us, confidence, maxAntecedentSize, maxConsequentSize, maximumSequenceCount);
    }
}
