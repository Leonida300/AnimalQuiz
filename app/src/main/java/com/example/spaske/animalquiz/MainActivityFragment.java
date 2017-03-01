package com.example.spaske.animalquiz;

import android.animation.Animator;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.example.spaske.animalquiz.MainActivity.GUESSES;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {


    private static final int NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ = 10;

    private List<String> allAnimalsNamesList; //Lista svih imena zivotinja koje sadrzi apk
    private List<String> animalsNamesQuizList; //list imena zivotinja koje ce biti emitovane u jednoj igri (u ovom slucaju 10)
    private Set<String> animalTypesInQuiz; //interfejs koji ne dozvoljava dupliranje vrednosti
    private String correctAnimalAnswer;
    private int numberOfAllGuesses;
    private int numberOfRightAnswers;
    private int numberOfAnimalsGuessRows;
    private SecureRandom secureRandomNumber;
    private Handler handler; //ako je odgovor tacan prosledjuje novo pitanje
    private Animation wrongAnswerAnimation;

    private LinearLayout animalQuizLinearLayout; //menjanje boje LinearLayouta - backgroundColor (id za 'fragment_main.xml)
    private TextView txtQuestionNumber; // redni broj pitanja
    private ImageView imgAnimal; //slika koja se prikazuje
    private LinearLayout[] rowOfGuessButtonsInAnimalQuiz; //postavlja koliko ce redova LinearLayouta biti...1 red 2 horizontalna buttona....buttons(2, 4 ili 6)
    private TextView txtAnswer; //ispisuje da li je odgovor tacan ili netacan


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //CONAINER zapravo predstavlja 'content_main' layaout, jer on u sebi sadrzi fragment
        //vracamo tip View
        View view = inflater.inflate(R.layout.fragment_main, container, false);//true-false se postavlja u zavisnosti da li zelimo da pricvrstimo-povezemo 'fragment_main' sa 'conteiner', ali posto su vec povezani stavljamo false


        allAnimalsNamesList = new ArrayList<>(); //kreiramo praznu listu
        animalsNamesQuizList = new ArrayList<>(); //kreiramo praznu listu
        secureRandomNumber = new SecureRandom(); //Random postavljanje slika iz liste
        handler = new Handler();


        //Ucitavamo animaciju iz anim foldera
        wrongAnswerAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.wrong_answer_animation);
        //broj ponavljanja animacije
        wrongAnswerAnimation.setRepeatCount(1);

//koristimo referencu view...kako bi odredili da se odnosi na 'fragment_main.xml' iz ove metode-klase
        animalQuizLinearLayout = (LinearLayout) view.findViewById(R.id.animalQuizLinearLayout);
        txtQuestionNumber = (TextView) view.findViewById(R.id.txtQuestionNumber);
        imgAnimal = (ImageView) view.findViewById(R.id.imgAnimal);

        rowOfGuessButtonsInAnimalQuiz = new LinearLayout[3]; //broj redova LinearLayouta
        rowOfGuessButtonsInAnimalQuiz[0] = (LinearLayout) view.findViewById(R.id.firstRowLinearLayout);
        rowOfGuessButtonsInAnimalQuiz[1] = (LinearLayout) view.findViewById(R.id.secondRowLinearLayout);
        rowOfGuessButtonsInAnimalQuiz[2] = (LinearLayout) view.findViewById(R.id.thirdRowLinearLayout);

        txtAnswer = (TextView) view.findViewById(R.id.txtAnswer);


        for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz) {

            //getChildCount....vraca broj buttona koji se nalaze u jednom redu LinearLayout-a
            for (int column = 0; column < row.getChildCount(); column++) {

                //row.getChildAt(column) - udji unutar redaLinearLayouta i odredi koji button(child)...button1[0], button2[1]
                Button btnGuess = (Button) row.getChildAt(column);
                btnGuess.setOnClickListener(btnGuessListener);
                btnGuess.setTextSize(24);
            }
        }


        //(%1$d) prvi argument, pocetna vrednost.............,(%2$d) drugi argument 10, definisano konstantom
        txtQuestionNumber.setText(getString(R.string.question_text, 1, NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ));

        return view;
    }


    //kreiranje btnGuessListener
    private View.OnClickListener btnGuessListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btnGuess = ((Button) view);
            String guessValue = btnGuess.getText().toString();
            String answerValue = getTheExactAnimalName(correctAnimalAnswer); //dole se nalazi ova metoda - za tacan odgovor
            ++numberOfAllGuesses;

            if (guessValue.equals(answerValue)) {

                ++numberOfRightAnswers;

                txtAnswer.setText(answerValue + "!" + " RIGHT");

                //Ako se da tacan odgovor ostali buttoni ne mogu da se kliknu
                disableQuizGuessButtons();

                //Da li je igra gotova ili nije
                if (numberOfRightAnswers == NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ) {


                    new AlertDialog.Builder(getContext())
                            .setMessage(getString(R.string.results_string_value, numberOfAllGuesses,
                                    1000 / (double) numberOfAllGuesses))
                            .setPositiveButton(R.string.reset_animal_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    resetAnimalQuiz();
                                }
                            })
                            .setCancelable(false)
                            .show();


                } else {
                    //koliko vremena od tacnog odgovora do novog pitanja
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animateAnimalQuiz(true);
                        }
                    }, 1000); //1000 milliseconds for 1 second delay (vreme posle kog se izvrsava 'animateAnimalQuiz()' metoda
                }
            } else {
                //Ako je dat netacan odgovor izvrsava se ovo
                imgAnimal.startAnimation(wrongAnswerAnimation);

                txtAnswer.setText(R.string.wrong_answer_message);
                btnGuess.setEnabled(false); //ako si kliknuo na pogresan odgovor, vise ne mozes kliknuti na isti
            }
        }
    };


    private String getTheExactAnimalName(String animalName) {
        //Izvlacenje imena zivotinje iz slike....+1 - pokazuje na string koji sledi posle '-'
        return animalName.substring(animalName.indexOf('-') + 1).replace('_', ' ');
    }


    private void disableQuizGuessButtons() {

        //broj redova LinearLayout-a.....max 3 reda po 2 button-a
        for (int row = 0; row < numberOfAnimalsGuessRows; row++) {
            //ovde zapravo odredjujemo koliko ce redova LinearLayouta biti omoguceno u zavisnosti od promenljive row
            //rowOfGuessButtonsInAnimalQuiz[]; je gore vec odredjeno da moze da primi max 3 reda LinearLayouta
            //I ovu vrednost dodeljujemo promenljivoj 'guessRowLinearLayout' tipa LinearLayout kako bi rukovali button-ima unutar tih redova
            LinearLayout guessRowLinearLayout = rowOfGuessButtonsInAnimalQuiz[row];

            //guessRowLinearLayout.getChildCount() vraca broj button-a iz reda LinearLayouta
            //moguce je samo buttonIndex[0] i buttonIndex[1] jer postoje samo dva button-a unutar reda LinearLayout-a, a spoljnom FOR petljom odredjujemo o kom se tacno redu radi
            for (int buttonIndex = 0; buttonIndex < guessRowLinearLayout.getChildCount(); buttonIndex++) {
                //odredjuje se koji je button kliknut i on postaje iskljucen za ponovo kliktanje
                guessRowLinearLayout.getChildAt(buttonIndex).setEnabled(false);
            }
        }
    }


    //Kada se izvrsi bilo kakva promena u settings-u, Quiz se resratruje i krece ispocetka
    public void resetAnimalQuiz() {

        //Pristupanje assets folderu i njegovom sadrzaju
        AssetManager assets = getActivity().getAssets();
        allAnimalsNamesList.clear(); //brise ponudjene odgovore, kako prilikom resetovanja ne bi opet bili isti ponudjeni odgovori

        try {
/*odredjuje koja ce vrsta zivotinja biti u igri (Tame ili Wild, ili oba)*/
            for (String animalType : animalTypesInQuiz) {

                //Punimo String[] putanjama do podataka iz assets liste-foldera, u zavisnosti koji je 'animalType' odabran .....Tame_Animal ili WIld_Animal
                String[] animalImagePathsInQuiz = assets.list(animalType);

                //Iz animalImagePathsInQuiz[] izvlacimo putanju za svaku sliku pojedinacno
                for (String animalImagePathInQuiz : animalImagePathsInQuiz) {

                    //dodajemo putanje listi svih imena, i pritom prisemo extenziju '.png' kako bi kao odgovor dobili samo ime zivotinje
                    allAnimalsNamesList.add(animalImagePathInQuiz.replace(".png", ""));
                }
            }
        }

        catch(
        IOException e
        )

        {
            Log.e("AnimalQuiz", "Error", e);
        }

        //prilikom resetovanja quiza, postavljamo pocetne vrednosti
        numberOfRightAnswers = 0;
        numberOfAllGuesses = 0;
        animalsNamesQuizList.clear(); //resetovanje ponudjenih odgovora u jednoj igri

        //brojac pokusaja...tj. datih odgovora
        int counter = 1;
        //broj mogucih odgovora u jednoj igri izvuceno iz svih imena
        int numberOfAvaliableAnimals = allAnimalsNamesList.size();

        while (counter <= NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ){

            //random od 0 do numberOfAvaliableAnimals(broj sadrzanih slika-imena)...Tame,Wild ili oba foldera
            int randomIndex = secureRandomNumber.nextInt(numberOfAvaliableAnimals);

            //bira random jednu od slika i smesta je u promenljivu animalImageName
            String animalImageName = allAnimalsNamesList.get(randomIndex);

            //da svako sledece pitanje bude razlicito, tj. da ne dodje do ponavljanja istih slika u jednoj igri
            if (!animalsNamesQuizList.contains(animalImageName)){

                //dodajemo sliku koja je prosla u datu listu 'animalsNamesQuizList' kako bi sprecili njeno ponavljanje uslofom IF naredbe
                animalsNamesQuizList.add(animalImageName);
                ++counter;
            }
        }

        showNextAnimal();
    }



    //kreiranje animacije
    private void animateAnimalQuiz(boolean animateOutAnimalImage){

        //prilikom prvog pitanja ne postavljati nikakvu animaciju, vec samo prikazati sliku
        if (numberOfRightAnswers == 0){

            return;
        }

        //pocetne koordinate.....goreLevo
        int xTopLeft = 0;
        int yTopLeft = 0;

        //Nacin-pravac prikazivanja animacije prilikom zamene pitanja
        int xBottomRight = animalQuizLinearLayout.getLeft() + animalQuizLinearLayout.getRight();
        int yBottomRight = animalQuizLinearLayout.getTop() + animalQuizLinearLayout.getBottom();


        //Here is max value for radius
        //Radius pravimo kako bi animacija dobila zaobljeni oblik
        //Math.max vraca maximalne vretnosti koje sadrzi u sebi
        int radius = Math.max(animalQuizLinearLayout.getWidth(), animalQuizLinearLayout.getHeight());

        //Kreiramo promenljivu animator tipa ANimator
        Animator animator;

        //postavljam uslov, ako je animateOutAnimalImage (boolean argument metode) true,onda se izvrsava if naredba
        if (animateOutAnimalImage){

            //Kreiramo ovalnu animaciju na osnovu prosledjenih argumenata
            animator = ViewAnimationUtils.createCircularReveal(animalQuizLinearLayout, xBottomRight, yBottomRight, radius, 0);

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //kada prodje animacije, prikazati sledecu sliku-pitanje
                    showNextAnimal();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }else{

            //nacin prikaza druge animacije priliko zamene pitanja
            animator = ViewAnimationUtils.createCircularReveal(animalQuizLinearLayout, xTopLeft, yTopLeft, 0, radius);
        }

        animator.setDuration(700); //700 miliseconds izmedju animacija
        animator.start(); //pokretanje animacije
    }



    private void showNextAnimal(){

        //Kreiranje pormenljive nextAnimalImageName tipa String, znaci da preuzima vrednosi animalsNamesQuizList nako sto se obrise slika [0] iz niza nakon prelaska na sledecu sliku
        //zatim se na mesto index[0] postavlja sledeca slika-ime
        String nextAnimalImageName = animalsNamesQuizList.remove(0);
        correctAnimalAnswer = nextAnimalImageName; //sadrzi tacan odgovor
        txtAnswer.setText("");

        //Ispis teksta 'This is Animal %1$d of %2$d' , pri cemu je %1$d - (numberOfRightAnswers + 1) - inicijalno je 0, pa se dodaje +1 kako bi pocetna vrednost bila 1
        // , a %2$d - NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ
        txtQuestionNumber.setText(getString(R.string.question_text, (numberOfRightAnswers + 1), NUMBER_OF_ANIMAL_INCLUDED_IN_QUIZ));

        //animalType, Iz 'nextAnimalImageName' izvlacimo tip zivotinja na sledeci nacin (pocetniIndex 0, krajnjiIndex nextAnimalImageName.indexOf("-")).....npr Tame_Animals-Bird je Tame_animal
        String animalType = nextAnimalImageName.substring(0, nextAnimalImageName.indexOf("-"));

        //Pristupanje assets folderu i njegovom sadrzaju
        AssetManager assets = getActivity().getAssets();


        //otvaramo assets folder(odredjujemo tip zivotinja Tame ili Wild +"/"+ ime zivotinje iz datog tipa +".png") extenziju dodajemo zato sto
        //u liniji 337 nextAnimalImageName dobijamo samo ime bez ".png" (da bi mogli pravilno da ucitamo sliku)........i sve to smestamo u promenljivu
        //stream tipa InputStream
        try(InputStream stream = assets.open(animalType + "/" + nextAnimalImageName + ".png")){

            //Drawable.createFromStream....kreiranje slike iz promenljive 'stream' (stream, nextAnimalImageName-ime slike koje streba da se ucita)
            Drawable animalImage = Drawable.createFromStream(stream, nextAnimalImageName);
            //postavljamo ImaggeView preko promenljive imgAnimal sa vrednostima (animalImage)
            imgAnimal.setImageDrawable(animalImage);

            animateAnimalQuiz(false);

        }catch (IOException e){
            Log.e("AnimalQuiz", "There is an Error Getting" + nextAnimalImageName, e);
        }

        //nasumicno mesanje vrednosti koje sadrzi LISTA allAnimalsNamesList
        Collections.shuffle(allAnimalsNamesList);

        //Dodeljujemo index vrednosti correctAnimalNameIndex, na osnovu parametra correctAnimalAnswer iz liste allAnimalsNamesList
        int correctAnimalNameIndex = allAnimalsNamesList.indexOf(correctAnimalAnswer); //line 340
         //vrednost correctAnimalName dobijamo tako sto brisemo vrednost sa indexom correctAnimalNameIndex, iz liste allAnimalsNamesList
        String correctAnimalName = allAnimalsNamesList.remove(correctAnimalNameIndex);
        //dodajemo uklonjenu vrednost kao poslednji index liste allAnimalsNamesList
        allAnimalsNamesList.add(correctAnimalName);


        //Ova petlja postavlja odgovore u button-ima , ali ne obezbedjuje da jedan od odgovora bude tacan
        //broj redova LinearLayouta
        for (int row = 0; row < numberOfAnimalsGuessRows; row++){

            //getChildCount vraca broj button-a iz niza rowOfGuessButtonsInAnimalQuiz u zavisnosti od vrednosti promenljive row.
            //1 row, prestavlja jedan red LinearLayouta, i sadrzi 2 buttona sa indexima [0] i [1]
            for (int column = 0; column < rowOfGuessButtonsInAnimalQuiz[row].getChildCount(); column++){

                //row.getChildAt(column) - udji unutar redaLinearLayouta i odredi koji button(child)...button1[0], button2[1]
                //odrediti koji je button u zavisnosti u kom se redu nalazi (rowOfGuessButtonsInAnimalQuiz[row]) i koji je button u tom redu
                Button btnGuess = (Button)rowOfGuessButtonsInAnimalQuiz[row].getChildAt(column);
                //u trenutku kada se klikne na tacan button(odgovor), svi ostali button-i postaju 'nevidljivi' do sledeceg pitanja, nakon ceka ponovo svi postaju 'vidljivi'
                btnGuess.setEnabled(true);

                //allAnimalsNamesList.get((row * 2) + column) ...vraca INDEX buttona u kom se redu nalazi, u listi allAnimalsNamesList (na osnovu svakog prolaska kroz for petlju)
                String animalImageName = allAnimalsNamesList.get((row * 2) + column);
                //postavljamo text na button u zavisnosti od animalImageName nakon sto se obradi u metodi getTheExactAnimalName
                btnGuess.setText(getTheExactAnimalName(animalImageName)); //line 181
            }
        }

        //random postavlja rowLinearLayout-a, u zavisnosti od broja numberOfAnimalsGuessRows......koliko je podeseno u settings-u
        int row = secureRandomNumber.nextInt(numberOfAnimalsGuessRows);
        //postavlja random na jedno od 2 buttona koja se nalaze u datom redu LinearLayouta
        int column = secureRandomNumber.nextInt(2); // 0 ili 1..
        //Kreiramo promenljivu randomRow tipa LinearLayout i dodeljujemo vrednost rowOfGuessButtonsInAnimalQuiz sa elementima [row]
        LinearLayout randomRow = rowOfGuessButtonsInAnimalQuiz[row];
        //correctAnimalImageName sadrzi tacan odgovor, tako sto se dobije putem metode getTheExactAnimalName sa parametnom correctAnimalAnswer
        String correctAnimalImageName = getTheExactAnimalName(correctAnimalAnswer);
        //Postavljanje tacnog imena preko promenljive correctAnimalImageName, na jedan od buttona, koji se nalazi u nekom od redova LinearLayouta (randomRow0
        ((Button) randomRow.getChildAt(column)).setText(correctAnimalImageName);

    }




    //Motoda koja je zaduzena za rukovanje broja redova LinearLayouta (odgovora) 2, 4 ili 6
    //i poziva se svaki put kada se izvrsi neka promena u tom segmentu podesavanja

    public void modifyAnimalsGuessRows(SharedPreferences sharedPreferences){

        //parametar GUESSES, je KEY iz MainActivity, drugi parametar je defaultValuje, u ovom slucaju je null, jer u quiz_preference.xml je postavljena defValue na 4 (br ponudjenih odgovora)
        //u xml fajlu quiz_preference se kotisti ListPreference....pa se zbog toga ovde koristi SharedPreference
        final String NUMBER_OF_GUESS_OPTIONS = sharedPreferences.getString(MainActivity.GUESSES, null);
        //broj redova mora da bude ceo broj, pa zato parsiramo u INT (zato sto je u listPreference defValue upisana kao String)....i delimo sa 2, kako bi dobili koliko redova ce LinearLayouta ce biti prikazano
        //NPR. U settings-u se izabere 2. ... 2/2 = 1, sto znaci da ima jedan redLinearLayouta, odnosno numberOfAnimalsGuessRows
        numberOfAnimalsGuessRows = Integer.parseInt(NUMBER_OF_GUESS_OPTIONS) / 2;


        //Da bi se izvrsila promena ponudjenih odgovora, tj. redovaLinearLayouta, to se radi tako sto se brisu stara i postavljaju nova
        //za BRISANJE starih je zaduzena ova petlja
        for (LinearLayout horizontalLinearLayout : rowOfGuessButtonsInAnimalQuiz){

            horizontalLinearLayout.setVisibility(View.GONE);
        }

        //Ova petlja je zaduzena za POSTAVLJANJE novi redova LinearLayouta - ponudjenih odgovora
        for (int row = 0; row < numberOfAnimalsGuessRows; row++){

            rowOfGuessButtonsInAnimalQuiz[row].setVisibility(View.VISIBLE);
        }
    }




    //Metoda koja je zaduzena za izbor vrste zivotinja u igri.
    //Tame_Animas, Wild_Animals ili oba

    public void modifyTypeOfAnimalsInQuiz(SharedPreferences sharedPreferences){

        //getStringSet. metoda se poziva jer je animalTypesInQuiz deklarisana kao Set<String>
        //konstanta ANIMALS_TYPE je referenca na KEY u MultiSelectListPreference u quiz_preference.xml fajlu
        animalTypesInQuiz = sharedPreferences.getStringSet(MainActivity.ANIMALS_TYPE, null);
    }



    //Metoda koja je zaduzena za odabir FONT-a

    public void modifyQuizFont(SharedPreferences sharedPreferences){

        //Prosledjujemo prvi parametar QUIZ_FONT, koji je referenca na KEY iz ListPreference vezane za font....kako bi pristupili njenim podacima
        //preuzimamo fontove preko KEY i spestamo u promenljivu fontStringValue
        String fontStringValue = sharedPreferences.getString(MainActivity.QUIZ_FONT, null);


        //U zavisnosti od podesavanja, koji je FONT odabran za prikaz
        //Prolazi se kroz svaki ROW iz liste rowOfGuessButtonsInAnimalQuiz preko foreach petlje,
        //a zatim se preko unutrasnje FOR petlje prolazi kroz sadrzaj ROW-a, tj. button-i. i Dodeljuje svakom button-u taj font
        switch (fontStringValue){

            case "Chunkfive.otf":
                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz){

                    for (int column = 0; column < row.getChildCount(); column++){

                        Button button = (Button)row.getChildAt(column);
                        button.setTypeface(MainActivity.chunkfive);
                    }
                }
                break;

            case "FontleroyBrown.ttf":
                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz){

                    for (int column = 0; column < row.getChildCount(); column++){

                        Button button = (Button)row.getChildAt(column);
                        button.setTypeface(MainActivity.fontleroybrown);
                    }
                }
                break;

            case "Wonderbar Demo.otf":
                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz){

                    for (int column = 0; column < row.getChildCount(); column++){

                        Button button = (Button)row.getChildAt(column);
                        button.setTypeface(MainActivity.wonderbarDemo);
                    }
                }
                break;
        }
    }



    //Metoda za podesavanje backgroundColor

    public void modifyBackgroundColor(SharedPreferences sharedPreferences){

        String backgroundColor = sharedPreferences.getString(MainActivity.QUIZ_BACKGROUND_COLOR, null);

        switch (backgroundColor){

            case "White":

                //postavljanje boje backgroundColor
                animalQuizLinearLayout.setBackgroundColor(Color.WHITE);

                //izvlacenje broja redova LinearLayouta
                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz){
                    //izvlacenje button-a iz redova
                    for (int column = 0; column < row.getChildCount(); column++){
                        //kreiranje button-a u redu
                        Button button = (Button)row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE); //postavljanje boje button-a
                        button.setTextColor(Color.WHITE); //postavljanje boje texta na button-u
                    }
                }

                txtAnswer.setTextColor(Color.BLUE);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;

            case "Black":
                //postavljanje boje backgroundColor
                animalQuizLinearLayout.setBackgroundColor(Color.BLACK);

                //izvlacenje broja redova LinearLayouta
                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz){
                    //izvlacenje button-a iz redova
                    for (int column = 0; column < row.getChildCount(); column++){
                        //kreiranje button-a u redu
                        Button button = (Button)row.getChildAt(column);
                        button.setBackgroundColor(Color.YELLOW); //postavljanje boje button-a
                        button.setTextColor(Color.BLACK); //postavljanje boje texta na button-u
                    }
                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Green":

                animalQuizLinearLayout.setBackgroundColor(Color.GREEN);

                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);

                    }

                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.YELLOW);


                break;

            case "Blue":

                animalQuizLinearLayout.setBackgroundColor(Color.BLUE);

                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.RED);
                        button.setTextColor(Color.WHITE);

                    }

                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);

                break;

            case "Red":

                animalQuizLinearLayout.setBackgroundColor(Color.RED);

                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLUE);
                        button.setTextColor(Color.WHITE);

                    }

                }

                txtAnswer.setTextColor(Color.WHITE);
                txtQuestionNumber.setTextColor(Color.WHITE);


                break;

            case "Yellow":

                animalQuizLinearLayout.setBackgroundColor(Color.YELLOW);

                for (LinearLayout row : rowOfGuessButtonsInAnimalQuiz) {

                    for (int column = 0; column < row.getChildCount(); column++) {

                        Button button = (Button) row.getChildAt(column);
                        button.setBackgroundColor(Color.BLACK);
                        button.setTextColor(Color.WHITE);

                    }

                }

                txtAnswer.setTextColor(Color.BLACK);
                txtQuestionNumber.setTextColor(Color.BLACK);

                break;

        }
    }


}








