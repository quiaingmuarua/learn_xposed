package com.demo.java.xposed.rcs.fake;

import com.demo.java.xposed.utils.RandomUtils;

public class FakeSimIdGenerator {

    // Generates a fake SIM Serial Number (ICCID)
    public static String generateFakeSimId(String countryCode,String phoneNumber) {

        String mii = "89"; // Major Industry Identifier for telecom
//        String countryCode = "310"; // Example country code (310 for the USA)
        String issuerIdentifier = "24"; // Example issuer identifier

        // Generate a random unique identifier (10 digits)
        String uniqueId = RandomUtils.generateRandomNumber((long) phoneNumber.hashCode(),12);

        // Combine all parts
        String partialIccid = mii + countryCode + issuerIdentifier + uniqueId;

        // Calculate the Luhn check digit
        String checkDigit = calculateLuhnCheckDigit(partialIccid);

        // Combine to form the complete ICCID
        return partialIccid + checkDigit;
    }


    public static String generateFakeImsi(String mcc, String mnc,String phoneNumber) {
//        String mcc = "310"; // Example MCC for USA
//        String mnc = "150"; // Example MNC for a carrier
        String msin = RandomUtils.generateRandomNumber((long) phoneNumber.hashCode(),10); // Generate a 10-digit random MSIN

        return mcc + mnc + msin;
    }



    // Calculates the Luhn check digit for the given number
    private static String calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return Integer.toString(checkDigit);
    }


}
/*

8901240357126712418
8901240384812800733
*/