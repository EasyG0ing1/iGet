package com.simtechdata.crypto;

public enum Algo {
    SHA_256,
    SHA_512,
    SHA3_512;

    private static final String sha256 = "SHA-256";
    private static final String sha512 = "SHA-512";
    private static final String sha3512 = "SHA3-512";


    public String getName() {
        return switch (this) {
            case SHA_256 -> sha256;
            case SHA_512 -> sha512;
            case SHA3_512 -> sha3512;
        };
    }

}
