package org.iam.pojo.domain;

import lombok.Data;

import java.util.UUID;
@Data
public class KeyAddress {
    UUID applicationUuid;
    String pkcs1PrivatePath;
    String pkcs1PublicPath;
    String pkcs8PrivatePath;
    String pkcs8PublicPath;
    public KeyAddress(UUID applicationUuid){
        this.applicationUuid=applicationUuid;
        this.pkcs1PrivatePath = "src/main/resources/keys/"+applicationUuid+"/pkcs1/private_key.pem";
        this.pkcs1PublicPath = "src/main/resources/keys/"+applicationUuid+"/pkcs1/public_key.pem";
        this.pkcs8PrivatePath = "src/main/resources/keys/"+applicationUuid+"/pkcs8/private_key.pem";
        this.pkcs8PublicPath = "src/main/resources/keys/"+applicationUuid+"/pkcs8/public_key.pem";
    }
}
