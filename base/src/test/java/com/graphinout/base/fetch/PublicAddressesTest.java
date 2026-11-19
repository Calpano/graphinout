package com.graphinout.base.fetch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicAddressesTest {

    static boolean isPublic(String literal) throws UnknownHostException {
        return PublicAddresses.isPublic(InetAddress.getByName(literal));
    }

    @Test
    @DisplayName("non-public IPv4 and IPv6 addresses, including disguised encodings, are refused")
    void nonPublic() throws UnknownHostException {
        for (String a : List.of(
                "0.0.0.0", "127.0.0.1", "127.255.255.254", "10.1.2.3", "172.16.0.1", "172.31.255.255", "192.168.1.1",
                "100.64.0.1", "169.254.169.254", "192.0.0.8", "192.0.2.1", "198.18.0.1", "198.51.100.7", "203.0.113.9",
                "224.0.0.1", "239.255.255.250", "240.0.0.1", "255.255.255.255",
                "2130706433",            // 127.0.0.1 as one decimal number
                "127.1",                 // short form
                "::", "::1", "fc00::1", "fd12:3456::1", "fe80::1", "fec0::1", "ff02::1", "2001:db8::1", "100::1",
                "2001::1",               // Teredo
                "::ffff:127.0.0.1",      // IPv4-mapped
                "::ffff:169.254.169.254",
                "::127.0.0.1",           // IPv4-compatible
                "64:ff9b::a9fe:a9fe",    // NAT64 of 169.254.169.254
                "64:ff9b:1::1",
                "2002:7f00:1::1",        // 6to4 of 127.0.0.1
                "2002:c0a8:101::1")) {   // 6to4 of 192.168.1.1
            assertWithMessage(a).that(isPublic(a)).isFalse();
        }
    }

    @Test
    void publicAddresses() throws UnknownHostException {
        for (String a : List.of("1.1.1.1", "8.8.8.8", "93.184.215.14", "172.32.0.1", "100.128.0.1", "192.169.0.1",
                "2606:4700:4700::1111", "2a00:1450:4001:80b::200e", "::ffff:8.8.8.8", "64:ff9b::808:808", "2002:808:808::1")) {
            assertWithMessage(a).that(isPublic(a)).isTrue();
        }
    }

    @Test
    @DisplayName("one non-public address refuses the whole host")
    void requirePublic() throws UnknownHostException {
        InetAddress pub = InetAddress.getByName("8.8.8.8");
        InetAddress priv = InetAddress.getByName("10.0.0.1");
        assertThrows(PublicAddresses.BlockedAddressException.class, () -> PublicAddresses.requirePublic("h", new InetAddress[]{pub, priv}));
        assertThrows(PublicAddresses.BlockedAddressException.class, () -> PublicAddresses.requirePublic("h", new InetAddress[0]));
        PublicAddresses.requirePublic("h", new InetAddress[]{pub});
    }

    @Test
    void literal() {
        assertWithMessage("v4").that(PublicAddresses.literal("127.0.0.1")).isNotNull();
        assertWithMessage("v6").that(PublicAddresses.literal("[::1]")).isNotNull();
        assertWithMessage("name").that(PublicAddresses.literal("example.org")).isNull();
    }
}
