package com.bank.impl;

import com.bank.service.impl.SmsServiceImpl;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SmsServiceImplTest {
    @InjectMocks
    SmsServiceImpl smsServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(smsServiceImpl, "simulateSms", true);
        ReflectionTestUtils.setField(smsServiceImpl, "twilioAccountSid", "sid");
        ReflectionTestUtils.setField(smsServiceImpl, "twilioAuthToken", "token");
        ReflectionTestUtils.setField(smsServiceImpl, "twilioPhoneNumber", "+1234567890");
    }

    @Test
    void sendOtp_simulationMode_logsInfo() {
        // Should not throw and should log info (log not asserted here)
        assertDoesNotThrow(() -> smsServiceImpl.sendOtp("+1111111111", "1234"));
    }

    @Test
    void sendOtp_twilioSuccess() {
        ReflectionTestUtils.setField(smsServiceImpl, "simulateSms", false);
        try (MockedStatic<com.twilio.Twilio> twilioMock = mockStatic(com.twilio.Twilio.class);
             MockedStatic<Message> messageMockStatic = mockStatic(Message.class)) {
            // Mock Twilio.init to do nothing
            twilioMock.when(() -> com.twilio.Twilio.init(anyString(), anyString())).then(invocation -> null);
            // Mock the creator and message
            MessageCreator creatorMock = mock(MessageCreator.class);
            Message messageMock = mock(Message.class);
            when(messageMock.getSid()).thenReturn("sid");
            when(creatorMock.create()).thenReturn(messageMock);
            messageMockStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
                    .thenReturn(creatorMock);
            assertDoesNotThrow(() -> smsServiceImpl.sendOtp("+1111111111", "1234"));
        }
    }

    @Test
    void sendOtp_twilioFailure_throwsException() {
        ReflectionTestUtils.setField(smsServiceImpl, "simulateSms", false);
        try (MockedStatic<com.twilio.Twilio> twilioMock = mockStatic(com.twilio.Twilio.class);
             MockedStatic<Message> messageMockStatic = mockStatic(Message.class)) {
            twilioMock.when(() -> com.twilio.Twilio.init(anyString(), anyString())).then(invocation -> null);
            MessageCreator creatorMock = mock(MessageCreator.class);
            when(creatorMock.create()).thenThrow(new RuntimeException("Twilio error"));
            messageMockStatic.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
                    .thenReturn(creatorMock);
            assertThrows(RuntimeException.class, () -> smsServiceImpl.sendOtp("+1111111111", "1234"));
        }
    }

    @Test
    void sendOtp_nullPhone_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> smsServiceImpl.sendOtp(null, "1234"));
    }

    @Test
    void sendOtp_nullOtp_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> smsServiceImpl.sendOtp("+1111111111", null));
    }

    @Test
    void sendOtp_emptyPhone_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> smsServiceImpl.sendOtp("", "1234"));
    }

    @Test
    void sendOtp_emptyOtp_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> smsServiceImpl.sendOtp("+1111111111", ""));
    }
}
