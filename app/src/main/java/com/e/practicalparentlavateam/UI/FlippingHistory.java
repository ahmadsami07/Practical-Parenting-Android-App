/*This is the flipping history which show the history of the flipping*/

package com.e.practicalparentlavateam.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.e.practicalparentlavateam.Model.ChildrenManager;
import com.e.practicalparentlavateam.R;
import com.e.practicalparentlavateam.Model.HistoryItem;
import com.e.practicalparentlavateam.Model.HistoryManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FlippingHistory extends AppCompatActivity {
    private HistoryManager historyManager;
    private ArrayAdapter<HistoryItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flipping_history);

        populateList();
    }

    private void populateList() {
        historyManager = HistoryManager.getInstance();
        adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.history_list);
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<HistoryItem>{
        public MyListAdapter() {super(FlippingHistory.this,
                R.layout.history_list, historyManager.getList());}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.history_list, parent, false);
            }

            HistoryItem currentItem = historyManager.getList().get(position);

            TextView timeView = (TextView) itemView.findViewById(R.id.item_time);
            timeView.setText(currentItem.getTime());

            TextView choiceView = (TextView) itemView.findViewById(R.id.item_num);
            choiceView.setText(currentItem.getChoise());

            TextView nameView = (TextView) itemView.findViewById(R.id.item_name);
            nameView.setText(currentItem.getName());

            ImageView result = (ImageView) itemView.findViewById(R.id.win_or_false);
            result.setImageResource(currentItem.getId());

            ImageView coin = (ImageView) itemView.findViewById(R.id.coin_result);
            coin.setImageResource(currentItem.getCoinIcon());

            ChildrenManager children = ChildrenManager.getInstance();

            if(!currentItem.getName().equals(getString(R.string.nobody))) {
                ImageView imageView = (ImageView) itemView.findViewById(R.id.history_image);
                try {
                    File file = new File(children.getPath(), currentItem.getName() + ".jpg");
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            return itemView;
        }
    }

    public static Intent makeLaunch(Context context) {
        return new Intent(context, FlippingHistory.class);
    }
}