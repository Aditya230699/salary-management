package com.salarymanagement.service;

import com.salarymanagement.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the countries the organisation operates in and the currency
 * each one is paid in. Previously this mapping was duplicated across the seeder, the
 * dashboard aggregation, and the frontend filter list, which meant adding a country
 * required three coordinated edits and any drift silently produced wrong reports.
 */
@Component
public class CurrencyResolver {

    private static final Map<String, String> COUNTRY_CURRENCIES = new LinkedHashMap<>();

    static {
        COUNTRY_CURRENCIES.put("India", "INR");
        COUNTRY_CURRENCIES.put("USA", "USD");
        COUNTRY_CURRENCIES.put("UK", "GBP");
        COUNTRY_CURRENCIES.put("Germany", "EUR");
        COUNTRY_CURRENCIES.put("Australia", "AUD");
    }

    public String currencyFor(String country) {
        String currency = COUNTRY_CURRENCIES.get(country);
        if (currency == null) {
            throw new ValidationException(
                    "Unsupported country '" + country + "'. Supported: " + COUNTRY_CURRENCIES.keySet());
        }
        return currency;
    }

    public List<String> supportedCountries() {
        return List.copyOf(COUNTRY_CURRENCIES.keySet());
    }

    public Map<String, String> countryCurrencies() {
        return Map.copyOf(COUNTRY_CURRENCIES);
    }
}
