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
    private TextView mTVPrevValue;
    private EditText mTVCurrentValue;
    private Long mId;
    private String mAddress;
    private String mClient;
    private String mClientId;
    private String mPrevDate;
    private String mCurrentDate;
    private String mPrevValue;
    private String mCurrentValue;

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
//        try {
//            mPrevDate = Utils.getFormattedDateDDMMYY(Utils.getJavaMainFormattedDate(mCurrentTaskInCard.getD_prev_date()));
//        }
//        catch (NullPointerException e) {
//            mPrevDate = null;
//        }
//        try {
//            mCurrentDate = Utils.getFormattedDateDDMMYY(Utils.getJavaMainFormattedDate(mCurrentTaskInCard.getD_current_date()));
//        }
//        catch (NullPointerException e) {
//            mCurrentDate = null;
//        }
        mPrevValue = mCurrentTaskInCard.getN_prev_value();
        mCurrentValue = mCurrentTaskInCard.getN_current_value();

        if (mPrevDate == null) {
            Toast.makeText(this, "Даты отсутствуют mPrevDate", Toast.LENGTH_LONG).show();
        } else if (mCurrentDate == null) {
            Toast.makeText(this, "Даты отсутствуют mCurrentDate", Toast.LENGTH_LONG).show();
        }

        mETClientId.setText(mClientId);
        mETClient.setText(mClient);
        mETAddress.setText(mAddress);
        mTVPrevDate.setText(String.format("Предыдущее от %s", mPrevDate));
        mTVCurrentDate.setText(String.format("   Текущее от %s", mCurrentDate));


        if (!mPrevValue.equals("null"))
            mTVPrevValue.setText(mPrevValue);
        else
            mTVPrevValue.setText("0");
        if (!mCurrentValue.equals("null"))
            mTVCurrentValue.setText(mCurrentValue);
        else
            mTVCurrentValue.setText("");
    }

    @Override
    public void onClick(View v) {
        Long newValue = Long.parseLong(mTVCurrentValue.getText().toString());
        if (newValue != null) {
            mTask = mTaskDao.queryBuilder().where(TaskDao.Properties.TaskId.eq(mId)).build().list().get(0);

//            mTask.setD_prev_date(mCurrentTaskInCard.getD_current_date());
//            mTask.setD_current_date(Utils.getFormattedDateDDMMYY(Utils.getCurrentDate()));

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

