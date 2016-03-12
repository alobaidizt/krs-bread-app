package com.vizorteam.krsbreadapp;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by ziyad on 3/9/16.
 */
public class JobSchedulerService extends JobService {

//    private Handler mJobHandler = new Handler( new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage( Message msg ) {
//            Toast.makeText( getApplicationContext(),
//                    "JobService task running", Toast.LENGTH_SHORT )
//                    .show();
//            jobFinished( (JobParameters) msg.obj, false );
//            return true;
//        }
//
//    } );

    @Override
    public boolean onStartJob(JobParameters params) {
//        mJobHandler.sendMessage( Message.obtain( mJobHandler, 1, params ) );
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
//        mJobHandler.removeMessages( 1 );
        return false;
    }

}
