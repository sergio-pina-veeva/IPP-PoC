package com.veeva.ipppapp;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Properties;

public class IPPPrintAppToPrinter {
  public static void print(Properties prop) {
    try {

      // Read properties
      String filename = prop.getProperty("filename", "test.pdf");
      String fileLocation = prop.getProperty("fileLocation", "./");
      String targetPrinterName = prop.getProperty("targetPrinterName", "Microsoft Print to PDF");
      String targetLocation = prop.getProperty("targetLocation", "./");
      String outputFile = prop.getProperty("outputFile", "printed_" + filename);
      String mediaSize = prop.getProperty("mediaSize", "A4");
      String orientation = prop.getProperty("orientation", "LANDSCAPE");
      String chromaticity = prop.getProperty("chromaticity", "MONOCHROME");
      String sides = prop.getProperty("sides", "ONE_SIDED");
      int copies = Integer.parseInt(prop.getProperty("copies", "1"));
      String printQuality = prop.getProperty("printQuality", "HIGH");
      String jobName = prop.getProperty("jobName", filename);

//    FileInputStream fis = new FileInputStream(fileLocation);
      PDDocument document = PDDocument.load(new File(fileLocation + filename));

      // Define print attributes
      PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
      attrs.add(new JobName(jobName, null));
      attrs.add(new Destination(new File(targetLocation + outputFile).toURI()));
      // Media size
      if ("A4".equalsIgnoreCase(mediaSize)) {
        attrs.add(MediaSizeName.ISO_A4);
      } else if ("LETTER".equalsIgnoreCase(mediaSize)) {
        attrs.add(MediaSizeName.NA_LETTER);
      }
      // Orientation
      if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
        attrs.add(OrientationRequested.LANDSCAPE);
      } else {
        attrs.add(OrientationRequested.PORTRAIT);
      }
      // Chromaticity
      if ("MONOCHROME".equalsIgnoreCase(chromaticity)) {
        attrs.add(Chromaticity.MONOCHROME);
      } else {
        attrs.add(Chromaticity.COLOR);
      }
      // Sides
      if ("DUPLEX".equalsIgnoreCase(sides)) {
        attrs.add(Sides.DUPLEX);
      } else {
        attrs.add(Sides.ONE_SIDED);
      }
      // Copies
      attrs.add(new Copies(copies));
      // Print quality
      if ("HIGH".equalsIgnoreCase(printQuality)) {
        attrs.add(PrintQuality.HIGH);
      } else if ("DRAFT".equalsIgnoreCase(printQuality)) {
        attrs.add(PrintQuality.DRAFT);
      } else {
        attrs.add(PrintQuality.NORMAL);
      }


      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPageable(new PDFPageable(document));

      PrintService[] services = PrinterJob.lookupPrintServices();
      boolean printerFound = false;
      for (PrintService svc : services) {
        if (svc.getName().equalsIgnoreCase(targetPrinterName)) {
          job.setPrintService(svc);
          printerFound = true;
          break;
        }
      }
      if (printerFound) {
        System.out.println("\nPrinter found: " + targetPrinterName);
      } else {
        System.out.println("\nPrinter not found: " + targetPrinterName);
      }

      System.out.println("\nPrint Attributes:");
      if (attrs == null || attrs.isEmpty()) {
        System.out.println("No attributes set.");
      } else {
        for (Attribute attr : attrs.toArray()) {
          System.out.println(attr.getName() + ": " + attr.toString());
        }
      }

      job.print(attrs);
      document.close();

      System.out.println("Print job sent to: " + targetPrinterName);

//    fis.close();
    } catch (Exception e) {
      if (e instanceof PrinterException) {

        System.out.println("ERROR: User is not allowed to use this printer. Error message: " + e.getMessage());
      } else {
        System.out.println("ERROR: The printer didn't receive the job. Error message: " + e.getMessage());
      }
    }
  }

  public static void printerFinder() {
    PrintService[] services = PrinterJob.lookupPrintServices();

    System.out.println("Available printers:");
    int i = 0;
    for (PrintService svc : services) {
      i++;
      System.out.println(i + ". " + svc.getName());
    }
  }
}