package com.bendaniel10.safefragmenttransactions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayDeque;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    Queue<DeferredFragmentTransaction> deferredFragmentTransactions = new ArrayDeque<>();
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Replaces the fragment currently occupying the view with id contentFrameId. This transaction will be added to the back stack
     *
     * @param contentFrameId    the view the fragment is going to be inflated in
     * @param replacingFragment the fragment that is going to be inflated.
     */
    public void replaceFragmentAddToBackStack(int contentFrameId, android.support.v4.app.Fragment replacingFragment) {

        if (!isRunning) {
            //This will handle switching of fragments when the activity is paused. To prevent IllegalSTateExecption.
            //This transaction will be used in the resume part too.
            DeferredFragmentTransaction deferredFragmentTransaction = new DeferredFragmentTransaction() {
                @Override
                public void commit() {
                    replaceFragmentAddToBackStackInternal(getContentFrameId(), getReplacingFragment());
                }
            };

            deferredFragmentTransaction.setContentFrameId(contentFrameId);
            deferredFragmentTransaction.setReplacingFragment(replacingFragment);

            deferredFragmentTransactions.add(deferredFragmentTransaction);
        } else {
            replaceFragmentAddToBackStackInternal(contentFrameId, replacingFragment);
        }
    }

    private void replaceFragmentAddToBackStackInternal(int contentFrameId, Fragment replacingFragment) {

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(contentFrameId, replacingFragment)
                .addToBackStack(replacingFragment.getClass().getSimpleName())
                .commit();
    }


    /**
     * Replaces the fragment currently occupying the view with id contentFrameId.
     *
     * @param contentFrameId    the view the fragment is going to be inflated in
     * @param replacingFragment the fragment that is going to be inflated.
     */
    public void replaceFragment(int contentFrameId, android.support.v4.app.Fragment replacingFragment) {
        if (!isRunning) {
            //This will handle switching of fragments when the activity is paused. To prevent IllegalSTateExecption.
            //This transaction will be used in the resume part too.
            DeferredFragmentTransaction deferredFragmentTransaction = new DeferredFragmentTransaction() {
                @Override
                public void commit() {
                    replaceFragmentInternal(getContentFrameId(), getReplacingFragment());
                }
            };

            deferredFragmentTransaction.setContentFrameId(contentFrameId);
            deferredFragmentTransaction.setReplacingFragment(replacingFragment);

            deferredFragmentTransactions.add(deferredFragmentTransaction);
        } else {
            replaceFragmentInternal(contentFrameId, replacingFragment);
        }
    }

    private void replaceFragmentInternal(int contentFrameId, Fragment replacingFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(contentFrameId, replacingFragment)
                .commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        while (!deferredFragmentTransactions.isEmpty()) {
            deferredFragmentTransactions.remove().commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }
}
