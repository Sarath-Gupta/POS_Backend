package com.increff.pos.flow;

import com.increff.pos.commons.ApiException;
import com.increff.pos.entity.OrderItem;
import com.increff.pos.model.data.InvoiceData;
import com.increff.pos.model.data.InvoiceRequest;
import com.increff.pos.model.data.OrderData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.util.Base64;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class InvoiceFlow {

    private static final float MARGIN = 50;

    public InvoiceData generateInvoice(InvoiceRequest invoiceRequest) throws ApiException {

        OrderData order = invoiceRequest.getOrderData();
        List<OrderItem> items = invoiceRequest.getOrderItems();

        byte[] pdfBytes;
        double grandTotal = 0.0;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            final float pageWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
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
                String createdAtStr = (order.getCreatedAt() != null) ? order.getCreatedAt().toString() : "N/A";
                contentStream.showText("Order ID: " + order.getId() + " | Date: " + createdAtStr);
                contentStream.endText();
                yPosition -= 40;

                Table.TableBuilder tableBuilder = Table.builder()
                        .addColumnsOfWidth(pageWidth * 0.25f, pageWidth * 0.25f, pageWidth * 0.25f, pageWidth * 0.25f)
                        .font(PDType1Font.HELVETICA)
                        .fontSize(10)
                        .padding(5);

                tableBuilder.addRow(Row.builder()
                        .add(TextCell.builder().text("Product ID").font(PDType1Font.HELVETICA_BOLD).backgroundColor(Color.LIGHT_GRAY).borderWidth(1).build())
                        .add(TextCell.builder().text("Quantity").font(PDType1Font.HELVETICA_BOLD).backgroundColor(Color.LIGHT_GRAY).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                        .add(TextCell.builder().text("Unit Price").font(PDType1Font.HELVETICA_BOLD).backgroundColor(Color.LIGHT_GRAY).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                        .add(TextCell.builder().text("Subtotal").font(PDType1Font.HELVETICA_BOLD).backgroundColor(Color.LIGHT_GRAY).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                        .build());

                for (OrderItem item : items) {
                    double subtotal = item.getQuantity() * item.getSellingPrice();
                    grandTotal += subtotal;

                    tableBuilder.addRow(Row.builder()
                            .add(TextCell.builder().text(String.valueOf(item.getProductId())).borderWidth(1).build())
                            .add(TextCell.builder().text(String.valueOf(item.getQuantity())).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                            .add(TextCell.builder().text(String.format("%.2f", item.getSellingPrice())).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                            .add(TextCell.builder().text(String.format("%.2f", subtotal)).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(1).build())
                            .build());
                }

                Table table = tableBuilder.build();

                TableDrawer tableDrawer = TableDrawer.builder()
                        .contentStream(contentStream)
                        .table(table)
                        .startX(MARGIN)
                        .startY(yPosition)
                        .build();

                tableDrawer.draw();

                yPosition -= table.getHeight();
                yPosition -= 20;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                float grandTotalX = page.getMediaBox().getWidth() - MARGIN - 150;
                contentStream.newLineAtOffset(grandTotalX, yPosition);
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

