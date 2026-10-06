package dev.miniredis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommandResultTest {

    @Test
    void statusResult_withStringValue_returnsStringValue(){
        StatusResult tempResult= new StatusResult("OK");
        assertEquals("OK",tempResult.getValue());
    }

    @Test
    void stringResult_withStringValue_returnsStringValue(){
        StringResult tempResult= new StringResult("Bro");
        assertEquals("Bro",tempResult.getValue());
    }
}
