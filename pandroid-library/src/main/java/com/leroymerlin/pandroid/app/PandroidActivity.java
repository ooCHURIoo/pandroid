package com.leroymerlin.pandroid.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.leroymerlin.pandroid.PandroidApplication;
import com.leroymerlin.pandroid.app.delegate.PandroidDelegate;
import com.leroymerlin.pandroid.event.EventBusManager;
import com.leroymerlin.pandroid.event.FragmentOpener;
import com.leroymerlin.pandroid.event.OnBackListener;
import com.leroymerlin.pandroid.event.ReceiversProvider;
import com.leroymerlin.pandroid.future.CancellableActionDelegate;
import com.leroymerlin.pandroid.log.LogWrapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by florian on 05/11/14.
 * <p/>
 * PandroidActivity is a Activity that simplify the activity cycle of life by introducing onResume(ResumeState) method.
 * This Activity handle support toolbar. You just have to use LinearToolbarLayout as root view or an equivalent view.
 * Back event and fragment back stack changed are treated to.
 * If static field TAG is set PandroidActivity inject Broadcast receiver himself
 */
public class PandroidActivity extends AppCompatActivity implements CancellableActionDelegate.ActionDelegateRegister, ReceiversProvider {

    //tag::PandroidActivityInjection[]
    @Inject
    protected LogWrapper logWrapper;
    @Inject
    protected EventBusManager eventBusManager;

    //end::PandroidActivityInjection[]

    protected PandroidDelegate pandroidDelegate;

    //tag::PandroidActivityInjection[]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Inject dagger base component
        PandroidApplication pandroidApplication = PandroidApplication.get(this);
        pandroidApplication.inject(this);

        //initialize PandroidDelegate with the default from PandroidApplication
        pandroidDelegate = pandroidApplication.createBasePandroidDelegate();
        //end::PandroidActivityInjection[]

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        pandroidDelegate.onCreateView(this, findViewById(android.R.id.content), savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pandroidDelegate.onResume(this);
        ResumeState state = pandroidDelegate.getResumeState();
        onResume(state);
    }

    //tag::PandroidActivityResume[]
    /**
     * call at the end of onResume process. This method will help you determine the king of resume
     * your activity is facing
     *
     * @param state current resume stat of the activity
     */
    protected void onResume(ResumeState state) {
        logWrapper.i(getClass().getSimpleName(), "resume state: " + state);
    }
    //end::PandroidActivityResume[]

    @Override
    protected void onPause() {
        super.onPause();
        pandroidDelegate.onPause(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        pandroidDelegate.onSaveView(this, outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pandroidDelegate.onDestroyView(this);
    }

    public Fragment getCurrentFragment() {
        return null;
    }

    @Override
    public void registerDelegate(CancellableActionDelegate delegate) {
        pandroidDelegate.registerDelegate(delegate);
    }

    @Override
    public boolean unregisterDelegate(CancellableActionDelegate delegate) {
        return pandroidDelegate.unregisterDelegate(delegate);
    }


    //tag::PandroidActivityBack[]
    @Override
    public void onBackPressed() {
        Fragment fragment = getCurrentFragment(); //override this methode to give your current fragment
        if (fragment != null && fragment instanceof OnBackListener && ((OnBackListener) fragment).onBackPressed()) {
            //back handle by current fragment
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            //back handle by getFragmentManager
            getFragmentManager().popBackStack();
        } else {
            onBackExit();
        }
    }

    //end::PandroidActivityBack[]

    private boolean existOnBack = false;

    private void onBackExit() {
        if (!existOnBack && showExitMessage()) {
            existOnBack = true;
        } else {
            super.onBackPressed();
        }
    }

    //tag::PandroidActivityBack[]
    /**
     * Override this method to show an exit message and enable back confirmation to exit
     * @return true to stop the app exit, false otherwise
     */
    protected boolean showExitMessage() {
        return false;
    }
    //end::PandroidActivityBack[]


    //tag::PandroidActivityReceivers[]

    /**
     * Override this method to automatically (un)register receiver to the event bus with the activity life cycle
     * @return list of receivers attached by EventBusLifecycleDelegate
     */
    @Override
    public List<EventBusManager.EventBusReceiver> getReceivers() {
        return new ArrayList<>();
    }
    //end::PandroidActivityReceivers[]

    public void startFragment(Class<? extends PandroidFragment> fragmentClass) {
        sendEventSync(new FragmentOpener(fragmentClass));
    }

    public void sendEvent(Object event) {
        eventBusManager.send(event);
    }

    public void sendEventSync(Object event) {
        eventBusManager.sendSync(event);
    }


}
