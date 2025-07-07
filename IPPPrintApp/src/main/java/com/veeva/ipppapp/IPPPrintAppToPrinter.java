package com.veeva.ipppapp;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobHoldUntil;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.JobPriority;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.net.URI;
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
      String mediaSize = prop.getProperty("mediaSize");
      String orientation = prop.getProperty("orientation");
      String chromaticity = prop.getProperty("chromaticity");
      String sides = prop.getProperty("sides");
      int copies = Integer.parseInt(prop.getProperty("copies", "1"));
      String printQuality = prop.getProperty("printQuality");
      String jobName = prop.getProperty("jobName", filename);
      boolean deleteSpooledFile = prop.getProperty("deleteSpoolingFile", "false").equals("true");

      PDDocument document = PDDocument.load(new File(fileLocation + filename));

      // Define print attributes
      PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
      attrs.add(new JobName(jobName, null));
      if ("Microsoft Print to PDF".equalsIgnoreCase(targetPrinterName)) {
      attrs.add(new Destination(new File(targetLocation + outputFile).toURI()));
      }
      // Media size
    if ("A4".equalsIgnoreCase(mediaSize)) {
        attrs.add(MediaSizeName.ISO_A4);
      } else if ("LETTER".equalsIgnoreCase(mediaSize)) {
        attrs.add(MediaSizeName.NA_LETTER);
      }
      // Orientation
      if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
        attrs.add(OrientationRequested.LANDSCAPE);
      } else  if (!"".equals(orientation)){
        attrs.add(OrientationRequested.PORTRAIT);
      }
      // Chromaticity
      if ("MONOCHROME".equalsIgnoreCase(chromaticity)) {
        attrs.add(Chromaticity.MONOCHROME);
      } else  if (!"".equals(chromaticity)){
        attrs.add(Chromaticity.COLOR);
      }
      // Sides
      if ("DUPLEX".equalsIgnoreCase(sides)) {
        attrs.add(Sides.DUPLEX);
      } else if (!"".equals(sides)) {
        attrs.add(Sides.ONE_SIDED);
      }
      // Copies
      attrs.add(new Copies(copies));
      // Print quality
      if ("HIGH".equalsIgnoreCase(printQuality)) {
        attrs.add(PrintQuality.HIGH);
      } else if ("DRAFT".equalsIgnoreCase(printQuality)) {
        attrs.add(PrintQuality.DRAFT);
      } else  if (!"".equals(printQuality)){
        attrs.add(PrintQuality.NORMAL);
      }

      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPageable(new PDFPageable(document));

      PrintService[] services = PrinterJob.lookupPrintServices();
      PrintService serviceUsed= null;
      boolean printerFound = false;
      for (PrintService svc : services) {
        if (svc.getName().equalsIgnoreCase(targetPrinterName)) {
          job.setPrintService(svc);
          serviceUsed = svc;
          printerFound = true;
          break;
        }
      }
      if (printerFound) {
        System.out.println("\nPrinter found: " + targetPrinterName);
        printPropertiesAvailability(serviceUsed);
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

      System.out.println("Print job sent to: " + targetPrinterName + "\n");

      if (deleteSpooledFile) {
        URI uri = new File(targetLocation + outputFile).toURI();
        if ("file".equalsIgnoreCase(uri.getScheme())) {
          File spooledFile = new File(uri);
          if (spooledFile.exists()) {
            if (spooledFile.delete()) {
              System.out.println("Spool file deleted: " + spooledFile.getAbsolutePath());
            } else {
              System.err.println("WARNING: Could not delete spool file: "
                   + spooledFile.getAbsolutePath());
            }
          }
        }
      }
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
      printPropertiesAvailability(svc);
    }
  }

  private static void printPropertiesAvailability(PrintService service) {
    System.out.println("--- Orientation supported: "
         + (service.isAttributeCategorySupported(OrientationRequested.class) ? "true" : "false"));
    System.out.println("--- Copies supported: "
         + (service.isAttributeCategorySupported(Copies.class) ? "true" : "false"));
    System.out.println("--- Media size supported: "
         + (service.isAttributeCategorySupported(Media.class) ? "true" : "false"));
    System.out.println("--- Print Quality supported: "
         + (service.isAttributeCategorySupported(PrintQuality.class) ? "true" : "false"));
    System.out.println("--- Duplex (Sides) supported: "
         + (service.isAttributeCategorySupported(Sides.class) ? "true" : "false"));
    System.out.println("--- Color supported: "
         + (service.isAttributeCategorySupported(ColorSupported.class) ? "true" : "false"));
    System.out.println("--- JobHoldUntil supported: "
         + (service.isAttributeCategorySupported(JobHoldUntil.class) ? "true" : "false"));
    System.out.println("--- JobPriority supported: "
         + (service.isAttributeCategorySupported(JobPriority.class) ? "true" : "false"));
  }
}