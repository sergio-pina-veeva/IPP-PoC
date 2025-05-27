package com.veeva.ipppapp;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class IPPPrintAppToPrintServer {
  public static void main(String[] args) throws Exception {
    String ippUrl = "http://localhost:9163/printers/VirtualPrinter";
    String filename = "test.pdf";
    String filepath = "C:\\dev\\projects\\IPP PoC\\IPPPrintApp\\src\\main\\resources\\" + filename;
    byte[] fileBytes= new FileInputStream(filepath).readAllBytes();
    byte[] ippRequest = buildIppPrintJobRequest(filename, fileBytes, ippUrl);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(ippUrl))
         .header("Content-Type", "application/ipp")
         .POST(HttpRequest.BodyPublishers.ofByteArray(ippRequest))
         .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("IPP Server Response code: " + response.statusCode());
    System.out.println("IPP Server Response body (raw): " + response.body());
  }

  private static byte[] buildIppPrintJobRequest(String jobName, byte[] documentData, String ippUrl) throws Exception {
        /*
         Minimal IPP request structure:

         [version]          2 bytes: 0x01 0x01 (IPP 1.1)
         [operation-id]      2 bytes: 0x00 0x02 (Print-Job)
         [request-id]        4 bytes: 0x00 0x00 0x00 0x01 (request number)

         // Operation Attributes Tag
         [tag]               1 byte: 0x01 (operation-attributes-tag)

         // attributes: charset, language, printer-uri, job-name, document-format

         [name-length]       2 bytes (big endian)
         [name]              variable length
         [value-length]      2 bytes (big endian)
         [value]             variable length

         // end of attributes: 1 byte 0x03

         [document-data]     raw document bytes
        */

    // We'll use a helper method to build the attribute byte arrays.

    // Buffer size estimation:
    int bufferSize = 1024 + documentData.length;
    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    // IPP Header
    buffer.put((byte)0x01); // version major
    buffer.put((byte)0x01); // version minor
    buffer.putShort((short)0x0002); // operation-id Print-Job
    buffer.putInt(1); // request-id

    // Operation Attributes Tag
    buffer.put((byte)0x01);

    // attributes
    putAttr(buffer, (byte)0x47, "attributes-charset", "utf-8");
    putAttr(buffer, (byte)0x48, "attributes-natural-language", "en");
    putAttr(buffer, (byte)0x45, "printer-uri", ippUrl); // uri type
    putAttr(buffer, (byte)0x42, "job-name", jobName);
    putAttr(buffer, (byte)0x49, "document-format", "text/plain");
    putAttr(buffer, (byte) 0x21, "copies", 2); // integer
    putAttr(buffer, (byte) 0x44, "media", "iso_a4_210x297mm"); // keyword
    putAttr(buffer, (byte) 0x23, "orientation-requested", 3); // enum
    putAttr(buffer, (byte) 0x44, "sides", "two-sided-long-edge"); // keyword

    // end of attributes tag
    buffer.put((byte)0x03);

    // Document data
    buffer.put(documentData);

    // Return array with exact size
    byte[] result = new byte[buffer.position()];
    buffer.flip();
    buffer.get(result);
    return result;
  }

  // Helper method to put an attribute:
  // tag = value-tag (e.g. 0x47 = charset, 0x42 = nameWithoutLanguage, 0x45 = uri, 0x48 = naturalLanguage, 0x49 = mimeMediaType)
  private static void putAttr(ByteBuffer buffer, byte tag, String name, String value) {
    buffer.put(tag);

    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) nameBytes.length);
    buffer.put(nameBytes);

    byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) valBytes.length);
    buffer.put(valBytes);
  }

  private static void putAttr(ByteBuffer buffer, byte tag, String name, int intValue) {
    buffer.put(tag);
    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
    buffer.putShort((short) nameBytes.length);
    buffer.put(nameBytes);

    buffer.putShort((short) 4); // int = 4 bytes
    buffer.putInt(intValue);
  }


}