package com.shadowfist.magicseteditor;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class CSVUtilsTest
{

    @Test
    public void testParsingLineWithQuotedTokenContainingComma()
    {
        List<String> result = CSVUtils.parseLine("a,bb,\"This sentence has a, comma.\",dddd");
        assertNotNull("result", result);
        assertEquals("tokens", 4, result.size());
        assertEquals("sentence", "This sentence has a, comma.", result.get(2));
        assertEquals("dddd", result.get(3));
    }

}
