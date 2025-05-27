package com.veeva.ipppapp;

import javax.print.attribute.Attribute;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class IPPPrintAppToPrintServer {
  public static void print() throws Exception {
    // Load properties
    Properties prop = new Properties();
    try (FileInputStream input = new FileInputStream("application.properties")) {
      if (input == null) {
        throw new FileNotFoundException("application.properties not found in classpath");
      }
      prop.load(input);
    }

    // Read properties
    String ippUrl = prop.getProperty("ippUrl", "http://localhost:9163/printers/VirtualPrinter");
    String filename = prop.getProperty("filename", "test.pdf");
    String fileLocation = prop.getProperty("fileLocation", "./");
    String filepath = fileLocation + filename;

    String jobName = prop.getProperty("jobName", filename);
    String documentFormat = prop.getProperty("documentFormat", "application/pdf");
    int copies = Integer.parseInt(prop.getProperty("copies", "1"));
    String media = prop.getProperty("media", "iso_a4_210x297mm");
    int orientationRequested = Integer.parseInt(prop.getProperty("orientationRequested", "3"));
    String sides = prop.getProperty("sides", "two-sided-long-edge");
    String charset = prop.getProperty("charset", "utf-8");
    String naturalLanguage = prop.getProperty("naturalLanguage", "en");byte[] fileBytes= new FileInputStream(filepath).readAllBytes();
    byte[] ippRequest = buildIppPrintJobRequest(
         jobName, fileBytes, ippUrl, documentFormat, copies, media, orientationRequested, sides, charset, naturalLanguage
    );

    System.out.println("Print Attributes:");
    for (String key : prop.stringPropertyNames()) {
      System.out.println(key + " = " + prop.getProperty(key));
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(ippUrl))
         .header("Content-Type", "application/ipp")
         .POST(HttpRequest.BodyPublishers.ofByteArray(ippRequest))
         .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    System.out.println("Print job sent to: " + ippUrl);
    System.out.println("IPP Server Response code: " + response.statusCode());
    System.out.println("IPP Server Response body: " + response.body());}

  private static byte[] buildIppPrintJobRequest(
       String jobName,
       byte[] documentData,
       String ippUrl,
       String documentFormat,
       int copies,
       String media,
       int orientationRequested,
       String sides,
       String charset,
       String naturalLanguage
  ) throws Exception {
    int bufferSize = 1024 + documentData.length;
    ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

    // IPP Header
    buffer.put((byte)0x01); // version major
    buffer.put((byte)0x01); // version minor
    buffer.putShort((short)0x0002); // operation-id Print-Job
    buffer.putInt(1); // request-id

    // Operation Attributes Tag
    buffer.put((byte)0x01);

    putAttr(buffer, (byte)0x47, "attributes-charset", charset);
    putAttr(buffer, (byte)0x48, "attributes-natural-language", naturalLanguage);
    putAttr(buffer, (byte)0x45, "printer-uri", ippUrl); // uri type
    putAttr(buffer, (byte)0x42, "job-name", jobName);
    putAttr(buffer, (byte)0x49, "document-format", documentFormat);
    putAttr(buffer, (byte)0x21, "copies", copies); // integer
    putAttr(buffer, (byte)0x44, "media", media); // keyword
    putAttr(buffer, (byte)0x23, "orientation-requested", orientationRequested); // enum
    putAttr(buffer, (byte)0x44, "sides", sides); // keyword

    // End of attributes tag
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