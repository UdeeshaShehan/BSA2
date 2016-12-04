package com.example.usid.mpos;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.usid.mpos.UI.InventoryFragment;
import com.example.usid.mpos.UI.MobilePaymentDialogFragment;
import com.example.usid.mpos.UI.ProductDetailActivity;
import com.example.usid.mpos.UI.ReportFragment;
import com.example.usid.mpos.UI.SaleFragment;
import com.example.usid.mpos.UI.UpdatableFragment;
import com.example.usid.mpos.domain.LanguageController;
import com.example.usid.mpos.domain.inventory.Inventory;
import com.example.usid.mpos.domain.inventory.Product;
import com.example.usid.mpos.domain.inventory.ProductCatalog;
import com.example.usid.mpos.technicalService.NoDaoSetException;

import java.util.Locale;

public class MainActivity extends FragmentActivity {

    private ViewPager viewPager;
    private ProductCatalog productCatalog;
    private String productId;
    private Product product;
    private static boolean SDK_SUPPORTED;
    private PagerAdapter pagerAdapter;
    private Resources res;

    @SuppressLint("NewApi")
    /**
     * Initiate this UI.
     */
    private void initiateActionBar() {
        if (SDK_SUPPORTED) {
            ActionBar actionBar = getActionBar();

            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                }
            };
            actionBar.addTab(actionBar.newTab().setText(res.getString(R.string.inventory))
                    .setTabListener(tabListener), 0, false);
            actionBar.addTab(actionBar.newTab().setText(res.getString(R.string.sale))
                    .setTabListener(tabListener), 1, true);
            actionBar.addTab(actionBar.newTab().setText(res.getString(R.string.payment))
                    .setTabListener(tabListener), 2, false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color
                        .parseColor("#73bde5")));
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        res = getResources();
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        super.onCreate(savedInstanceState);
        SDK_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
        initiateActionBar();
        FragmentManager fragmentManager = getSupportFragmentManager();
        pagerAdapter = new PagerAdapter(fragmentManager, res);
        viewPager.setAdapter(pagerAdapter);
        viewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        if (SDK_SUPPORTED)
                            getActionBar().setSelectedNavigationItem(position);
                    }
                });
        viewPager.setCurrentItem(1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            openQuitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Open quit dialog.
     */
    private void openQuitDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle(res.getString(R.string.dialog_quit));
        quitDialog.setPositiveButton(res.getString(R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    /**
     * Option on-click handler.
     * @param view
     */
    public void optionOnClickHandler(View view) {
        viewPager.setCurrentItem(0);
        String id = view.getTag().toString();
        productId = id;
        try {
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        product = productCatalog.getProductById(Integer.parseInt(productId));
        openDetailDialog();

    }

    /**
     * Open detail dialog.
     */
    private void openDetailDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(MainActivity.this);
        quitDialog.setTitle(product.getName());
        quitDialog.setPositiveButton(res.getString(R.string.remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                openRemoveDialog();
            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.product_detail), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
               Intent newActivity = new Intent(MainActivity.this,
                        ProductDetailActivity.class);
                newActivity.putExtra("id", productId);
                startActivity(newActivity);
            }
        });

        quitDialog.show();
    }

    /**
     * Open remove dialog.
     */
    private void openRemoveDialog() {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                MainActivity.this);
        quitDialog.setTitle(res.getString(R.string.dialog_remove_product));
        quitDialog.setPositiveButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        quitDialog.setNegativeButton(res.getString(R.string.remove), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                productCatalog.suspendProduct(product);
                pagerAdapter.update(0);
            }
        });

        quitDialog.show();
    }

    /**
     * Get view-pager
     * @return
     */
    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.swipe:
                /*setLanguage("en");*/
                swipeardpayment();
                return true;
            case R.id.chip:
                /*setLanguage("sin");*/
                chipecardpayment();
                return true;
            case R.id.NFC:
                /*setLanguage("tam");*/
                nfcpayment();
                return true;
            case R.id.mobile:
                /*setLanguage("tam");*/
                mobilepayment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
 /*   public void showPopup() {
        MobilePaymentDialogFragment newFragment = new MobilePaymentDialogFragment();
        newFragment.show(getFragmentManager(), "");
    }*/
    public void mobilepayment(){
      //  showPopup();
        MobilePaymentDialogFragment newFragment = new MobilePaymentDialogFragment();
        newFragment.show(getSupportFragmentManager(), "");

    }
    public void nfcpayment(){

    }
    public void swipeardpayment(){

    }
    public void chipecardpayment(){

    }

    /**
     * Set language
     * @param localeString
     */
    private void setLanguage(String localeString) {
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        LanguageController.getInstance().setLanguage(localeString);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
class PagerAdapter extends FragmentStatePagerAdapter {

    private UpdatableFragment[] fragments;
    private String[] fragmentNames;

    /**
     * Construct a new PagerAdapter.
     * @param fragmentManager
     * @param res
     */
    public PagerAdapter(FragmentManager fragmentManager, Resources res) {

        super(fragmentManager);

        UpdatableFragment reportFragment = new ReportFragment();
        UpdatableFragment saleFragment = new SaleFragment(reportFragment);
        UpdatableFragment inventoryFragment = new InventoryFragment(
                saleFragment);

        fragments = new UpdatableFragment[] { inventoryFragment, saleFragment,
                reportFragment };
        fragmentNames = new String[] { res.getString(R.string.inventory),
                res.getString(R.string.sale),
                res.getString(R.string.payment) };

    }

    @Override
    public Fragment getItem(int i) {
        return fragments[i];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentNames[i];
    }

    /**
     * Update
     * @param index
     */
    public void update(int index) {
        fragments[index].update();
    }


}
