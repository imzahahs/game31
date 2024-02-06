package sengine.video;

public interface PlatformProvider {
    PlatformHandle open(String filename);
    Metadata inspect(String filename);
}
