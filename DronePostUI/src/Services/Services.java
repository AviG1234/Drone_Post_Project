package Services;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Services {
    private final String folderAddress = "data\\";
    private final String fileName = "currentUser.txt";
    public Services() {}

    public ArrayList<String> getClientDetailsFromFile() {
        File outputFile = createOutputFile(folderAddress + fileName);
        ArrayList<String> clientData = new ArrayList<>();
        try (Scanner myReader = new Scanner(outputFile)){
            while (myReader.hasNextLine())
                clientData.add(myReader.nextLine().trim());
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return clientData;
    }

    public void saveClientDetailsToFile(ArrayList<String> dataSource) {
        try {
            File outputFile = createOutputFile(folderAddress + fileName);
            FileWriter myWriter = new FileWriter(outputFile, false);
            PrintWriter out = new PrintWriter(new BufferedWriter(myWriter));
            dataSource.forEach(out::println);
            out.close();
        } catch (IOException e){
            System.out.println("Error");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Checks if the requested output folder exists and creates it if absent
     */
    private void createOutputFolder(){
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
    private File createOutputFile(String outputFile){
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
