package com.increff.pos.service;

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
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class InvoiceApi {

    private static final float MARGIN = 50;

    private static final Color COLOR_PRIMARY = new Color(34, 66, 124);
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_BLACK = Color.BLACK;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public InvoiceData generateInvoice(InvoiceRequest invoiceRequest) throws ApiException {

        OrderData order = invoiceRequest.getOrderData();
        List<OrderItem> items = invoiceRequest.getOrderItemData();
        List<String> productNames = invoiceRequest.getProductNames();


        byte[] pdfBytes;
        double grandTotal = 0.0;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            final float pageWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
            float yPosition = page.getMediaBox().getHeight() - MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                float headerBlockHeight = 50;

                contentStream.setNonStrokingColor(COLOR_PRIMARY);
                contentStream.addRect(MARGIN, yPosition - headerBlockHeight + 10, pageWidth, headerBlockHeight);
                contentStream.fill();

                contentStream.setNonStrokingColor(COLOR_WHITE);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);

                contentStream.newLineAtOffset(MARGIN + 20, yPosition - (headerBlockHeight / 2) + 10);
                contentStream.showText("INVOICE");
                contentStream.endText();

                contentStream.setNonStrokingColor(COLOR_BLACK);
                yPosition -= (headerBlockHeight + 40);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(MARGIN, yPosition);

                contentStream.showText("Order ID: " + order.getId());

                contentStream.newLineAtOffset(0, -18);

                String createdAtStr = "N/A";
                if (order.getCreatedAt() != null) {
                    createdAtStr = order.getCreatedAt().toLocalDate().format(DATE_FORMATTER);
                }
                contentStream.showText("Date: " + createdAtStr);

                contentStream.endText();

                yPosition -= 58;

                Table.TableBuilder tableBuilder = Table.builder()
                        .addColumnsOfWidth(pageWidth * 0.4f, pageWidth * 0.2f, pageWidth * 0.2f, pageWidth * 0.2f)
                        .font(PDType1Font.HELVETICA)
                        .fontSize(10)
                        .padding(8);

                tableBuilder.addRow(Row.builder()
                        .add(TextCell.builder().text("Product Name").font(PDType1Font.HELVETICA_BOLD).backgroundColor(COLOR_PRIMARY).textColor(COLOR_WHITE).borderWidth(0).build())
                        .add(TextCell.builder().text("Quantity").font(PDType1Font.HELVETICA_BOLD).backgroundColor(COLOR_PRIMARY).textColor(COLOR_WHITE).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
                        .add(TextCell.builder().text("Unit Price").font(PDType1Font.HELVETICA_BOLD).backgroundColor(COLOR_PRIMARY).textColor(COLOR_WHITE).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
                        .add(TextCell.builder().text("Subtotal").font(PDType1Font.HELVETICA_BOLD).backgroundColor(COLOR_PRIMARY).textColor(COLOR_WHITE).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
                        .build());

                for (int i = 0; i < items.size(); i++) {
                    OrderItem item = items.get(i);
                    String productName = productNames.get(i);
                    double subtotal = item.getQuantity() * item.getSellingPrice();
                    grandTotal += subtotal;

                    tableBuilder.addRow(Row.builder()
                            .add(TextCell.builder().text(String.valueOf(productName)).borderWidth(0).build())
                            .add(TextCell.builder().text(String.valueOf(item.getQuantity())).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
                            .add(TextCell.builder().text(String.format("%.2f", item.getSellingPrice())).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
                            .add(TextCell.builder().text(String.format("%.2f", subtotal)).horizontalAlignment(HorizontalAlignment.RIGHT).borderWidth(0).build())
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
                yPosition -= 30;

                contentStream.setNonStrokingColor(COLOR_BLACK);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();

                float grandTotalX = page.getMediaBox().getWidth() - MARGIN - 180;
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

