package com.veeva.ipppapp.util;

import com.sun.jna.platform.win32.WinBase.SYSTEMTIME;

public class JnaPrintJobInfo {
  private int jobId;
  private String printerName;
  private String machineName;
  private String userName;
  private String documentName;
  private String dataType;
  private String status;
  private int statusCode;
  private int priority;
  private int position;
  private int totalPages;
  private int pagesPrinted;
  private SYSTEMTIME submitted;

  // Constructor
  public JnaPrintJobInfo() {}

  // Getters and setters
  public int getJobId() { return jobId; }
  public void setJobId(int jobId) { this.jobId = jobId; }

  public String getPrinterName() { return printerName; }
  public void setPrinterName(String printerName) { this.printerName = printerName; }

  public String getMachineName() { return machineName; }
  public void setMachineName(String machineName) { this.machineName = machineName; }

  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }

  public String getDocumentName() { return documentName; }
  public void setDocumentName(String documentName) { this.documentName = documentName; }

  public String getDataType() { return dataType; }
  public void setDataType(String dataType) { this.dataType = dataType; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public int getStatusCode() { return statusCode; }
  public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

  public int getPriority() { return priority; }
  public void setPriority(int priority) { this.priority = priority; }

  public int getPosition() { return position; }
  public void setPosition(int position) { this.position = position; }

  public int getTotalPages() { return totalPages; }
  public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

  public int getPagesPrinted() { return pagesPrinted; }
  public void setPagesPrinted(int pagesPrinted) { this.pagesPrinted = pagesPrinted; }

  public SYSTEMTIME getSubmitted() { return submitted; }
  public void setSubmitted(SYSTEMTIME submitted) { this.submitted = submitted; }

  /**
   * Converts job status code to human-readable string
   */
  public String getStatusDescription() {
    switch (statusCode) {
      case 0x00000001: return "Paused";
      case 0x00000002: return "Error";
      case 0x00000004: return "Deleting";
      case 0x00000008: return "Spooling";
      case 0x00000010: return "Printing";
      case 0x00000020: return "Offline";
      case 0x00000040: return "Paper Out";
      case 0x00000080: return "Printed";
      case 0x00000100: return "Deleted";
      case 0x00000200: return "Blocked";
      case 0x00000400: return "User Intervention Required";
      case 0x00000800: return "Restarting";
      case 0x00001000: return "Complete";
      case 0x00004000: return "Retained";
      default: return "Unknown (" + statusCode + ")";
    }
  }

  @Override
  public String toString() {
    return String.format("PrintJob[ID=%d, Printer=%s, User=%s, Document=%s, Status=%s, Pages=%d/%d]",
         jobId, printerName, userName, documentName, getStatusDescription(), pagesPrinted, totalPages);
  }
}

