package org.mcgill.ca.it.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogFileGenerator {
    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private String reportFile;
    private String errorFile;
    private String successFile;
    private String debugFile;

    public LogFileGenerator(String reportFile, String errorFile, String successFile, String debugFile) {
        this.reportFile = reportFile;
        this.errorFile = errorFile;
        this.successFile = successFile;
        this.debugFile = debugFile;
    }

    public String getReportFile() {
        return reportFile;
    }

    public String getErrorFile() {
        return errorFile;
    }

    public String getSuccessFile() {
        return successFile;
    }

    public String getDebugFile() {
        return debugFile;
    }

    public void reportFile(int messageCount, int messageConsumer, int messageReject, int messageTransferred) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime now = LocalDateTime.now();

        try {

            File newFile = new File(this.getReportFile());

            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, true));

            List<String> messages = new ArrayList<>();
            messages.add("Batch process is running at : " + dtf.format(now));
            messages.add("No of messages in the Queue : " + messageCount);
            messages.add("No of messages consumed from the RabbitMQ : " + messageConsumer);
            messages.add("No of messages not consumed from the Queue : " + messageReject);
            messages.add("No of messages transferred to another Queue : " + messageTransferred);
            messages.add("************* End of the RabbitMQ queue messages report ****************\n");

            for (String sentence : messages) {
                writer.write(sentence);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void errorFile(String message, int responseCode, String responseBody, long duration) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime now = LocalDateTime.now();

        List<String> messages = new ArrayList<>();
        messages.add("************* Start of error message ****************");
        messages.add("Message in RabbitMQ is : ");
        messages.add(message);
        messages.add("Execution in " + duration + " milliseconds");
        messages.add("HTTP code : " + responseCode);
        messages.add("Response Body : ");
        messages.add(responseBody);
        messages.add("Batch process is running at : " + dtf.format(now));
        messages.add("************* End of error message ****************\n");

        generateFile(this.getErrorFile(), messages);
    }

    public void successFile(String message, int responseCode, String responseBody, long duration) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime now = LocalDateTime.now();

        List<String> messages = new ArrayList<>();
        messages.add("************* Start of success message ****************");
        messages.add("Message in RabbitMQ is : ");
        messages.add(message);
        messages.add("Execution in " + duration + " milliseconds");
        messages.add("HTTP code : " + responseCode);
        messages.add("Response Body : ");
        messages.add(responseBody);
        messages.add("Batch process is running at : " + dtf.format(now));
        messages.add("************* End of success message ****************\n");

        generateFile(this.getSuccessFile(), messages);
    }

    public void debugFile(String message) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime now = LocalDateTime.now();

        try {

            File newFile = new File(this.getDebugFile());

            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, true));

            List<String> messages = new ArrayList<>();
            messages.add("Batch process is running at : " + dtf.format(now));
            messages.add("************* Start of debug report ****************");
            messages.add("Error occurred in client a newCall : " + message);
            messages.add("************* End of debug report ****************\n");

            for (String sentence : messages) {
                writer.write(sentence);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void generateFile(String fileName, List<String> messages) {
        try {
            File newFile = new File(fileName);

            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, true));

            for (String sentence : messages) {
                writer.write(sentence);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
