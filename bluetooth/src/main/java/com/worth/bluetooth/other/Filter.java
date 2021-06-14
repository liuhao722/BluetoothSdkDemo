package com.worth.bluetooth.other;

public interface Filter {

    /**
     * Check if a given response should be published.
     * @param response a given response
     * @return if the response should be published
     */
    boolean isCorrect(String response);

}
