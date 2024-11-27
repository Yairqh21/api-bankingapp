package com.riaydev.bankingapp.Services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.riaydev.bankingapp.Entities.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    public void sendInvestmentPurchaseEmail(
            User user, BigDecimal quantity, String assetSymbol, BigDecimal amount,
            BigDecimal totalHoldings, BigDecimal purchasePrice, BigDecimal balance, BigDecimal netWorth) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Dear ").append(user.getName()).append(",\n\n")
                .append("You have successfully purchased ").append(quantity).append(" units of ")
                .append(assetSymbol).append(" for a total amount of $").append(amount).append(".\n\n")
                .append("Current holdings of ").append(assetSymbol).append(": ").append(totalHoldings)
                .append(" units\n\n")
                .append("Summary of current assets:\n")
                .append("- ").append(assetSymbol).append(": ").append(totalHoldings).append(" units purchased at $")
                .append(purchasePrice).append("\n\n")
                .append("Account Balance: $").append(balance).append("\n")
                .append("Net Worth: $").append(netWorth).append("\n\n")
                .append("Thank you for using our investment services.\n\n")
                .append("Best Regards,\n")
                .append("Investment Management Team");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Investment Purchase Confirmation");
        message.setText(emailBody.toString());

        mailSender.send(message);
    }

    public void sendInvestmentSaleEmail(User user, BigDecimal quantity, String assetSymbol, BigDecimal profitOrLoss,
            BigDecimal remainingQuantity, BigDecimal purchasePrice, BigDecimal balance, BigDecimal netWorth) {
                
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("Dear ").append(user.getName()).append(",\n\n")
                .append("You have successfully sold ").append(quantity).append(" units of ")
                .append(assetSymbol).append(".\n")
                .append("Total Gain/Loss: $").append(profitOrLoss).append(".\n\n")
                .append("Remaining holdings of ").append(assetSymbol).append(": ").append(remainingQuantity)
                .append(" units\n\n")
                .append("Summary of current assets:\n")
                .append("- ").append(assetSymbol).append(": ").append(remainingQuantity).append(" units purchased at $")
                .append(purchasePrice).append("\n\n")
                .append("Account Balance: $").append(balance).append("\n")
                .append("Net Worth: $").append(netWorth).append("\n\n")
                .append("Thank you for using our investment services.\n\n")
                .append("Best Regards,\n")
                .append("Investment Management Team");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Investment Sale Confirmation");
        message.setText(emailBody.toString());

        mailSender.send(message);
    }

}
