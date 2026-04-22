package com.example.inventoryapp;

import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class EmailSender {

    public interface EmailCallback {
        void onResult(boolean success);
    }

    public static void sendApprovalEmail(String recipientEmail, EmailCallback callback) {
        new SendEmailTask(recipientEmail, "Account Approved - Invento", getApprovalHtml(), null, callback).execute();
    }

    public static void sendInvoiceEmail(Context context, String recipientEmail, String customerName, List<SellStockActivity.CartItem> cart, EmailCallback callback) {
        new SendEmailTask(recipientEmail, "Your Invoice - Invento", getInvoiceHtml(customerName, cart), context, callback).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String recipient;
        private final String subject;
        private final String htmlContent;
        private final Context context;
        private final EmailCallback callback;

        SendEmailTask(String recipient, String subject, String htmlContent, Context context, EmailCallback callback) {
            this.recipient = recipient;
            this.subject = subject;
            this.htmlContent = htmlContent;
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            final String username = SecretConfig.SENDER_EMAIL;
            final String password = SecretConfig.APP_PASSWORD;

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username, "Invento Team"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);

                Multipart multipart = new MimeMultipart("related");

                // HTML part
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");
                multipart.addBodyPart(messageBodyPart);

                // Logo part
                if (context != null) {
                    BodyPart imageBodyPart = new MimeBodyPart();
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo);
                    if (bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        DataSource dataSource = new ByteArrayDataSource(imageBytes, "image/png");
                        imageBodyPart.setDataHandler(new DataHandler(dataSource));
                        imageBodyPart.setHeader("Content-ID", "<logo>");
                        imageBodyPart.setDisposition(MimeBodyPart.INLINE);
                        multipart.addBodyPart(imageBodyPart);
                    }
                }

                message.setContent(multipart);
                Transport.send(message);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                callback.onResult(success);
            }
        }
    }

    private static String getApprovalHtml() {
        return "<html>" +
                "<head><meta name='viewport' content='width=device-width, initial-scale=1.0'></head>" +
                "<body style='font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; background-color: #F5F7FA; padding: 20px; margin: 0;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>" +
                "    <div style='background-color: #252F6B; padding: 30px; text-align: center;'>" +
                "      <h1 style='color: #FFFFFF; margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 800;'>INVENTO</h1>" +
                "      <p style='color: #B0B7FF; margin: 5px 0 0 0; font-size: 13px; text-transform: uppercase; letter-spacing: 1px;'>Smart Inventory Solutions</p>" +
                "    </div>" +
                "    <div style='padding: 40px 30px; color: #333333; line-height: 1.6;'>" +
                "      <h2 style='color: #252F6B; margin-top: 0; font-size: 22px;'>Welcome to the Team!</h2>" +
                "      <p style='font-size: 16px;'>Hello,</p>" +
                "      <p style='font-size: 16px;'>We are pleased to inform you that your account registration has been <b>officially approved</b> by the administration.</p>" +
                "      <div style='background-color: #F0F4FF; padding: 20px; border-left: 5px solid #252F6B; margin: 25px 0; border-radius: 4px;'>" +
                "        <p style='margin: 0; color: #252F6B; font-weight: 600;'>Status: Fully Activated</p>" +
                "        <p style='margin: 10px 0 0 0; font-size: 15px;'>Admin has approved your account you can access using your credentials.</p>" +
                "      </div>" +
                "      <p style='font-size: 16px;'>You can now log in to manage stock, view analytics, and process sales logs seamlessly.</p>" +
                "    </div>" +
                "    <div style='background-color: #F9F9F9; padding: 25px; text-align: center; color: #8A8A8A; font-size: 13px; border-top: 1px solid #EEEEEE;'>" +
                "      <p style='margin: 0;'>Many Thanks,</p>" +
                "      <p style='margin: 5px 0 15px 0; font-weight: bold; color: #252F6B;'>The Invento Team</p>" +
                "      <p style='margin: 0;'>&copy; 2024 Invento Management. All rights reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    private static String getInvoiceHtml(String customerName, List<SellStockActivity.CartItem> cart) {
        StringBuilder itemsHtml = new StringBuilder();
        double grandTotal = 0;

        for (SellStockActivity.CartItem item : cart) {
            double total = item.quantity * item.inventoryItem.getPrice();
            grandTotal += total;
            itemsHtml.append("<tr>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #EEEEEE; word-break: break-all;'>").append(item.inventoryItem.getName()).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #EEEEEE; text-align: center;'>").append(item.quantity).append("</td>")
                    .append("<td style='padding: 10px; border-bottom: 1px solid #EEEEEE; text-align: right;'>₹").append(String.format(Locale.getDefault(), "%.2f", total)).append("</td>")
                    .append("</tr>");
        }

        return "<html>" +
                "<head>" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "  <style>" +
                "    body { margin: 0; padding: 0; width: 100% !important; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }" +
                "    .ExternalClass { width: 100%; }" +
                "    img { outline: none; text-decoration: none; -ms-interpolation-mode: bicubic; }" +
                "    table { border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; }" +
                "    @media only screen and (max-width: 480px) {" +
                "      .container { width: 100% !important; border-radius: 0 !important; }" +
                "      .header { padding: 20px 10px !important; }" +
                "      .content { padding: 20px 15px !important; }" +
                "      .item-table th, .item-table td { font-size: 12px !important; padding: 8px 4px !important; }" +
                "      .logo { width: 60px !important; height: auto !important; }" +
                "    }" +
                "  </style>" +
                "</head>" +
                "<body style='font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif; background-color: #F5F7FA; padding: 10px; margin: 0;'>" +
                "  <div class='container' style='max-width: 600px; margin: 0 auto; background-color: #FFFFFF; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>" +
                "    <div class='header' style='background-color: #252F6B; padding: 30px; text-align: center;'>" +
                "      <img src='cid:logo' alt='Invento Logo' class='logo' style='width: 80px; height: 80px; margin-bottom: 10px;' />" +
                "      <h1 style='color: #FFFFFF; margin: 0; font-size: 28px; letter-spacing: 2px; font-weight: 800;'>INVENTO</h1>" +
                "      <p style='color: #B0B7FF; margin: 5px 0 0 0; font-size: 12px; text-transform: uppercase; letter-spacing: 1px;'>Smart Inventory Solutions</p>" +
                "    </div>" +
                "    <div class='content' style='padding: 25px; color: #333333;'>" +
                "      <h2 style='color: #252F6B; margin-top: 0; font-size: 20px;'>Tax Invoice</h2>" +
                "      <p style='font-size: 14px;'>Dear <b>" + customerName + "</b>,</p>" +
                "      <p style='font-size: 14px;'>Thank you for your business. Summary of your purchase:</p>" +
                "      <div style='width: 100%;'>" +
                "        <table class='item-table' style='width: 100%; border-collapse: collapse; margin: 20px 0; table-layout: fixed;'>" +
                "          <thead>" +
                "            <tr style='background-color: #F8F9FA;'>" +
                "              <th style='width: 50%; padding: 10px; text-align: left; border-bottom: 2px solid #252F6B; font-size: 13px;'>Item</th>" +
                "              <th style='width: 20%; padding: 10px; text-align: center; border-bottom: 2px solid #252F6B; font-size: 13px;'>Qty</th>" +
                "              <th style='width: 30%; padding: 10px; text-align: right; border-bottom: 2px solid #252F6B; font-size: 13px;'>Total</th>" +
                "            </tr>" +
                "          </thead>" +
                "          <tbody>" +
                itemsHtml.toString() +
                "          </tbody>" +
                "        </table>" +
                "      </div>" +
                "      <div style='text-align: right; padding: 15px; background-color: #F0F4FF; border-radius: 8px; margin-top: 20px;'>" +
                "        <span style='font-size: 16px; color: #252F6B;'>Grand Total:</span>" +
                "        <span style='font-size: 22px; font-weight: bold; color: #252F6B; margin-left: 10px;'>₹" + String.format(Locale.getDefault(), "%.2f", grandTotal) + "</span>" +
                "      </div>" +
                "    </div>" +
                "    <div style='background-color: #F9F9F9; padding: 20px; text-align: center; color: #8A8A8A; font-size: 12px; border-top: 1px solid #EEEEEE;'>" +
                "      <p style='margin: 0;'>Thank you for choosing <b>Invento</b>!</p>" +
                "      <p style='margin: 5px 0 0 0;'>&copy; 2024 Invento Management. All rights reserved.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }
}
