package com.gxt.identity.port;

import com.gxt.identity.port.model.GoogleUserInfo;

public interface GoogleTokenVerifierPort {
    GoogleUserInfo verifyIdToken(String idToken);
}
