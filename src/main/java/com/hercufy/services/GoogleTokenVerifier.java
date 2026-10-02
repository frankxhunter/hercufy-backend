package com.hercufy.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hercufy.exceptions.GoogleUnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * El login con Google se mantiene en el backend a modo de plantilla, aunque el
 * frontend no lo use todavia. Mientras no se configure GOOGLE_CLIENT_ID, el
 * endpoint /api/auth/google seguira disponible pero rechazara cualquier token
 * (no hay audiencia real contra la que verificar).
 */
@Service
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${GOOGLE_CLIENT_ID:}") String googleClientId) throws Exception {
        verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                new GsonFactory()
        ).setAudience(Collections.singletonList(googleClientId)).build();
    }

    public Payload verify(String token) throws Exception {
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken == null || idToken.getPayload() == null) {
            throw new GoogleUnauthorizedException("Invalid Google token");
        }
        return idToken.getPayload();
    }
}
