package io.jenkins.plugins.adobe.cloudmanager;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import hudson.util.Secret;
import io.jenkins.plugins.adobe.jwt.swagger.api.JwtApi;
import io.jenkins.plugins.adobe.jwt.swagger.invoker.ApiClient;
import io.jenkins.plugins.adobe.jwt.swagger.invoker.ApiException;
import io.jenkins.plugins.adobe.jwt.swagger.model.Token;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static io.jenkins.plugins.adobe.cloudmanager.AdobeioConstants.*;
import static io.jsonwebtoken.SignatureAlgorithm.*;

// an adaptation of:
// https://github.com/Adobe-Consulting-Services/acs-aem-commons/blob/59b165575017da39b61a317d89bb303571149bbc/bundle/src/main/java/com/adobe/acs/commons/adobeio/service/impl/IntegrationServiceImpl.java
public class CloudManagerAuthUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudManagerAuthUtil.class);
    private static final Base64.Decoder DECODER = Base64.getMimeDecoder();

    /**
     * Get access token
     *
     * @return
     */
    public static String getAccessToken(AdobeioConfig config) throws AdobeIOException {
        try {
            ApiClient client = new ApiClient();
            Token token = new JwtApi(client).authenticate(
                    safeGetPlainText(config.getApiKey()),
                    safeGetPlainText(config.getClientSecret())
                    , getJwtToken(config));
            return token.getAccessToken();
        } catch (ApiException ex) {
            throw new AdobeIOException(ex.getMessage());
        }
    }

    public static String safeGetPlainText(Secret secret) {
        return Optional.ofNullable(secret).map(Secret::getPlainText).orElse(null);
    }

    private static String getJwtToken(AdobeioConfig config) {
        String jwtToken;
        try {
            jwtToken =
                    Jwts.builder()
                            // claims
                            .setIssuer(config.getOrganizationID())
                            .setSubject(config.getTechnicalAccountId())
                            .setExpiration(getExpirationDate())
                            .setAudience(String.format("%s/c/%s", IMS_ENDPOINT, safeGetPlainText(config.getApiKey())))
                            .claim(CLOUD_MANAGER_JWT_SCOPE, Boolean.TRUE)
                            // sign
                            .signWith(RS256, getPrivateKey(config))
                            .compact();
        } catch (Exception e) { // yeah yeah, rethrow them all.
            throw new IllegalStateException("Error while generating JWT token", e);
        }
        return jwtToken;
    }

    private static PrivateKey getPrivateKey(AdobeioConfig config)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPrivateKey =
                Optional.ofNullable(safeGetPlainText(config.getPrivateKey()))
                        // Remove the "BEGIN" and "END" lines, as well as any whitespace
                        .map(k -> k.replaceAll("-----\\w+ PRIVATE KEY-----", ""))
                        .map(k -> k.replaceAll("\\s+", ""))
                        .map(DECODER::decode)
                        .orElse(null);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private static Date getExpirationDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, 24);
        return cal.getTime();
    }
}
