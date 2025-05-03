package com.skible.be.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class WordService {
    private final List<String> wordPool = new ArrayList<>();

     public  WordService(){
         Collections.addAll(wordPool,"apple", "banana", "cherry", "dog", "elephant",
                 "flower", "guitar", "house", "island", "jacket",
                 "kangaroo", "lamp", "mountain", "notebook", "ocean",
                 "piano", "quilt", "rocket", "sunflower", "tiger",
                 "umbrella", "violin", "whale", "xylophone", "yacht", "zebra");
     }

    public List<String> pickN(int n) {
        int size = wordPool.size();
        if (n <= 0 || size == 0) {
            return Collections.emptyList();
        }
        if (n >= size) {
            List<String> all = new ArrayList<>(wordPool);
            Collections.shuffle(all);
            return all;
        }
        Set<Integer> chosenIdx = new HashSet<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        while (chosenIdx.size() < n) {
            chosenIdx.add(rnd.nextInt(size));
        }

        List<String> result = new ArrayList<>(n);
        for (int idx : chosenIdx) {
            result.add(wordPool.get(idx));
        }
        return result;
    }


}
