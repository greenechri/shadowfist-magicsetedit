/* @Copyright 2016 Christian Greene
 * Shadowfist is a registered trademark of Inner Kingdom Games.
 */
package com.shadowfist.magicseteditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    protected static final int COL_TITLE = 0;
    protected static final int COL_SUBTITLE = 1;
    protected static final int COL_FACTION = 2;
    protected static final int COL_TYPE = 3;
    protected static final int COL_COST = 4;
    protected static final int COL_PROVIDES = 5;
    protected static final int COL_FIGHTING = 6;
    protected static final int COL_POWER = 7;
    protected static final int COL_BODY = 8;
    protected static final int COL_RULES = 9;
    protected static final int COL_ARTIST = 10;
    protected static final int COL_DESIGNER = 11;


    /**
     * Will be set to value of "mse" in {@link #DEFAULT_PROPERTIES}.
     */
    private static Path defaultMsePath;
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
     * Set by command-line input or defaults as the file path to write to.
     */
    private static Path outputPath;
    /**
     * Set by command-line input or defaults as the url to download from.
     */
    private static URL inputUrl;
    /**
     * The copyright statement from the properties file.
     */
    private static String copyright;

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
//            String cardContents = transformInput();

            // build set file
//            String setData = buildSetFile(cardContents);

            // create zip file for mse-set
//            writeMseFile(setData);

            // load mse cli interface
            System.out.println("Starting cli...");
            ProcessBuilder builder = new ProcessBuilder(defaultMsePath.toString(), "--cli", "--quiet", outputPath.toString());
            builder.redirectErrorStream(true);
            Process mse = builder.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mse.getOutputStream()));
            // start reading process output thread
            BufferedReader reader = new BufferedReader(new InputStreamReader(mse.getInputStream()));
//        	Thread printMseOutputThread = new Thread(() -> {
//                String line;
//                try
//                {
//					while ((line = reader.readLine()) != null)
//					{
//					    System.out.println("*" + line);
//					}
//				} 
//                catch (Exception e)
//                {
//					e.printStackTrace();
//				}
//        	}, "printMseOutputThread");
//        	printMseOutputThread.setDaemon(true);
//        	printMseOutputThread.start();

            // get information
            System.out.println("Getting set length...");
            writer.write("length(set.cards)");
            writer.newLine();
            writer.flush();
            String length = reader.readLine();
            System.out.println(length);

            // name of a card
            int index = 12;
            System.out.println("Getting card name...");
            writer.write("set.cards[" + index + "].title");
            writer.newLine();
            writer.flush();
            String name = reader.readLine();
            System.out.println(name);

            // render a card
            System.out.println("Writing image file...");
            writer.write("write_image_file(file:\"" + index + "-" + name + ".jpg\", set.cards[" + index + "])\n");
            writer.newLine();
            writer.flush();
            System.out.println(reader.readLine());

            // export all images

            // TODO collate into pdf file

            mse.destroy();
            System.out.println("done.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(getHelpString());
        }
    }

    /**
     * Create the mse-set (zip) file at the location {@link #outputPath}.
     * Overwrites the file if needed. Creates any directories if needed.
     *
     * @param set the set contents
     * @throws IOException if file can't be created or zipping fails.
     */
    protected static void writeMseFile(String setData) throws IOException
    {
        if (outputPath == null)
        {
            throw new IllegalStateException("The output path is not valid or was not determined correctly.");
        }
        // check if file exists, delete if needed
        File outputFile = outputPath.toFile();
        if (outputFile.exists())
        {
            System.out.println("Deleting existing mse-st file...");
            outputFile.delete();
        }
        // create any parent directories if needed
        if (!outputFile.getParentFile().exists())
        {
            System.out.println("Creating directory " + outputFile.getParentFile() + "...");
            outputFile.getParentFile().mkdirs();
        }

        // create zip 
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));

        // create set entry
        out.putNextEntry(new ZipEntry("set"));

        // write entry
        out.write(setData.getBytes("UTF-8"), 0, setData.getBytes().length);
        out.closeEntry();
        out.close();
    }

    /**
     * Wrap the card contents in set meta-data.
     *
     * @param cardContents the list of cards in the set
     * @return the completed set file including meta-data
     */
    protected static String buildSetFile(String cardContents)
    {
        StringBuilder set = new StringBuilder(cardContents.length());
        set.append("mse version: 0.3.8\n");
        set.append("game: shadowfist\n");
        set.append("stylesheet: fullblank\n");
        set.append("set info:\n");
        set.append("\tsymbol:\n");
        set.append(cardContents);
        set.append("version control:\n");
        set.append("\ttype: none\n");
        set.append("apprentice code:\n");
        return set.toString();
    }

    /**
     * Using the input values, download the HTTP contents as a string,
     * parsing the content, transforming it, and return the formatted body of
     * the set file. Each line is transformed by {@link #transformCard(String)}.
     * 
     * @throws IOException if something bad happens
     */
    protected static String transformInput() throws IOException
    {
        InputStream is = null;
        StringBuilder formattedContents = new StringBuilder();
        try
        {
            String line = null;
            System.out.println("Opening connection to url: " + inputUrl);
            is = inputUrl.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            reader.readLine();// skip header line
            while ((line = reader.readLine()) != null)
            {
                System.out.print(".");
                formattedContents.append(transformCard(line));
            }
            System.out.println("");
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
        return formattedContents.toString();
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
    protected static CharSequence transformCard(String cardDetails)
    {
        List<String> values = CSVUtils.parseLine(cardDetails);
        String now = DATE_FORMAT.format(new Date());
        StringBuilder transformedCard = new StringBuilder("card:\n");
        transformedCard.append("\thas styling: false\n");
        transformedCard.append("\tnotes:\n");
        transformedCard.append("\ttime created: ").append(now).append("\n");
        transformedCard.append("\ttime modified: ").append(now).append("\n");
        transformedCard.append("\tattributes: ").append(toAttributes(values.get(COL_TYPE), values.get(COL_FACTION))).append("\n");
        transformedCard.append("\ttitle: ").append(values.get(COL_TITLE)).append("\n");
        transformedCard.append("\tscene:\n");
        if (values.get(COL_FIGHTING).length() > 0)
        {
            transformedCard.append("\tfighting: ").append(values.get(COL_FIGHTING)).append("\n");
        }
        if (values.get(COL_POWER).length() > 0)
        {
            transformedCard.append("\tpower: ").append(values.get(COL_POWER)).append("\n");
        }
        if (values.get(COL_BODY).length() > 0)
        {
            transformedCard.append("\tbody: ").append(values.get(COL_BODY)).append("\n");
        }
        transformedCard.append("\timage:\n");
        transformedCard.append("\tsubtitle: ").append(values.get(COL_SUBTITLE)).append("\n");
        transformedCard.append("\trules: ").append(values.get(COL_RULES)).append("\n");
        transformedCard.append("\ttag:\n");
        if (values.get(COL_COST).length() > 0)
        {
            transformedCard.append("\tcost: ").append(toResources(values.get(COL_COST))).append("\n");
        }
        transformedCard.append("\tcopyright: ").append(copyright).append("\n");
        if (values.get(COL_ARTIST).length() > 0)
        {
            transformedCard.append("\tartist: ").append(values.get(COL_ARTIST)).append("\n");
        }
        if (values.get(COL_PROVIDES).length() > 0)
        {
            transformedCard.append("\tresources: ").append(toResources(values.get(COL_PROVIDES))).append("\n");
        }
        return transformedCard;
    }

    /**
     * Sets the value of {@link #inputUrl} and {@link #outputPath}.
     * 
     * @param args arguments passed into the application
     * @throws MalformedURLException if a URL was specified but it was invalid.
     */
    protected static void parseArguments(String[] args) throws MalformedURLException
    {
        inputUrl = defaultURL;
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
                    inputUrl = new URL(url);
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
                    throw new IllegalArgumentException("An invalid argument \"" + arg + "\" was specified on the command line.");
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
        // try load from working dir
        try
        {
            properties.load(new FileReader(DEFAULT_PROPERTIES));
        }
        catch (Exception e)
        {
            System.out.println("Could not load default.properties working directory. Trying classpath...");

            // try from root classpath
            InputStream stream = Main.class.getResourceAsStream("/" + DEFAULT_PROPERTIES);
            try
            {
                properties.load(stream);
                System.out.println("Loaded " + properties);
            }
            catch (Exception e1)
            {
                throw new IOException("Could not load default.properties from the jar or classpath.", e1);
            }
        }

        // determine mse location
        defaultMsePath = Paths.get(properties.getProperty("mse"));

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
            defaultURL = new URL(defUrlString);
            System.out.println("Setting defaultURL " + defaultURL);
        }

        // determine the copyright
        copyright = properties.getProperty("copyright");
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
     * Return a comma-separated list of attributes based upon the specified input.
     *
     * @param type
     * @param faction
     * @return
     */
    private static Object toAttributes(String type, String faction)
    {
        StringBuilder attr = new StringBuilder();
        if (type.equals("Feng Shui Site"))
        {
            type = "fss";
        }
        attr.append(type.toLowerCase());

        if (faction.equals("Lotus"))
        {
            faction = "eaters of the lotus";
        }
        else if (faction.equals("Monarchs"))
        {
            faction = "four monarchs";
        }
        else if (faction.equals("Hand"))
        {
            faction = "guiding hand";
        }
        attr.append(", ").append(faction.toLowerCase());
        return attr.toString();
    }

    /**
     * Upper-cases and converts "a" for Ascended to "W" since a is for Architects
     * in MSE.
     *
     * @param resources the inpur resources.
     * @return
     */
    private static Object toResources(String resources)
    {
        StringBuilder builder = new StringBuilder(resources.toUpperCase());
        for (int i = 0; i < resources.length(); i++)
        {
            if (builder.charAt(i) == 'A')
            {
                builder.replace(i, i+1, "W");
            }
        }
        return builder.toString();
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
