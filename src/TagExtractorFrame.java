import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;

public class TagExtractorFrame extends JFrame {

    JPanel mainPnl;
    JPanel topPnl;
    JPanel middlePnl;

    JPanel bottomPnl;

    JTextArea displayTA;
    JScrollPane scroller;

    JLabel titleLbl;

    JLabel fileLbl;
    ImageIcon icon;

    JButton scan;
    JButton save;

    public Map<String, Integer> frequencies = new TreeMap<>();

    Random rnd = new Random();


    public TagExtractorFrame()
    {
        mainPnl = new JPanel();

        mainPnl.setLayout(new BorderLayout());

        createTopPnl();
        mainPnl.add(topPnl, BorderLayout.NORTH);

        createMiddlePnl();
        mainPnl.add(middlePnl, BorderLayout.CENTER);

        createBottomPnl();
        mainPnl.add(bottomPnl, BorderLayout.SOUTH);

        add(mainPnl);

    }

    private void createTopPnl()
    {
        topPnl = new JPanel();

        topPnl.setLayout(new GridLayout(2, 1));

        topPnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        titleLbl = new JLabel("Tag Extractor", JLabel.CENTER);

        fileLbl = new JLabel("Tag File: ", JLabel.CENTER);

        titleLbl.setFont(new Font("Roboto", Font.PLAIN, 36));

        fileLbl.setFont(new Font("Roboto", Font.PLAIN, 24));

        titleLbl.setVerticalTextPosition(JLabel.TOP);
        titleLbl.setHorizontalTextPosition(JLabel.CENTER);

        fileLbl.setVerticalTextPosition(JLabel.BOTTOM);
        fileLbl.setHorizontalTextPosition(JLabel.CENTER);

        topPnl.setBackground(new Color(198,226,255));

        topPnl.add(titleLbl);
        topPnl.add(fileLbl);
    }

    private void createMiddlePnl()
    {
        middlePnl = new JPanel();
        displayTA = new JTextArea(10, 40);

        displayTA.setFont(new Font("Verdana", Font.PLAIN, 20));

        displayTA.setEditable(false);
        scroller = new JScrollPane(displayTA);
        middlePnl.add(scroller);

        middlePnl.setBackground(new Color(198,226,255));

    }

    private void createBottomPnl()
    {

        bottomPnl = new JPanel();
        bottomPnl.setLayout(new GridLayout(1, 2));

        scan = new JButton("Scan File");
        scan.addActionListener((ActionEvent ae) ->
        {
            frequencies = scanFrequency();

            for (String key : frequencies.keySet())
            {
                displayTA.append(key + "\t" + frequencies.get(key) + "\n");
            }

        });

        save = new JButton("Save File");
        save.addActionListener((ActionEvent ae) ->
        {
            saveFile();
        });

        scan.setPreferredSize(new Dimension(40, 40));
        save.setPreferredSize(new Dimension(40, 40));

        scan.setFont(new Font("Sans Serif", Font.PLAIN, 15));
        save.setFont(new Font("Sans Serif", Font.PLAIN, 15));

        bottomPnl.add(scan);
        bottomPnl.add(save);

        bottomPnl.setBackground(new Color(198,226,255));

    }

    private Map<String, Integer> scanFrequency() {



        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";

        Map<String, Integer> frequencies = new TreeMap<>();

        try
        {

            File workingDirectory = new File(System.getProperty("user.dir"));

            chooser.setCurrentDirectory(workingDirectory);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();

                String name = (file.getFileName()).toString();

                InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                Set<String> stopWords = selectNoise();

                while (reader.ready())
                {
                    rec = reader.readLine();

                    String[] record = rec.split(" ");

                    for (String word: record) {

                        word = clean(word.toString());

                        Integer amount = frequencies.get(word);

                        if (!(stopWords.contains(word)) && word != " " && word != "") {
                            if (amount == null) { amount = 1; }
                            else { amount = amount + 1; }
                            frequencies.put(word, amount);
                        }
                    }

                }

                reader.close();


                fileLbl.setText("Tag File: " + name);

            }

            else {
                System.out.println("Failed to choose a file to process");
                System.out.println("Run the program again!");
                System.exit(0);
            }
        }

        catch (FileNotFoundException e)
        {
            System.out.println("File not found!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return frequencies;

    }

    private Set<String> selectNoise() {

        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";

        Set<String> stopWords = new TreeSet<>();

        try
        {

            File workingDirectory = new File(System.getProperty("user.dir"));

            chooser.setCurrentDirectory(workingDirectory);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();

                String name = (file.getFileName()).toString();

                InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));


                while (reader.ready())
                {
                    rec = reader.readLine();

                    String word = rec.toString();

                    stopWords.add(rec);

                }

                reader.close();

            }

            else {
                System.out.println("Failed to choose a file to process");
                System.out.println("Run the program again!");
                System.exit(0);
            }
        }

        catch (FileNotFoundException e)
        {
            System.out.println("File not found!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return stopWords;
    }

    private static String clean(String s)
    {
        String r = "";
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (Character.isLetter(c))
            {
                r = r + c;
            }
        }
        return r.toLowerCase();
    }

    private void saveFile() {

        String fileName = "tags.txt";

        File workingDirectory = new File(System.getProperty("user.dir"));

        Path file;

        file = Paths.get(workingDirectory.getPath()).resolve(fileName);

        try {
            File userFile = new File((file.getFileName().toString()));
            if (userFile.createNewFile()) {
                try {

                    Files.newBufferedWriter(file, TRUNCATE_EXISTING);
                    Files.newInputStream(file, TRUNCATE_EXISTING);

                    OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

                    for (String key: frequencies.keySet())
                    {

                        Integer value = frequencies.get(key);

                        String record = key + ", " + value;

                        writer.write(record, 0, record.length());

                        writer.newLine();
                    }
                    writer.close();

                    System.out.println("\nThe list has been saved to the file " + file.getFileName() + "!");
                }

                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}

