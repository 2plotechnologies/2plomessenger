package com.twoploapps.a2plomessenger;

import java.util.Random;

public class GeneradorCodigoGrupo {
    private final String LETTERS="abcdefghijklmnopkrstuvwxyz";
    private final String NUMBERS="0123456789";
    private final char[]ALPHANUMERIC=(LETTERS+LETTERS.toUpperCase()+NUMBERS).toCharArray();

    public String generateAlphaNumeric(int length){
        StringBuilder result = new StringBuilder();
        for(int i=0; i<length ;i++){
            result.append(ALPHANUMERIC[new Random().nextInt(ALPHANUMERIC.length)]);
        }
        return result.toString();
    }
}
