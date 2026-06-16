package com.graphinout.reader.textbased.adjlist;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEndpoint;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Shared helper for the text-based writers: derive a (source, target) pair from an edge's endpoints. Uses the
 * IN/OUT directions when present (CJ directed edge), otherwise the first two endpoints positionally (the textbased
 * readers store edges as two ordered, direction-less endpoints).
 */
final class TextEndpoints {

    private TextEndpoints() {
    }

    static String @Nullable [] sourceTarget(List<ICjEndpoint> endpoints) {
        ICjEndpoint in = null, out = null;
        for (ICjEndpoint ep : endpoints) {
            if (ep.direction() == CjDirection.IN && in == null) in = ep;
            else if (ep.direction() == CjDirection.OUT && out == null) out = ep;
        }
        if (in != null && out != null) {
            return new String[]{in.node(), out.node()};
        }
        if (endpoints.size() == 2) {
            return new String[]{endpoints.get(0).node(), endpoints.get(1).node()};
        }
        return null;
    }

}
