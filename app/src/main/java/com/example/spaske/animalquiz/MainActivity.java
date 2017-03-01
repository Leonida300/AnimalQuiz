package com.example.spaske.animalquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    //KEYs - from quiz_performances.xml
    public static final String GUESSES = "settings_numberOfGuesses";
    public static final String ANIMALS_TYPE = "settings_animalsType";
    public static final String QUIZ_BACKGROUND_COLOR = "settings_quiz_background_color";
    public static final String QUIZ_FONT = "settings_quiz_font";

    //Da li je bilokakva promena izvrsena u 'settings'
    private boolean isSettingsChanged = false;

    //Font
    static Typeface chunkfive;
    static Typeface fontleroybrown;
    static Typeface wonderbarDemo;


    //Kreiramo referencu na MainActivityFragment
    MainActivityFragment myAnimalQuizFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Preuzimanje fonta iz 'assets' foldera
        chunkfive = Typeface.createFromAsset(getAssets(), "fonts/Chunkfive.otf");
        fontleroybrown = Typeface.createFromAsset(getAssets(), "fonts/FontleroyBrown.ttf");
        wonderbarDemo = Typeface.createFromAsset(getAssets(), "fonts/Wonderbar Demo.otf");

        //Postavlanje dafault-nih vrednosti kad se apk pokrene prvi put...(sve vrednosti koje se nalaze u settings-u iz quiz_preferences.xml fajla)
        //FALSE, se postavlja bas iz razloga da se default-ne vrednosti postave samo pri prvom pokretanju apk, dok bi TRYE to radio svaki put
        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.quiz_preferences, false);

        //Svaka promena koju korisnik unese u 'settings' cuva se u SharedPreferences
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                //pozivamo listener za promene u setting-su
                registerOnSharedPreferenceChangeListener(settingsChangedListener);


        //id.animalQuizFragment je id content_main.xml koji u sebi sadrzi fragment_main.xml
        myAnimalQuizFragment = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.animalQuizFragment);


        //cuvanje promena preko SharedPreferences prilikom iskljucivanja apk i ponovnog pokretanja apk
        myAnimalQuizFragment.modifyAnimalsGuessRows(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyTypeOfAnimalsInQuiz(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyQuizFont(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.modifyBackgroundColor(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        myAnimalQuizFragment.resetAnimalQuiz();
        isSettingsChanged = false;


    }

    //postavlja itemene u settings
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent preferencesIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);

    }


    //kreiramo listener za cuvanje promena...ovo je zapravo anonimus inner klasa
    private SharedPreferences.OnSharedPreferenceChangeListener settingsChangedListener
            = new SharedPreferences.OnSharedPreferenceChangeListener(){

        //Ova metoda se poziva svaki put kada korisnik izvrsi promenu u settings-u
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            isSettingsChanged = true;

            if (key.equals(GUESSES)){

                myAnimalQuizFragment.modifyAnimalsGuessRows(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();

            }else if (key.equals(ANIMALS_TYPE)){

                //iz sharedPreferences izvlacimo ANIMAL_TYPE preko metode getStringSet i smestamo u promenljivu animalTypes tipa Set<String>
                Set<String> animalTypes = sharedPreferences.getStringSet(ANIMALS_TYPE, null);

                if (animalTypes != null && animalTypes.size() > 0){

                    myAnimalQuizFragment.modifyTypeOfAnimalsInQuiz(sharedPreferences);
                    myAnimalQuizFragment.resetAnimalQuiz();

                }else {//u slucaju da nije cekiran nijedan tip zivotinja
                    //u tom slucaju ispisujemo Toast poruku i postavljamo default vrednost za tip zivotinje

                    //kreiramo editor kako bi postavili defaultValue za tip zivotinje u slucaju da nijedan tip nije cekiran
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    animalTypes.add(getString(R.string.default_animal_type)); //postavljanje default tip zivotinje
                    editor.putStringSet(ANIMALS_TYPE, animalTypes);
                    editor.apply();

                    Toast.makeText(MainActivity.this, R.string.toast_message, Toast.LENGTH_SHORT).show();
                }
            }else if (key.equals(QUIZ_FONT)){

                myAnimalQuizFragment.modifyQuizFont(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();

            }else if (key.equals(QUIZ_BACKGROUND_COLOR)){

                myAnimalQuizFragment.modifyBackgroundColor(sharedPreferences);
                myAnimalQuizFragment.resetAnimalQuiz();

            }

            Toast.makeText(MainActivity.this, R.string.change_message, Toast.LENGTH_SHORT).show();
        }
    };
}
