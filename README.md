# MedScribe AI — AI Medical Scribe Application

An AI-powered medical scribe that converts doctor-patient conversations into structured SOAP notes and exports them as FHIR R4 bundles.

What it does:

*Records doctor-patient consultation audio
*Whisper AI transcribes speech to text in real time
*Groq LLM (Llama 3) structures transcript into SOAP format
*Exports as FHIR R4 bundle (international healthcare standard)
*Full React dashboard for doctors to review and approve notes

Tech Stack:

| Layer                 |        Technology         |
|-----------------------|---------------------------|
| Backend               | Java 17 + Spring Boot 3.2 |
| Frontend              | React 18                  |
| Database              | MS SQL Server             |
| Speech-to-Text        | OpenAI Whisper (local)    |
| LLM                   | Groq API (Llama 3.1)      |
| Healthcare Standard   | HL7 FHIR R4               |
| Transcription Service | Python Flask              |


Features:

*Real-time audio transcription using Whisper
*AI SOAP note generation using Groq LLM
*FHIR R4 bundle export (Encounter, Condition, Observation, MedicationRequest)
*Doctor review and approval workflow
*Audit logging for every action
*WebSocket support for real-time updates


How to Run:

*Prerequisites
*Java 17+
*Python 3.14+
*Node.js 18+
*MS SQL Server Express


Backend:

*application.properties - create one

Run Spring Boot
*./mvnw spring-boot:run


Whisper Service:

*pip install flask openai-whisper torch
*py -3.14 whisper_service.py



Frontend:

*cd frontend
*npm install
*npm start




