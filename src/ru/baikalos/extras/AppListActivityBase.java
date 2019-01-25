package ru.baikalos.extras;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ru.baikalos.extras.R;

public abstract class AppListActivityBase extends BaseActivity {

    AppChooserAdapter dAdapter = null;
    ProgressBar dProgressBar  = null;
    ListView dListView  = null;
    EditText dSearch = null;
    ImageButton dButton = null;

    private int mId;

    public AppListActivityBase() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.app_list_activity);

        dListView = (ListView) findViewById(R.id.app_list_view);
        dSearch = (EditText) findViewById(R.id.app_search_text);
        dButton = (ImageButton) findViewById(R.id.app_search_button);
        dProgressBar = (ProgressBar) findViewById(R.id.app_list_progress_bar);

        dAdapter = new AppChooserAdapter(this) {
            @Override
            public void onStartUpdate() {
                dProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinishUpdate() {
                dProgressBar.setVisibility(View.GONE);
            }
        };

        dListView.setAdapter(dAdapter);
        dListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                AppChooserAdapter.AppItem info = (AppChooserAdapter.AppItem) av
                        .getItemAtPosition(pos);
                onListViewItemClick(info, mId);

            }
        });

        dButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dAdapter.getFilter().filter(dSearch.getText().toString(), new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        dAdapter.update();
                    }
                });
            }
        });

        dSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    dAdapter.getFilter().filter(dSearch.getText().toString(), new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            dAdapter.update();
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        dAdapter.update();
    }

    public void setLauncherFilter(boolean enabled) {
        dAdapter.setLauncherFilter(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract void onListViewItemClick(AppChooserAdapter.AppItem info, int id);

}
