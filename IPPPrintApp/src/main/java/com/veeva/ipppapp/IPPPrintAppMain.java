package com.veeva.ipppapp;

public class IPPPrintAppMain {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java -jar IPPPrintApp.jar -d | -s");
      System.out.println("  -d   Direct printing to printer");
      System.out.println("  -s   Printing through IPP print server");
      System.exit(1);
    }

    switch (args[0]) {
      case "-d":
        IPPPrintAppToPrinter.print();
        break;
      case "-s":
        IPPPrintAppToPrintServer.print();
        break;
      default:
        System.out.println("Unknown option: " + args[0]);
        System.out.println("Usage: java -jar IPPPrintApp.jar -d | -s");
        System.exit(1);
    }
  }
}

