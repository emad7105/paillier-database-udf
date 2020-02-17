package be.heydari.cassandra;

import java.math.BigInteger;

public class CassandraDocument {

    public String invoiceId; // unique
    public String invoiceName; // emad, bert, ...
    public String invoiceStatus; // paid,unpaid
    public BigInteger invoiceAmount;
    public BigInteger nSquare;

    public CassandraDocument(String invoiceId, String invoiceName, String invoiceStatus, BigInteger invoiceAmount) {
        this.invoiceId = invoiceId;
        this.invoiceName = invoiceName;
        this.invoiceStatus = invoiceStatus;
        this.invoiceAmount = invoiceAmount;
    }

    public CassandraDocument() {

    }


    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceName() {
        return invoiceName;
    }

    public void setInvoiceName(String invoiceName) {
        this.invoiceName = invoiceName;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public BigInteger getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigInteger invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public BigInteger getnSquare() {
        return nSquare;
    }

    public void setnSquare(BigInteger nSquare) {
        this.nSquare = nSquare;
    }
}
