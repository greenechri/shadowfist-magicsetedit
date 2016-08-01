/* @Copyright 2016 Christian Greene
 * Shadowfist is a registered trademark of Inner Kingdom Games.
 */
package com.shadowfist.magicseteditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This program downloads a CSV file from a URL (typically set to a Google
 * Docs location but could just be a file:// location) and converts it to a
 * Magic Set Editor file format.
 * <p>
 * The file format of the CSV file is:
 * Title,Subtitle,Faction,CardType,Cost,Provides,Fighting,Power,Body,Text,Artist,Designer
 * <P>
 * The file format of the MSE set file is a zip file containing a set with
 * contents like
 * <pre>
 * mse version: 0.3.8
 * game: shadowfist
 * stylesheet: fullblank
 * set info:
 *    symbol:
 * card:
 *    has styling: false
 *    notes:
 *    time created: 2016-07-28 15:08:54
 *    time modified: 2016-07-28 15:14:50
 *    title: All the Power
 *    scene:
 *    fighting: D
 *    image:
 *    subtitle: Edge
 *    rules: If you have 50 Power in your pool, win the game.
 *    tag: <i>The only way Daniel can win</i>
 *    copyright: playtest round 1
 *    artist: footer2
 * card:
 *    has styling: false
 *    notes:
 *    time created: 2016-07-28 15:14:59
 *    time modified: 2016-07-28 15:17:00
 *    attributes: character, guiding hand
 *    title: A Character
 *    scene:
 *    fighting: 8
 *    image:
 *    subtitle: Subtitle of Character
 *    rules: Guts. Some awesome ability.
 *    tag:
 *    copyright: playtest round 1
 *    artist: footer2
 * version control:
 *    type: none
 * apprentice code: 
 * </pre>
 *
 * @author cgreene
 */
public class Main
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static final String DEFAULT_PROPERTIES = "default.properties";
    public static final String DEFAULT_FILENAME = "shadowfist-cardset.mse-set";
    public static final String KEY_DIRECTORY = "directory";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_URL = "url";

    /**
     * Will be set to value of "user.dir" in System properties. 
     */
    private static Path defaultOutputDirectory;
    /**
     * Will be set to value of "filename" in {@link #DEFAULT_PROPERTIES}
     * or set to {@link #DEFAULT_FILENAME}. 
     */
    private static String defaultFileName;
    /**
     * Will be set to value of "url" in {@link #DEFAULT_PROPERTIES}.
     */
    private static URL defaultURL;


    /**
     * Set by input or defaults as the file path to write to.
     */
    private static Path outputPath;
    /**
     * Set by input or defaults as the url to download from.
     */
    private static URL downloadUrl;

    /**
     * Load the default settings.
     */
    static
    {
        try
        {
            // load input
            determineDefaultValues();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(getHelpString());
        }
    }

    /**
     * Main method called to execute converting CVS card list to card set.
     *
     * @param args the starting program arguments
     */
    public static void main(String[] args)
    {
        try
        {
            parseArguments(args);
            // download csv and transform into mse-set
            CharSequence cardContents = downloadAndTransform();
            
            // build set file
            StringBuilder set = new StringBuilder("mse version: 0.3.8\n");
            set.append("game: shadowfist\n");
            set.append("stylesheet: fullblank\n");
            set.append("set info:\n");
            set.append("   symbol:\n");
            set.append(cardContents);
            set.append("version control:\n");
            set.append("   type: none\n");
            set.append("apprentice code:\n");
            System.out.println(set);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(getHelpString());
        }
    }

    /**
     * Using the input values, download the HTTP contents as a string,
     * parsing the content, transforming it, and return the formatted body of
     * the set file.
     * 
     * @throws IOException if something bad happens
     */
    protected static CharSequence downloadAndTransform() throws IOException
    {
        InputStream is = null;
        StringBuilder formattedContents = new StringBuilder();
        try
        {
            String line = null;
            is = downloadUrl.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null)
            {
                formattedContents.append(transformCard(line));
            }
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        return formattedContents;
    }

    /**
     * Transform to
     * <pre>
     * card:
     *    has styling: false
     *    notes:
     *    time created: 2016-07-28 15:08:54
     *    time modified: 2016-07-28 15:14:50
     *    title: All the Power
     *    scene:
     *    fighting: D
     *    image:
     *    subtitle: Edge
     *    rules: If you have 50 Power in your pool, win the game.
     *    tag: <i>The only way Daniel can win</i>
     *    copyright: playtest round 1
     *    artist: footer2
     * </pre>
     * @param cardDetails
     * @return
     */
    private static CharSequence transformCard(String cardDetails)
    {
        List<String> values = CSVUtils.parseLine(cardDetails);
        String now = DATE_FORMAT.format(new Date());
        StringBuilder transformedCard = new StringBuilder("card:\n");
        transformedCard.append("   has styling: false\n");
        transformedCard.append("   notes:\n");
        transformedCard.append("   time created: ").append(now).append("\n");
        transformedCard.append("   time modified: ").append(now).append("\n");
        transformedCard.append("   title: ").append(values.get(0)).append("\n");
        transformedCard.append("   scene:\n");
        transformedCard.append("   fighting: ").append(values.get(6)).append("\n");
        transformedCard.append("   image:\n");
        transformedCard.append("   subtitle: ").append(values.get(1)).append("\n");
        transformedCard.append("   rules: ").append(values.get(9)).append("\n");
        transformedCard.append("   tag:\n");
        transformedCard.append("   copyright: ").append(values.get(10)).append("\n");
        transformedCard.append("   artist: ").append(values.get(11)).append("\n");
        return transformedCard;
    }

    /**
     * Sets the value of {@link #downloadUrl} and {@link #outputPath}.
     * 
     * @param args arguments passed into the application
     * @throws MalformedURLException if a URL was specified but it was invalid.
     */
    protected static void parseArguments(String[] args) throws MalformedURLException
    {
        downloadUrl = defaultURL;
        outputPath = defaultOutputDirectory.resolve(defaultFileName);

        if (args != null)
        {
            String specifiedFile = null;
            Path specifiedPath = null;
            for (String arg : args)
            {
                if (arg.startsWith("-u"))
                {
                    String url = stripFlag(arg);
                    downloadUrl = new URL(url);
                }
                else if (arg.startsWith("-d"))
                {
                    String dir = stripFlag(arg);
                    specifiedPath = Paths.get(dir);
                }
                else if (arg.startsWith("-f"))
                {
                    specifiedFile = stripFlag(arg);
                }
                else
                {
                    throw new IllegalArgumentException("An invalid argument was specified on the command line.");
                }
            }
            if (specifiedFile != null)
            {
                outputPath = (specifiedPath != null)? specifiedPath.resolve(specifiedFile) :
                    defaultOutputDirectory.resolve(specifiedFile);
            }
            else if (specifiedPath != null)
            {
                outputPath = specifiedPath.resolve(defaultFileName);
            }
            System.out.println("Set output file to " + outputPath);
        }
        else
        {
            System.out.println("No arguements set, using default values.");
        }
    }

    /**
     * Loads properties from default.properties in the root of the classpath
     * and also locates the "user.dir" value. The file looks like
     * <pre>
     * url=https://docs.google.com/feeds/download/spreadsheets/Export?key=1fM5tyIo1KZI8eE8VQdLVkVRG30J_224Nk1LKb3PYuZ0&exportFormat=csv
     * directory=C:/Users/cgreene/Documents/Shadowfist
     * filename=my-shadowfist-cardset.mse-set
     * </pre>
     * @throws IOException if properties load fails
     */
    protected static void determineDefaultValues() throws IOException
    {
        // determine output dir
        defaultOutputDirectory = Paths.get(System.getProperty("user.dir"));
        if (defaultOutputDirectory == null || !defaultOutputDirectory.toFile().exists())
        {
            throw new IllegalStateException("The current working directory couldn't be found.");
        }

        // load properties
        Properties properties = new Properties();
        InputStream stream = Main.class.getResourceAsStream("/" + DEFAULT_PROPERTIES);
        try
        {
            properties.load(stream);
            System.out.println("Loaded " + properties);
        }
        catch (Exception e)
        {
            throw new IOException("Could not load default.properties from the jar or classpath.", e);
        }
        // determine default filename
        defaultFileName = properties.getProperty(KEY_FILENAME);
        if (defaultFileName == null)
        {
            defaultFileName = DEFAULT_FILENAME;
        }
        
        // determine default URL
        String defUrlString = properties.getProperty(KEY_URL);
        if (defUrlString != null)
        {
            System.out.println("Setting defaultURL " + defaultURL);
            defaultURL = new URL(defUrlString);
        }
    }

    /**
     * Return a description of how to use this program.
     *
     * @return a description of how to use this program.
     */
    public static String getHelpString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append("Usage: java com.shadowfist.magicseteditor.Main -u=<csv_file_url> -d=<output_directory> -f=<output_filename>\n");
        buff.append("\n");
        buff.append("where args are:\n");
        buff.append("    -u:  The URL of the comma-separated-value file to download that contains card data.\n");
        buff.append("    -d:  The directory to write the Magic Set Editor file to. Defaults to the current working directory.\n");
        buff.append("    -f:  The file name of the out file. Defaults to \"" + DEFAULT_FILENAME + "\".\n");
        buff.append("\n");
        return buff.toString();
    }

    /**
     * Remove the "-u" and "=" from the front of the arg.
     * 
     * @param arg
     * @return
     */
    private static String stripFlag(String arg)
    {
        if (arg.startsWith("-u") || arg.startsWith("-d") || arg.startsWith("-f"))
        {
            arg = arg.substring(2);
        }
        if (arg.startsWith("="))
        {
            arg = arg.substring(1);
        }
        return arg;
    }

}
