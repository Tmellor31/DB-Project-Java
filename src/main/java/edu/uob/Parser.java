package edu.uob;

import java.util.ArrayList;

public class Parser {
    int count = 0;


    public boolean readCommand(Tokeniser query) {
        System.out.println("first command token is " + query.tokens.get(count));

        //Check that the last token is ;
        int lastTokenNum = query.tokens.size();
        String lastToken = query.tokens.get(lastTokenNum - 1);
        if (lastToken.equals(";")) {
            System.out.println("true");
            return true;
        }
        //query.tokens.get(count) should equal the 'current command' - at the start it is equal to zero so the first input
        if (query.tokens.get(count).equalsIgnoreCase(("ALTER"))) {
            //alter(query);
        }
        else if (query.tokens.get(count).equalsIgnoreCase(("USE"))) {
            //use(query);
        }
        else if (query.tokens.get(count).equalsIgnoreCase(("INSERT"))){
            //insert(query);
        }
        return false;
    }

    private void use(Tokeniser query){
        System.out.println(count);
    }
}









