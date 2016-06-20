package com.example.bkkinfoplus;

import com.example.bkkinfoplus.ui.AlertListFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Required interface for Dagger code generation.
 * The methods are used where field injection is needed.
 */

@Singleton
@Component(modules={AppModule.class, NetModule.class})
public interface AppComponent {

    void inject(AlertListFragment alertListFragment);

}
