package com.graphinout.foundation.pure.functional;

/**
 * Interface for providing an alternative action to be executed if a certain condition is not met. This is typically
 * used in a functional programming context where you want to specify a fallback action.
 */
public interface IOrElse {

    /** An instance of IOrElse that executes the provided else-runnable. */
    IOrElse EXECUTE_ELSE = Runnable::run;
    /** An instance of IOrElse that does not execute the provided else-runnable. */
    IOrElse DONT_EXECUTE_ELSE = runnable -> {};

    /** Get an instance of IOrElse that executes the provided else-runnable if the provided flag is true. */
    static IOrElse create(boolean executeElse) {
        return executeElse ? EXECUTE_ELSE : DONT_EXECUTE_ELSE;
    }

    /** Execute the provided runnable if the condition is not met. */
    void orElse(Runnable runnable);

}
