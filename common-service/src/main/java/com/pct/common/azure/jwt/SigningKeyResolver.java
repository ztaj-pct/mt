package com.pct.common.azure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class SigningKeyResolver extends SigningKeyResolverAdapter {
	
	private static final String NBF = "nbf";
	private static final String PEMSTART = "-----BEGIN CERTIFICATE-----\n";
	private static final String PEMEND = "\n-----END CERTIFICATE-----\n";

    private AADKeySet keySet;

    public SigningKeyResolver(String authority) throws Exception {
        keySet = getSigningKeys(authority);
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
    	if(claims.containsKey(NBF)) {
    		claims.remove(NBF);
    	}
    	String tokenKeyId = jwsHeader.getKeyId();
        for(JsonWebKey key: keySet.getKeys()){
            if(key.getKid().equalsIgnoreCase(tokenKeyId)){
                return generatePublicKey(key);
                //return generatePublicKeyUsingPom(key);
            }
        }

        throw new JwtValidationException("Signature validation failed: Could not find a key with matching kid");
    }

    private AADKeySet getSigningKeys(String authority)throws Exception {
        OpenIdConnectConfiguration openIdConfig = getOpenIdConfiguration(authority);
        return getKeysFromJwkUri(openIdConfig.getJwksUri());
    }

    private OpenIdConnectConfiguration getOpenIdConfiguration(String authority) throws Exception {

        String openIdConnectDiscoveryEndpoint = authority + "/.well-known/openid-configuration";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(openIdConnectDiscoveryEndpoint)
                .build();

        Response response = client.newCall(request).execute();
        String responseJson =  response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseJson, OpenIdConnectConfiguration.class);
    }

    private AADKeySet getKeysFromJwkUri(String jwksUri) throws Exception {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(jwksUri)
                .build();

        Response response = client.newCall(request).execute();
        String responseJson =  response.body().string();

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(responseJson, AADKeySet.class);
    }

    private PublicKey generatePublicKey(JsonWebKey key) {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.getN()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(key.getE()));

            RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(publicSpec);
        } catch(Exception e){
            throw new JwtValidationException("Key generation failed", e);
        }
    }
    
    private PublicKey generatePublicKeyUsingPom(JsonWebKey key) {
        try {
            String x5c = key.getX5c().get(0);
            
            String certStr = PEMSTART + x5c + PEMEND;
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate  x509Certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certStr.getBytes()));
           
            return x509Certificate.getPublicKey();
        } catch(Exception e){
            throw new JwtValidationException("Key generation failed", e);
        }
    }
}
