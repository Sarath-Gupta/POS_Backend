package com.increff.pos.flow;

import com.increff.pos.service.OrderApi;
import com.increff.pos.service.OrderItemApi;
import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.Orders;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.model.data.InvoiceData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
@Transactional(readOnly = true) // This operation is read-only, so we mark it as such for performance.
public class InvoiceFlow {

    @Autowired
    private OrderApi orderApi;
    @Autowired
    private OrderItemApi orderItemApi;

    // Constants for PDF layout
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;

    public InvoiceData generateInvoice(Integer orderId) throws ApiException {
        Orders order = orderApi.getById(orderId);
        List<OrderItem> items = orderItemApi.getAllByOrderId(orderId);
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

                // --- 2. Item Table Header ---
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setLeading(LINE_HEIGHT);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(String.format("%-10s %-10s %10s %10s", "Prod ID", "Quantity", "Price", "Subtotal"));
                contentStream.endText();
                yPosition -= LINE_HEIGHT;

                // Draw a separator line
                contentStream.setLineWidth(1f);
                contentStream.moveTo(MARGIN, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - MARGIN, yPosition);
                contentStream.stroke();
                yPosition -= 5;

                // --- 3. Order Item Loop (Dynamic Content) ---
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.beginText();

                for (OrderItem item : items) {
                    double subtotal = item.getQuantity() * item.getSellingPrice();
                    grandTotal += subtotal;

                    // Move to the next line position
                    yPosition -= LINE_HEIGHT;
                    contentStream.newLineAtOffset(MARGIN, yPosition);

                    // Note: Since we don't have the Product Name, we use the Product ID.
                    // If you fetch the product data, you would use product.getName() here.
                    String itemLine = String.format("%-10s %-10s %10.2f %10.2f",
                            item.getProductId(),
                            item.getQuantity(),
                            item.getSellingPrice(),
                            subtotal);

                    contentStream.showText(itemLine);
                    contentStream.newLineAtOffset(-MARGIN, 0); // Reset for the next line
                }
                contentStream.endText();

                yPosition -= 20; // Move down before the total

                // --- 4. Grand Total ---
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

            // Save the generated document into a byte array in memory.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            pdfBytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            // If PDF generation fails for any reason, throw a clean ApiException.
            throw new ApiException("Failed to generate PDF invoice: " + e.getMessage());
        }

        // --- Step 3: Base64 Encoding and DTO Creation ---
        String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setBase64Pdf(base64Pdf);

        return invoiceData;
    }
}
