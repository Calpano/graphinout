package com.graphinout.base.http;

import com.graphinout.base.fetch.PublicAddresses;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Resolves host names, and refuses hosts with any non-public address.
 * <p>
 * Apache HttpClient connects to exactly the addresses a {@link DnsResolver} returns. So the addresses we check are the
 * addresses we connect to: no second lookup, no DNS rebinding. IP literals ({@code 127.0.0.1}, {@code [::1]}) are
 * checked the same way. Unlike a JEP 418 resolver, this applies to our HTTP client only, not to the whole JVM.
 */
public final class PublicOnlyDnsResolver implements DnsResolver {

    public static final PublicOnlyDnsResolver INSTANCE = new PublicOnlyDnsResolver(SystemDefaultDnsResolver.INSTANCE, PublicAddresses::isPublic);

    private final DnsResolver delegate;
    private final Predicate<InetAddress> allowed;

    /** @param allowed which addresses to accept; tests may widen it, production uses {@link PublicAddresses#isPublic} */
    PublicOnlyDnsResolver(DnsResolver delegate, Predicate<InetAddress> allowed) {
        this.delegate = delegate;
        this.allowed = allowed;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress literal = PublicAddresses.literal(host);
        InetAddress[] addresses = literal != null ? new InetAddress[]{literal} : delegate.resolve(host);
        if (addresses == null || addresses.length == 0) throw new PublicAddresses.BlockedAddressException(host, null);
        for (InetAddress address : addresses) {
            if (!allowed.test(address)) throw new PublicAddresses.BlockedAddressException(host, address);
        }
        return addresses;
    }

    @Override
    public List<InetSocketAddress> resolve(String host, int port) throws UnknownHostException {
        List<InetSocketAddress> result = new ArrayList<>();
        for (InetAddress address : resolve(host)) {
            result.add(new InetSocketAddress(address, port));
        }
        return result;
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        return delegate.resolveCanonicalHostname(host);
    }
}
