package com.example.spaske.animalquiz;

import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
//Vrsimo promenu, umesto Fragment....nasledjujemo PreferenceFragment koja nasledjuje Fragment
public class SettingsActivityFragment extends PreferenceFragment {

    //brisemo automatski kreirane metode i rucno postavljamo svoje


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //dodajemo funkcionalnost iz fajla quiz_preferences.xml u fragment_settings.xml
        addPreferencesFromResource(R.xml.quiz_preferences);
    }
}
