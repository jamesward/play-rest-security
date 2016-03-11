package utils;

import com.google.inject.AbstractModule;

public class DemoDataModule extends AbstractModule {
    protected void configure() {
        bind(DemoData.class).asEagerSingleton();
    }
}