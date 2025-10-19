package com.increff.pos.flow;

import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class InvoiceFlow {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;

    public InvoiceData generateInvoice(InvoiceRequest invoiceRequest) throws ApiException {

        Orders order = invoiceRequest.getOrder();
        List<OrderItem> items = invoiceRequest.getOrderItems();

        byte[] pdfBytes;
        double grandTotal = 0.0;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("INVOICE - POS SYSTEM");
                contentStream.endText();
                yPosition -= 40;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Order ID: " + order.getId() + " | Date: " + order.getCreatedAt().toString());
                contentStream.endText();
                yPosition -= 40;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setLeading(LINE_HEIGHT);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(String.format("%-10s %-10s %10s %10s", "Prod ID", "Quantity", "Price", "Subtotal"));
                contentStream.endText();
                yPosition -= LINE_HEIGHT;

                contentStream.setLineWidth(1f);
                contentStream.moveTo(MARGIN, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
                contentStream.stroke();
                yPosition -= 5;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                for (OrderItem item : items) {
                    double subtotal = item.getQuantity() * item.getSellingPrice();
                    grandTotal += subtotal;

                    yPosition -= LINE_HEIGHT;

                    contentStream.setTextMatrix(Matrix.getTranslateInstance(MARGIN, yPosition));

                    String itemLine = String.format("%-10s %-10s %10.2f %10.2f",
                            item.getProductId(),
                            item.getQuantity(),
                            item.getSellingPrice(),
                            subtotal);

                    contentStream.showText(itemLine);

                }
                contentStream.endText();


                yPosition -= 20;

                contentStream.setLineWidth(1f);
                contentStream.moveTo(page.getMediaBox().getWidth() - 150, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
                contentStream.stroke();
                yPosition -= 5;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 150, yPosition);
                contentStream.showText(String.format("GRAND TOTAL: %.2f", grandTotal));
                contentStream.endText();
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            pdfBytes = byteArrayOutputStream.toByteArray();

            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            InvoiceData invoiceData = new InvoiceData();
            invoiceData.setOrderId(order.getId());
            invoiceData.setBase64Pdf(base64Pdf);

            return invoiceData;
        }
        catch (IOException e) {
            throw new ApiException("Failed to generate PDF invoice: " + e.getMessage());
        }
    }
}