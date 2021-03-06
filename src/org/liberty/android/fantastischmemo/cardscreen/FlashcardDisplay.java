/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
package org.liberty.android.fantastischmemo.cardscreen;

import org.liberty.android.fantastischmemo.*;

import org.amr.arabic.ArabicUtilities;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.net.URL;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.content.Context;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.ClipboardManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.gesture.GestureOverlayView;
import android.widget.Button;
import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.os.SystemClock;
import android.os.Environment;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;

import android.text.Html.TagHandler;
import android.text.Html.ImageGetter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;

public class FlashcardDisplay implements TagHandler, ImageGetter{
    private final static String TAG = "org.liberty.android.fantastischmemo.cardscreen.FlashcardDisplay";
    private Context mContext;
    private View flashcardView;
    private LinearLayout questionLayout;
    private LinearLayout answerLayout;
    private LinearLayout separator;
    private TextView questionView;
    private TextView answerView;
    private SettingManager settingManager;
    private boolean answerShown = true;


    public FlashcardDisplay(Context context){
        this(context, new SettingManager(context));
    }

    public FlashcardDisplay(Context context, SettingManager manager){
        mContext = context;
        settingManager = manager;
        LayoutInflater factory = LayoutInflater.from(mContext);
        flashcardView= factory.inflate(R.layout.flashcard_screen, null);
        questionLayout = (LinearLayout)flashcardView.findViewById(R.id.layout_question);
        answerLayout = (LinearLayout)flashcardView.findViewById(R.id.layout_answer);
        questionView = (TextView)flashcardView.findViewById(R.id.question);
        answerView = (TextView)flashcardView.findViewById(R.id.answer);
        separator = (LinearLayout)flashcardView.findViewById(R.id.horizontalLine);
    }


    public View getView(){
        return flashcardView;
    }

    public View getQuestionView(){
        return questionView;
    }
    
    public View getAnswerView(){
        return answerView;
    }

    /* Update view with answer shown */
    public void updateView(Item item){
        updateView(item, true);
    }

	public void updateView(Item item, boolean showAnswer) {
        if(item == null){
            return;
        }
        answerShown = showAnswer;
        float qaRatio = settingManager.getQARatio();
        List<Integer> colors = settingManager.getColors();
        setQARatio(qaRatio);
        setScreenColor(colors);
        displayQA(item);
        if(showAnswer == false){
            answerView.setText(R.string.memo_show_answer);
        }
	}

    public void setQuestionLayoutClickListener(View.OnClickListener l){
        questionLayout.setOnClickListener(l);
    }

    public void setAnswerLayoutClickListener(View.OnClickListener l){
        answerLayout.setOnClickListener(l);
    }

    public void setQuestionTextClickListener(View.OnClickListener l){
        questionView.setOnClickListener(l);
    }

    public void setAnswerTextClickListener(View.OnClickListener l){
        answerView.setOnClickListener(l);
    }

    public void setQuestionLayoutLongClickListener(View.OnLongClickListener l){
        questionLayout.setOnLongClickListener(l);
        questionView.setOnLongClickListener(l);
    }

    public void setAnswerLayoutLongClickListener(View.OnLongClickListener l){
        answerLayout.setOnLongClickListener(l);
        answerView.setOnLongClickListener(l);
    }

    public void setScreenOnTouchListener(View.OnTouchListener l){
        flashcardView.setOnTouchListener(l);
        questionView.setOnTouchListener(l);
        answerView.setOnTouchListener(l);
    }

    public boolean isAnswerShown(){
        return answerShown;
    }



	protected void displayQA(Item item) {
		/* Display question and answer according to item */
        
        String questionTypeface = settingManager.getQuestionTypeface();
        String answerTypeface = settingManager.getAnswerTypeface();
        boolean enableThirdPartyArabic = settingManager.getEnableThirdPartyArabic();
        SettingManager.Alignment questionAlign = settingManager.getQuestionAlign();
        SettingManager.Alignment answerAlign = settingManager.getAnswerAlign();
        SettingManager.HTMLDisplayType htmlDisplay = settingManager.getHtmlDisplay();

        /* Set the typeface of question an d answer */
        if(questionTypeface != null && !questionTypeface.equals("")){
            try{
                Typeface qt = Typeface.createFromFile(questionTypeface);
                questionView.setTypeface(qt);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting question font", e);
            }

        }
        if(answerTypeface != null && !answerTypeface.equals("")){
            try{
                Typeface at = Typeface.createFromFile(answerTypeface);
                answerView.setTypeface(at);
            }
            catch(Exception e){
                Log.e(TAG, "Typeface error when setting answer font", e);
            }

        }

        String sq, sa;
        if(enableThirdPartyArabic){
            sq = ArabicUtilities.reshape(item.getQuestion());
            sa = ArabicUtilities.reshape(item.getAnswer());
        }
        else{
            sq = item.getQuestion();
            sa = item.getAnswer();
        }
		
		
        /* Use HTML to display */
		if(htmlDisplay == SettingManager.HTMLDisplayType.QUESTION){
            questionView.setText(Html.fromHtml(sq, this, this));
            answerView.setText(sa);
		}
		else if(htmlDisplay == SettingManager.HTMLDisplayType.ANSWER){
            answerView.setText(Html.fromHtml(sa, this, this));
            questionView.setText(sq);
		}
		else if(htmlDisplay == SettingManager.HTMLDisplayType.NONE){
            questionView.setText(sq);
            answerView.setText(sa);
		}
		else{
            /* Both */
            questionView.setText(Html.fromHtml(sq, this, this));
            answerView.setText(Html.fromHtml(sa, this, this));
		}
		
        /* Here is tricky to set up the alignment of the text */
		if(questionAlign == SettingManager.Alignment.CENTER){
			questionView.setGravity(Gravity.CENTER);
			questionLayout.setGravity(Gravity.CENTER);
		}
		else if(questionAlign == SettingManager.Alignment.RIGHT){
			questionView.setGravity(Gravity.RIGHT);
			questionLayout.setGravity(Gravity.NO_GRAVITY);
		}
		else{
			questionView.setGravity(Gravity.LEFT);
			questionLayout.setGravity(Gravity.NO_GRAVITY);
		}
		if(answerAlign == SettingManager.Alignment.CENTER){
			answerView.setGravity(Gravity.CENTER);
			answerLayout.setGravity(Gravity.CENTER);
		} 
        else if(answerAlign == SettingManager.Alignment.RIGHT){
			answerView.setGravity(Gravity.RIGHT);
			answerLayout.setGravity(Gravity.NO_GRAVITY);
			
		}
		else{
			answerView.setGravity(Gravity.LEFT);
			answerLayout.setGravity(Gravity.NO_GRAVITY);
		}
		questionView.setTextSize(settingManager.getQuestionFontSize());
		answerView.setTextSize(settingManager.getAnswerFontSize());
	}


    
    private void setScreenColor(List<Integer> colors){
        /* Set both text and the background color */
        if(colors != null){
            questionView.setTextColor(colors.get(0));
            answerView.setTextColor(colors.get(1));
            questionLayout.setBackgroundColor(colors.get(2));
            answerLayout.setBackgroundColor(colors.get(3));
            separator.setBackgroundColor(colors.get(4));
        }
    }

    private void setQARatio(float qRatio){

        if(qRatio > 99.0f){
            answerLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else if(qRatio < 1.0f){
            questionLayout.setVisibility(View.GONE);
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
        }
        else{

            float aRatio = 100.0f - qRatio;
            qRatio /= 50.0;
            aRatio /= 50.0;
            questionLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, qRatio));
            answerLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, aRatio));
        }
    }

    @Override
    public Drawable getDrawable(String source){
        Log.v(TAG, "Source: " + source);
        try{
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.default_image_dir) + "/" + settingManager.getDbName()+ "/" + source;
            String filePath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + mContext.getString(R.string.default_image_dir) + "/" + source;
            Bitmap orngBitmap;
            /* Try the image in /sdcard/anymemo/images/dbname/myimg.png */
            if((new File(filePath)).exists()){
                orngBitmap = BitmapFactory.decodeFile(filePath);
            }
            /* Try the image in /sdcard/anymemo/images/myimg.png */
            else if((new File(filePath2)).exists()){
                orngBitmap = BitmapFactory.decodeFile(filePath2);
            }
            /* Try the image from internet */
            else{
                InputStream is = (InputStream)new URL(source).getContent();
                orngBitmap = BitmapFactory.decodeStream(is);
            }

            int width = orngBitmap.getWidth();
            int height = orngBitmap.getHeight();
            int scaledWidth = width;
            int scaledHeight = height;
            float scaleFactor = ((float)settingManager.getScreenWidth()) / width;
            Matrix matrix = new Matrix();
            if(scaleFactor < 1.0f){
                matrix.postScale(scaleFactor, scaleFactor);
                scaledWidth = (int)(width * scaleFactor);
                scaledHeight = (int)(height * scaleFactor);
            }
            Bitmap resizedBitmap = Bitmap.createBitmap(orngBitmap, 0, 0, width, height, matrix, true);
            BitmapDrawable d = new BitmapDrawable(resizedBitmap);
            //d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            d.setBounds(0, 0, scaledWidth, scaledHeight);
            return d; 
        }
        catch(Exception e){
            Log.e(TAG, "getDrawable() Image handling error", e);
        }

        /* Fallback, display default image */
        Drawable d = mContext.getResources().getDrawable(R.drawable.picture);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        return d;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader){
        return;
    }
}
