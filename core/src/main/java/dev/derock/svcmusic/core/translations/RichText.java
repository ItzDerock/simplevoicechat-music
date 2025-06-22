package dev.derock.svcmusic.core.translations;

/**
 * Platform-agnostic container for fully-formated text
 * This is an immutable, "compiled" object that is ready to be sent to the client.
 */
public interface RichText {
    /**
     * Returns the underlying platform-specific object.
     * @return The native component (e.g., Fabric Text)
     */
    <T> T asNative();
}
