package com.calpano.graphinout.foundation.json.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A navigation path has no wildcards.
 */
public interface IJsonNavigationPath {

    IJsonNavigationPath EMPTY = of(Collections.emptyList());

    static IJsonNavigationPath of(List<IJsonContainerNavigationStep> steps) {
        return new IJsonNavigationPath() {
            @Override
            public List<IJsonContainerNavigationStep> steps() {
                return steps;
            }

            @Override
            public String toString() {
                return steps.stream().map(Object::toString).reduce((a, b) -> a + "/" + b).orElse("--");
            }
        };
    }

    List<IJsonContainerNavigationStep> steps();

    default IJsonNavigationPath withAppend(IJsonContainerNavigationStep step) {
        ArrayList<IJsonContainerNavigationStep> steps = new ArrayList<>(this.steps());
        steps.add(step);
        return of(steps);
    }


}
