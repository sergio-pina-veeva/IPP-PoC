package com.veeva.ipppapp;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;
import java.awt.print.PrinterJob;
import java.io.File;

public class IPPPrintAppToPrinter {
  public static void main(String[] args) throws Exception {
    String filename = "test.pdf";
    String fileLocation = "C:\\dev\\projects\\IPP PoC\\IPPPrintApp\\src\\main\\resources\\";
//    FileInputStream fis = new FileInputStream(fileLocation);
    PDDocument document = PDDocument.load(new File(fileLocation + filename));
    DocFlavor flavor = new DocFlavor.INPUT_STREAM("application/octet-stream");

    // Define print attributes
    PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
    attrs.add(new JobName(filename, null));
    attrs.add(new Destination(new File(fileLocation + "printed_" + filename).toURI()));
    attrs.add(MediaSizeName.ISO_A4);                         // A4 paper
    attrs.add(OrientationRequested.LANDSCAPE);               // Landscape orientation
    attrs.add(Chromaticity.MONOCHROME);                      // Black & white
    attrs.add(Sides.DUPLEX);                                 // Duplex (double-sided)
//    attrs.add(new Copies(2));                                 // 2 copies
    attrs.add(PrintQuality.HIGH);                            // High quality*/

    // Name of the target printer
//    String targetPrinterName = "VirtualPrinter";
    String targetPrinterName = "Microsoft Print to PDF";

    // Search for the target printer
    PrintService[] allPrinters = PrintServiceLookup.lookupPrintServices(flavor, null);
    PrintService selectedPrinter = null;

    if (allPrinters.length == 0) {
      System.out.println("No suitable printers found.");
      return;
    }

    for (PrintService printer : allPrinters) {
      if (printer.getName().equalsIgnoreCase(targetPrinterName)) {
        selectedPrinter = printer;
        break;
      }
    }

    if (selectedPrinter == null) {
      System.err.println("Printer not found: " + targetPrinterName);
      return;
    }

    /*DocPrintJob job = selectedPrinter.createPrintJob();
    attrs.add(new JobName(filename, null));
    Doc doc = new SimpleDoc(fis, flavor, null);
    job.print(doc, attrs);*/


    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPageable(new PDFPageable(document));

    // Find Microsoft Print to PDF
    for (PrintService svc : PrinterJob.lookupPrintServices()) {
      if (svc.getName().equalsIgnoreCase(selectedPrinter.getName())) {
        job.setPrintService(svc);
        break;
      }
    }

    job.print(attrs);
    document.close();

    System.out.println("Print job sent to: " + selectedPrinter.getName());

//    fis.close();
  }
}