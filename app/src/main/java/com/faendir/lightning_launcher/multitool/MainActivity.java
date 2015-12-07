package com.faendir.lightning_launcher.multitool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.faendir.lightning_launcher.scriptlib.ScriptManager;


public class MainActivity extends AppCompatActivity {

    private TextView nameTextView;
    private SharedPreferences shareprefs;

    private Button importButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //populate
        setContentView(R.layout.activity_main);



        checkLauncher();
        checkImporter();

        //prepare variables
        nameTextView = (TextView) findViewById(R.id.main_scriptName);
        importButton = (Button) findViewById(R.id.import_button);
        shareprefs = PreferenceManager.getDefaultSharedPreferences(this);


        //set name text
        nameTextView.setText(
                shareprefs.getString(
                        getString(R.string.preference_scriptName),
                        getString(R.string.script_name)
                )
        );


    }

    //checks if the importer app is installed
    private boolean checkImporter() {
        if(isPackageInstalled("com.trianguloy.llscript.repository", this)){
            findViewById (R.id.view_repositoryImporter).setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    //checks if lightning launcher is installed and shows the alert view
    private void checkLauncher() {
        if (!isPackageInstalled("net.pierrox.lightning_launcher_extreme", this)
                &&
                !isPackageInstalled("net.pierrox.lightning_launcher", this)
                ) {
            findViewById (R.id.view_noLauncher).setVisibility(View.VISIBLE);
            findViewById(R.id.view_yesLauncher).setVisibility(View.GONE);
                }
    }

    @Override
    public void onPause(){
        super.onPause();
        saveName();
    }

    private void saveName() {
        //save the name preference
        shareprefs.edit().putString(getString(R.string.preference_scriptName),nameTextView.getText().toString()).apply();
    }





    //onClick button
    public void openHelpPage(View view){
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    //onClick button
    public void showScriptCode(View view){
        Intent intent = new Intent(this, Code.class);
        startActivity(intent);
    }

    //onClick button
    public void openPlayStore(View view){
        final String appPackageName = "net.pierrox.lightning_launcher";
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    //onClick button
    public void openLink(View view){

        Intent intent=null;

        switch (view.getId()){
            case R.id.wiki:
                intent=new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_wiki)));
                break;
            case R.id.googleplus:
                intent=new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_googlePlus)));
                break;
            case R.id.email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        getString(R.string.link_email_scheme), getString(R.string.link_email_adress), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.link_email_subject));
                intent= Intent.createChooser(emailIntent, getString(R.string.link_email_chooser));
                break;
        }

        if (intent != null) {
            startActivity(intent);
        } else {
            Log.d("Error", "no intent");
        }
    }

    //onClick button
    public void importFromApp(View view){
        saveName();
        changeText(getString(R.string.button_repositoryImporter_importing));

        //Lukas API
        ScriptManager.loadScript(
                this,
                Utils.getStringFromResource(R.raw.multitool, this, getString(R.string.error_noResourceFound)),
                nameTextView.getText().toString(),
                Constants.FLAG_APP_MENU + Constants.FLAG_ITEM_MENU,
                new managerListener()
        );
    }






    private boolean isPackageInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void changeText(String newText){
        importButton.setText(newText);
    }








    private class managerListener extends ScriptManager.Listener{
        @Override
        public void OnLoadFinished(int i) {
            changeText(getString(R.string.button_repositoryImporter_importOk));
        }

        @Override
        public void OnError() {
            changeText(getString(R.string.button_repositoryImporter_importError));
        }
    }


}
