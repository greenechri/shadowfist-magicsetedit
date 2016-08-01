package com.shadowfist.magicseteditor;

import java.util.ArrayList;
import java.util.List;

public class CSVUtils
{
    public static final char DEFAULT_SEPARATOR = ',';
    public static final char DEFAULT_QUOTE = '"';

    public static List<String> parseLine(String cvsLine)
    {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators)
    {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote)
    {
        List<String> result = new ArrayList<>();

        // if empty, return!
        if (cvsLine == null || cvsLine.isEmpty())
        {
            return result;
        }

        if (customQuote == ' ')
        {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ')
        {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars)
        {

            if (inQuotes)
            {
                if (ch == customQuote)
                {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                }
                else
                {

                    // Fixed : allow "" in custom quote enclosed
                    if (ch == '\"')
                    {
                        if (!doubleQuotesInColumn)
                        {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    }
                    else
                    {
                        curVal.append(ch);
                    }

                }
            }
            else
            {
                if (ch == customQuote)
                {
                    inQuotes = true;
                }
                else if (ch == separators)
                {
                    // add result
                    result.add(curVal.toString());

                    // reset value
                    curVal = new StringBuffer();
                }
                else if (ch == '\r')
                {
                    // ignore LF characters
                    continue;
                }
                else if (ch == '\n')
                {
                    // the end, break!
                    break;
                }
                else
                {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}