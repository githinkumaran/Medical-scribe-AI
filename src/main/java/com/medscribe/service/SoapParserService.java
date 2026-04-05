package com.medscribe.service;

import com.medscribe.model.Session;
import com.medscribe.model.SoapNote;
import org.springframework.stereotype.Service;

@Service
public class SoapParserService {

    public SoapNote parse(String rawSoap, Session session) {

        SoapNote soapNote = new SoapNote();
        soapNote.setSession(session);

        // Extract each SOAP section using markers
        soapNote.setSubjective(extractSection(rawSoap, "SUBJECTIVE:", "OBJECTIVE:"));
        soapNote.setObjective(extractSection(rawSoap, "OBJECTIVE:", "ASSESSMENT:"));
        soapNote.setAssessment(extractSection(rawSoap, "ASSESSMENT:", "PLAN:"));
        soapNote.setPlan(extractSection(rawSoap, "PLAN:", null));

        return soapNote;
    }

    private String extractSection(String text, String startMarker, String endMarker) {
        try {
            int startIndex = text.toUpperCase().indexOf(startMarker);
            if (startIndex == -1) return "";

            startIndex += startMarker.length();

            int endIndex;
            if (endMarker != null) {
                endIndex = text.toUpperCase().indexOf(endMarker, startIndex);
                if (endIndex == -1) endIndex = text.length();
            } else {
                endIndex = text.length();
            }

            return text.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            return "";
        }
    }
}