package com.graphinout.base.fetch;

import org.jspecify.annotations.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Decides whether an IP address is on the public internet, following the IANA special-purpose address registries:
 * an address in a block that is not "globally reachable" is not public.
 * <p>
 * Fetching is decided on resolved addresses, never on host names: {@code 2130706433}, {@code 0x7f.1} and any public
 * DNS name can all lead to {@code 127.0.0.1}. Addresses that embed an IPv4 address (IPv4-mapped, NAT64, 6to4) are
 * judged by that IPv4 address. See {@code doc/url-fetching-safe.adoc}.
 */
public final class PublicAddresses {

    /** A CIDR block. */
    private record Block(byte[] prefix, int bits) {

        static Block of(String cidr) {
            int slash = cidr.indexOf('/');
            try {
                return new Block(InetAddress.ofLiteral(cidr.substring(0, slash)).getAddress(),
                        Integer.parseInt(cidr.substring(slash + 1)));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Bad CIDR " + cidr, e);
            }
        }

        boolean contains(byte[] address) {
            if (address.length != prefix.length) return false;
            int full = bits / 8;
            for (int i = 0; i < full; i++) {
                if (address[i] != prefix[i]) return false;
            }
            int rest = bits % 8;
            if (rest == 0) return true;
            int mask = (0xFF << (8 - rest)) & 0xFF;
            return (address[full] & mask) == (prefix[full] & mask);
        }
    }

    /** IPv4 blocks that are not globally reachable (IANA IPv4 special-purpose registry). */
    private static final List<Block> NON_PUBLIC_V4 = List.of(
            Block.of("0.0.0.0/8"),          // "this network"
            Block.of("10.0.0.0/8"),         // private
            Block.of("100.64.0.0/10"),      // shared address space (carrier-grade NAT)
            Block.of("127.0.0.0/8"),        // loopback
            Block.of("169.254.0.0/16"),     // link-local, incl. cloud metadata 169.254.169.254
            Block.of("172.16.0.0/12"),      // private
            Block.of("192.0.0.0/24"),       // IETF protocol assignments
            Block.of("192.0.2.0/24"),       // documentation TEST-NET-1
            Block.of("192.88.99.0/24"),     // deprecated 6to4 relay anycast
            Block.of("192.168.0.0/16"),     // private
            Block.of("198.18.0.0/15"),      // benchmarking
            Block.of("198.51.100.0/24"),    // documentation TEST-NET-2
            Block.of("203.0.113.0/24"),     // documentation TEST-NET-3
            Block.of("224.0.0.0/4"),        // multicast
            Block.of("240.0.0.0/4")         // reserved, incl. limited broadcast 255.255.255.255
    );

    /** IPv6 blocks that are not globally reachable (IANA IPv6 special-purpose registry), minus the IPv4-embedding ones. */
    private static final List<Block> NON_PUBLIC_V6 = List.of(
            Block.of("::/128"),             // unspecified
            Block.of("::1/128"),            // loopback
            Block.of("100::/64"),           // discard-only
            Block.of("2001::/23"),          // IETF protocol assignments (incl. Teredo 2001::/32)
            Block.of("2001:db8::/32"),      // documentation
            Block.of("3fff::/20"),          // documentation
            Block.of("5f00::/16"),          // segment routing SIDs
            Block.of("fc00::/7"),           // unique local
            Block.of("fe80::/10"),          // link-local
            Block.of("fec0::/10"),          // deprecated site-local
            Block.of("ff00::/8")            // multicast
    );

    private static final Block IPV4_MAPPED = Block.of("::ffff:0:0/96");
    private static final Block IPV4_COMPATIBLE = Block.of("::/96");
    private static final Block NAT64 = Block.of("64:ff9b::/96");
    private static final Block NAT64_LOCAL = Block.of("64:ff9b:1::/48");
    private static final Block SIX_TO_FOUR = Block.of("2002::/16");

    private PublicAddresses() {}

    public static boolean isPublic(InetAddress address) {
        byte[] a = address.getAddress();
        if (address instanceof Inet4Address) {
            return isPublicV4(a);
        }
        if (address instanceof Inet6Address) {
            if (IPV4_MAPPED.contains(a) || IPV4_COMPATIBLE.contains(a) || NAT64.contains(a)) {
                return isPublicV4(new byte[]{a[12], a[13], a[14], a[15]});
            }
            if (NAT64_LOCAL.contains(a)) return false;
            if (SIX_TO_FOUR.contains(a)) {
                return isPublicV4(new byte[]{a[2], a[3], a[4], a[5]});
            }
            for (Block b : NON_PUBLIC_V6) {
                if (b.contains(a)) return false;
            }
            return true;
        }
        return false;
    }

    private static boolean isPublicV4(byte[] a) {
        for (Block b : NON_PUBLIC_V4) {
            if (b.contains(a)) return false;
        }
        return true;
    }

    /**
     * @return {@code addresses}, if every one is public
     * @throws BlockedAddressException if none are given, or any is not public: a host with one private address is
     *                                 refused as a whole, so a client can not fall back to it
     */
    public static InetAddress[] requirePublic(String host, InetAddress @Nullable [] addresses) throws BlockedAddressException {
        if (addresses == null || addresses.length == 0) {
            throw new BlockedAddressException(host, null);
        }
        for (InetAddress address : addresses) {
            if (!isPublic(address)) throw new BlockedAddressException(host, address);
        }
        return addresses;
    }

    /** The address of an IP literal host ({@code 127.0.0.1}, {@code [::1]}), or null for a host name. */
    public static @Nullable InetAddress literal(String host) {
        String h = host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
        try {
            return InetAddress.ofLiteral(h);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** A host resolves to an address that is not on the public internet. */
    public static final class BlockedAddressException extends UnknownHostException {

        public BlockedAddressException(String host, @Nullable InetAddress address) {
            super(address == null
                    ? "Host " + host + " has no address"
                    : "Host " + host + " resolves to non-public address " + address.getHostAddress() + "; refusing to connect");
        }
    }

}
