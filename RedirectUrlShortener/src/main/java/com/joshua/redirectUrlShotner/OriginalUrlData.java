package com.joshua.redirectUrlShotner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OriginalUrlData {

    private String originalUrl;
    private long expirationTime;

}