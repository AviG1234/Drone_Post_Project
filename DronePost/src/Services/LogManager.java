package Services;

import java.io.*;

public class LogManager {
    private static final String folderAddress = "data\\";
    private static final String fileName = "serverLog.txt";

    public LogManager() {}

    public static void printLog(String log) {
        try {
            File outputFile = createOutputFile(folderAddress + fileName);
            if (outputFile == null)
                throw new NullPointerException("Failed to create the output file.");
            FileWriter myWriter = new FileWriter(outputFile, true);
            PrintWriter out = new PrintWriter(new BufferedWriter(myWriter));
            out.println(log + "\n");
            out.close();
        } catch (IOException e){
            System.out.println("Error");
            System.out.println(e.getMessage());
        } catch (NullPointerException npe) { npe.printStackTrace(); }
    }

    /**
     * Checks if the requested output folder exists and creates it if absent
     */
    private static void createOutputFolder(){
        try {
            File folder = new File(folderAddress);
            if (!folder.isDirectory())
                if (folder.mkdir())
                    System.out.println("Folder created: " + folder.getName());
                else throw new IOException();
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the requested output file exists and creates it if absent
     * @param outputFile String containing the full name of the destination file
     * @return The requested output file
     */
    private static File createOutputFile(String outputFile){
        try {
            createOutputFolder();
            File file = new File(outputFile);
            if (!file.exists())
                if (file.createNewFile())
                    System.out.println("File created: " + file.getName());
                else throw new IOException();
            return file;
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }
}
