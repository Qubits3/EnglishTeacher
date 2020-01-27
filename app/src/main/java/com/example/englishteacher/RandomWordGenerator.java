package com.example.englishteacher;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class RandomWordGenerator {

    static void generate(String[] firstArrayToShuffle, String[] secondArrayToShuffle, String[] firstArrayToUse, String[] secondArrayToUse){

        shuffleArray(firstArrayToShuffle, secondArrayToShuffle);
        System.out.println("Birinci");
        for (int i = 0; i < firstArrayToShuffle.length; i++)
        {
            System.out.print(firstArrayToShuffle[i] + " ");
        }

        System.arraycopy(firstArrayToShuffle, 0, firstArrayToUse, 0, firstArrayToShuffle.length);
        System.arraycopy(secondArrayToShuffle, 0, secondArrayToUse, 0, secondArrayToShuffle.length);

        System.out.println();

        System.out.println("İkinci");
        for (int i = 0; i < firstArrayToUse.length; i++)
        {
            System.out.print(firstArrayToUse[i] + " ");
        }
        System.out.println();
    }

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(String[] ar, String[] ar2)
    {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;

            // Simple swap 2
            String b = ar2[index];
            ar2[index] = ar2[i];
            ar2[i] = b;
        }
    }
}