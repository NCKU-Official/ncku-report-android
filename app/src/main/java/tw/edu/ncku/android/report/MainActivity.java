package tw.edu.ncku.android.report;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tw.edu.ncku.android.report.client.NckuReportService;
import tw.edu.ncku.android.report.client.model.Category;
import tw.edu.ncku.android.report.client.model.CategoryResponse;
import tw.edu.ncku.android.report.storage.NckuReportStorage;

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private NckuReportService mNckuReportService;
    private NckuReportStorage mNckuReportStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNckuReportService = new NckuReportService();
        mNckuReportStorage = NckuReportStorage.getInstance(getApplicationContext());

        final Observable<CategoryResponse> fetchDataObservable = mNckuReportService.fetchCategories();
        fetchDataObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CategoryResponse>() {

                    @Override
                    public void onCompleted() {

                        NavMenuManager.setupMenu(MainActivity.this,
                                mNckuReportStorage, navigationView);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CategoryResponse categoryResponse) {
                        mNckuReportStorage.saveCategory(categoryResponse);
                    }
                });

        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        NavMenuManager.setupMenu(this, mNckuReportStorage, navigationView);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();
                return false;
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

        String intentAction = intent.getAction();
        if (intentAction.compareTo(Constants.INTENT_NAV_ITEM_SELECTED) == 0) {

            String category_json = intent.getStringExtra(Constants.INTENT_EXTRA_CATEGORY);
            Gson gson = new Gson();
            Category category = gson.fromJson(category_json, Category.class);

            // set url for webview
            ContentFragment fragment = new ContentFragment();
            Bundle b = new Bundle();
            b.putString(Constants.INTENT_EXTRA_CATEGORY_VIEW_URL, category.getAction().getView());
            fragment.setArguments(b);

            // do fragment transaction
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.commit();

            // set title
            toolbar.setSubtitle(category.getName());

            // add action menu
            toolbar.getMenu().clear();
            toolbar.getMenu().add("test");

            MenuItem mi = toolbar.getMenu().add(category.getName());
            Intent i = new Intent();
            i.setClass(this, MainActivity.class);
            i.setAction(Constants.INTENT_ACTION_MENU_CREATE);
            i.putExtra(Constants.INTENT_EXTRA_CATEGORY, gson.toJson(category));
            mi.setIntent(i);
            mi.setIcon(R.drawable.ic_action_content_add);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else if (intentAction.compareTo(Constants.INTENT_ACTION_MENU_CREATE) == 0) {

            String category_json = intent.getStringExtra(Constants.INTENT_EXTRA_CATEGORY);
            Gson gson = new Gson();
            Category category = gson.fromJson(category_json, Category.class);

            // set url for webview
            ContentFragment fragment = new ContentFragment();
            Bundle b = new Bundle();
            b.putString(Constants.INTENT_EXTRA_CATEGORY_VIEW_URL, category.getAction().getCreate());
            fragment.setArguments(b);

            // do fragment transaction
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("Main optional menu", String.valueOf(id));
        Log.d("Main optional menu", (String) item.getTitle());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
