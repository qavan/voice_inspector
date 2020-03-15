package com.sac.speechdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.user.speechrecognizationasservice.R;
import com.sac.speechdemo.util.Util;


public class CardActivity extends Activity implements Button.OnClickListener {
    private static final String TAG = "CARD_ACTIVITY";

    private Task mTask;
    private TaskDao mTaskDao;
    private Task mCurrentTaskInCard;
    private TextView mETClient;
    private TextView mETClientId;
    private TextView mETAddress;
    private TextView mTVPrevDate;
    private TextView mTVCurrentDate;
    private TextView mTVNewDate;
    private TextView mTVPrevValue;
    private TextView mTVCurrentValue;
    private EditText mTVNewValue;
    private Long mId;
    private String mAddress;
    private String mClient;
    private String mClientId;
    private String mPrevDate;
    private String mCurrentDate;
    private String mNewDate;
    private String mPrevValue;
    private String mCurrentValue;
    private String mNewValue = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        DaoSession daoSession = ((App) getApplication()).getDaoSession();

        mTaskDao = daoSession.getTaskDao();

        mETClientId = findViewById(R.id.idClientIdEditText);
        mETClient = findViewById(R.id.idClientEditText);
        mETAddress = findViewById(R.id.idAddressEditText);
        mTVPrevDate = findViewById(R.id.idPrevDateTextView);
        mTVPrevValue = findViewById(R.id.idPrevValueEditText);
        mTVCurrentDate = findViewById(R.id.idСurrentDateTextView);
        mTVCurrentValue = findViewById(R.id.idCurrentValueEditText);
        mTVNewDate = findViewById(R.id.idNewDateTextView);
        mTVNewValue = findViewById(R.id.idNewValueEditText);

        Button bWrite = findViewById(R.id.button);
        bWrite.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mId = getIntent().getExtras().getLong("id");
        mCurrentTaskInCard = mTaskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(mId)).build().list().get(0);
        mAddress = mCurrentTaskInCard.getC_address();
        mClient = mCurrentTaskInCard.getC_client();
        mClientId = mCurrentTaskInCard.getC_client_id();
        mPrevDate = Util.dateCombine(mCurrentTaskInCard.getD_prev_date());
        mCurrentDate = Util.dateCombine(mCurrentTaskInCard.getD_current_date());
        mNewDate = Util.dateCombine(Util.getCurrentDate().toString());
        mPrevValue = mCurrentTaskInCard.getN_prev_value();
        mCurrentValue = mCurrentTaskInCard.getN_current_value();

        mETClientId.setText(mClientId);
        mETClient.setText(mClient);
        mETAddress.setText(mAddress);

        mTVPrevValue.setText(mPrevValue);
        mTVCurrentValue.setText(mCurrentValue);
        mTVNewValue.setText(mNewValue);
        mTVPrevDate.setText(String.format("Предыдущее от %s", mPrevDate));
        mTVCurrentDate.setText(String.format("Текущее от %s", mCurrentDate));
        mTVNewDate.setText(String.format("Новое от %s", mNewDate));
    }

    @Override
    public void onClick(View v) {
        Long newValue = (long) Float.parseFloat(mTVNewValue.getText().toString());
        if (newValue != null) {
            mTask = mTaskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(mId)).build().list().get(0);

            mTask.setD_prev_date(Util.dateCombine(mCurrentTaskInCard.getD_current_date()));
            mTask.setD_current_date(Util.dateCombine(Util.getCurrentDate().toString()));
            mTask.setN_prev_value(mCurrentValue);
            mTask.setN_current_value(newValue.toString());
            mTask.update();
            Log.i(TAG, "Task updated locally" + mId);
            Toast.makeText(this, "Показания записаны", Toast.LENGTH_SHORT).show();
            this.finish();
        } else {
            Toast.makeText(this, "Введите правильные показания!", Toast.LENGTH_SHORT).show();
        }
    }
}

