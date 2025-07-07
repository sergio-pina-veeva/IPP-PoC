package com.veeva.ipppapp;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

public class IPPPrintAppMain {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java -jar IPPPrintApp.jar -d | -s");
      System.out.println("  -d   Direct printing to printer.");
      System.out.println("  -s   Printing through IPP print server.");
      System.out.println("  -f   Finding of available printers.");
      System.out.println("  -j   Checking the statuses of the jobs in the target printer.");
      System.exit(1);
    }

    // Load properties
    Properties prop = new Properties();
    FileReader reader;
    try {
      reader = new FileReader("src/main/resources/application.properties");
      prop.load(reader);
    } catch (FileNotFoundException e) {
      reader = new FileReader("application.properties");
      prop.load(reader);
    }

    switch (args[0]) {
      case "-d":
        IPPPrintAppToPrinter.print(prop);
        break;
      case "-s":
        IPPPrintAppToPrintServer.print(prop);
        break;
      case "-j":
        JnaJobChecker.checkJobs(prop);
        break;
      case "-f":
        IPPPrintAppToPrinter.printerFinder();
        break;
      default:
        System.out.println("Unknown option: " + args[0]);
        System.out.println("Usage: java -jar IPPPrintApp.jar -d | -s");
        System.exit(1);
    }
  }
}

