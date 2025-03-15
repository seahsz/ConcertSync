package vttp.server.utilities;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class StringSimilarity {

    public static double calculateSimilarity(String s1, String s2) {
        LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
        int distance = ld.apply(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return 1.0 - (double) distance / maxLength;
    }
    
}
