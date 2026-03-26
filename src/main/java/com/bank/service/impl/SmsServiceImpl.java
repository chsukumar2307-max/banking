package com.bank.service.impl;
import com.bank.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String OTP_MESSAGE_TEMPLATE = "Your OTP for Banking App is: %s";

    @Value("${app.sms.simulate:false}")
    private boolean simulateSms;

    @Value("${twilio.accountSid}")
    private String twilioAccountSid;

    @Value("${twilio.authToken}")
    private String twilioAuthToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    /**
     * Send an OTP SMS to the given phone number.
     * @param phone the recipient's phone number
     * @param otp the OTP code
     */
    @Override
    public void sendOtp(String phone, String otp) {
        if (!isValid(phone, otp)) {
            logWarnInvalidInput(phone, otp);
            return;
        }
        if (simulateSms) {
            log.info("[SIMULATION] OTP SMS to {}: {}", phone, otp);
            return;
        }
        try {
            sendViaTwilio(phone, otp);
        } catch (IllegalArgumentException e) {
            log.error("Invalid phone number format for {}: {}", phone, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send OTP SMS to {} via Twilio. Error: {}", phone, e.getMessage(), e);
        }
    }

    private boolean isValid(String phone, String otp) {
        return StringUtils.hasText(phone) && StringUtils.hasText(otp);
    }

    private void logWarnInvalidInput(String phone, String otp) {
        log.warn("Phone number or OTP is blank (phone: '{}', otp: '{}'), not sending SMS.", phone, otp);
    }

    /**
     * Protected for testability.
     */
    protected void sendViaTwilio(String phone, String otp) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message message = Message.creator(
                new PhoneNumber(phone),
                new PhoneNumber(twilioPhoneNumber),
                String.format(OTP_MESSAGE_TEMPLATE, otp)
        ).create();
        log.info("Twilio SMS sent to {}: SID {}", phone, message.getSid());
    }
}
