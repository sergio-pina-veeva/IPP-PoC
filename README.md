# IPPPrintApp

This repository contains two Java command-line applications for printing PDF documents using the Internet Printing Protocol (IPP):

- **IPPPrintAppToPrinter.java** – Prints directly to a local or network printer.
- **IPPPrintAppToPrintServer.java** – Sends print jobs to an IPP print server using raw IPP requests.

---

## Table of Contents

- [Overview](#overview)
- [Requirements](#requirements)
- [Usage](#usage)
  - [Direct Printing (IPPPrintAppToPrinter)](#direct-printing-ippprintapptoprinter)
  - [Print Server Printing (IPPPrintAppToPrintServer)](#print-server-printing-ippprintapptoprintserver)
- [Configuration](#configuration)
- [Print Attributes](#print-attributes)
- [PaperCut Print Server Setup](#papercut_print_server_setup)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Overview

This project demonstrates two approaches for IPP-based printing in Java:

- **Direct to Printer:** Uses Java Print Service API and PDFBox to render and send PDF files to a specified printer (e.g., "Microsoft Print to PDF"), with support for print attributes like paper size, orientation, duplex, and output file destination.
- **Via Print Server:** Constructs and sends a raw IPP Print-Job request (including document data and attributes) to a print server endpoint using Java's `HttpClient`.

---

## Requirements

- Java 11 or newer
- [Apache PDFBox](https://pdfbox.apache.org/) library (for direct PDF printing)
- Access to a printer, virtual printer, or an IPP print server
- For "Microsoft Print to PDF": Windows 10/11 with the feature enabled

---

## Usage

### Configuratiuon with application.properties

All settings for both applications are managed in the provided application.properties file.

### Direct Printing (`IPPPrintAppToPrinter.java`)

**Purpose:**  
Print a PDF file directly to a local or network printer with configurable print attributes.

**How it works:**

1. Loads a PDF document using PDFBox.
2. Sets print attributes (job name, destination file, paper size, orientation, monochrome, duplex, print quality).
3. Finds the target printer by name.
4. Uses Java's `PrinterJob` to send the rendered PDF to the printer.

**Sample Command:**
java -cp ".;pdfbox-app-2.x.x.jar" com.veeva.ipppapp.IPPPrintAppToPrinter


**Key Configuration in Code:**
String filename = "test.pdf";
String fileLocation = "C:\dev\projects\IPP PoC\IPPPrintApp\src\main\resources\";
String targetPrinterName = "Microsoft Print to PDF";


**Print Attributes Example:**
- Output file: `printed_test.pdf` in the resources folder
- Paper size: A4
- Orientation: Landscape
- Color: Monochrome
- Duplex: Double-sided
- Print quality: High

*To change attributes, edit the `attrs.add(...)` lines in the source code.*

---

### Print Server Printing (`IPPPrintAppToPrintServer.java`)

**Purpose:**  
Send a PDF file as a raw IPP Print-Job request to an IPP print server.

**How it works:**

1. Reads the PDF file as a byte array.
2. Constructs a minimal IPP 1.1 Print-Job request, including operation attributes (charset, language, printer URI, job name, document format, copies, media, orientation, sides).
3. Sends the request to the print server using Java's `HttpClient`.
4. Prints the server's HTTP response code and raw body.

**Sample Command:**
String ippUrl = "http://localhost:9163/printers/VirtualPrinter";
String filename = "test.pdf";
String filepath = "C:\dev\projects\IPP PoC\IPPPrintApp\src\main\resources\" + filename;


**IPP Attributes Example:**
- Job name: `test.pdf`
- Copies: 2
- Media: `iso_a4_210x297mm`
- Orientation: Landscape
- Sides: Two-sided long edge

*To change attributes, edit the `putAttr(...)` calls in the `buildIppPrintJobRequest` method.*

---

## Configuration

- **Printer Name:**  
  Set the `targetPrinterName` in `IPPPrintAppToPrinter.java` to match your installed printer.
- **Print Server URL:**  
  Set the `ippUrl` in `IPPPrintAppToPrintServer.java` to your print server's IPP endpoint.
- **File Path:**  
  Place your PDF in the resources folder and update the `filename` as needed.

---

## Print Attributes

| Attribute      | Direct to Printer (Java API)         | Via Print Server (IPP)         |
| -------------- | ------------------------------------ | ----------------------------- |
| Output file    | `Destination` attribute              | Not applicable (server-side)   |
| Paper size     | `MediaSizeName.ISO_A4`               | `media: iso_a4_210x297mm`     |
| Orientation    | `OrientationRequested.LANDSCAPE`     | `orientation-requested: 3`    |
| Color          | `Chromaticity.MONOCHROME`            | Not set (add if needed)       |
| Duplex         | `Sides.DUPLEX`                       | `sides: two-sided-long-edge`  |
| Copies         | `Copies` (commented in example)      | `copies: 2`                   |
| Print quality  | `PrintQuality.HIGH`                  | Not set (add if needed)       |

---

## Troubleshooting

- **No suitable printers found:**  
  Ensure the printer is installed and the name matches exactly (case-insensitive).
- **Empty or corrupt PDFs:**  
  Make sure you are rendering PDFs with PDFBox and not sending raw PDF bytes to "Microsoft Print to PDF".
- **IPP server errors:**  
  Check the IPP endpoint, document format, and that your server supports the attributes you set.
- **Permissions:**  
  Run your application with sufficient permissions to access printers and files.

---

## PaperCut Print Server Setup

### Installing PaperCut NG/MF

System Requirements:
Ensure your server meets PaperCut NG/MF system requirements.

Download and Install:
Download PaperCut NG/MF from the official PaperCut website.
Run the installer and follow the configuration wizard.
Complete the initial setup and test the client software.

Add Printers:
Set up all printers on the system using the manufacturer’s print drivers.
Test print on each printer to confirm functionality.

Firewall:
Ensure ports 9163 (HTTP) and 9164 (HTTPS) are open for PaperCut Mobility Print.

Verify Setup:
Log in to the PaperCut web admin interface.
Confirm printers are listed and jobs are tracked.

For detailed steps, see the PaperCut NG/MF Installation Guide on the PaperCut website.

### Configuring a Virtual Print Queue ("Find-Me" Printing)

A virtual or "Find-Me" print queue allows users to print to a single queue and release jobs at any physical printer.

Create a Virtual Print Queue:
On the print server, create a new print queue (e.g., find-me-queue) using the same driver as your physical printers.
Point the queue to a nul port (Windows) or leave it unassigned (some drivers require a real IP).

Register with PaperCut:
Ensure the virtual queue appears in the PaperCut admin interface under the Printers tab.

Configure as Virtual Queue:
In the PaperCut admin interface, select the virtual queue.
Set Queue type to "This is a virtual queue (jobs will be forwarded to a different queue)".
In Job Redirection Settings, select destination print queues.
Enable Hold/Release Queue if desired.

Secure Print Release (optional):
Configure devices for secure print release via the Devices tab.
Link the virtual queue to devices for user job release.

For more, see Find-Me Printing Overview and Virtual Queue Setup in the PaperCut documentation.

## License

This project is provided as a Proof of Concept (PoC) and is not licensed for production use.  
For more information, see the LICENSE file or contact the author.

---

**Author:**  
Sergio Pina - EU Integration Services team

---

*For questions or contributions, please open an issue or contact the maintainer.*
