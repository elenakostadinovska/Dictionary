// FavoriteTwitterSearches.java
// Stores Twitter search queries and tags for easily opening them
// in a browser.
package com.deitel.favoritetwittersearches;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import android.util.Log;
import java.util.Map;

// main (and only) Activity class for the Favorite Twitter Searches app
public class FavoriteTwitterSearches extends Activity {
    private SharedPreferences savedSearches; // user's favorite searches
    private TableLayout queryTableLayout; // shows the search buttons
    private EditText queryEditText; // where the user enters queries
    private EditText tagEditText; // where the user enters a query's tag
    // called when the activity is first created
    private EditText dictionarySearchEditText; // where the user enters dictionary searches
    private TextView dictionaryResultTextView;
    private HashMap<String, String> dictionary;
    private EditText englishWordEditText;
    private EditText macedonianTranslationEditText;
    private static final String TAG = "YourClassName";

    private boolean isDictionaryFileInInternalStorage() {
        File file = new File(getFilesDir(), "dictionary.txt");
        return file.exists();
    }

    private String findTranslation(String word) {
        // Retrieve the translation for the given word from the dictionary
        return dictionary.get(word);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // call the superclass version
        setContentView(R.layout.main); // set the layout

        // get the SharedPreferences that contains the user's saved searches
        savedSearches = getSharedPreferences("searches", MODE_PRIVATE);

        // get a reference to the queryTableLayout
        queryTableLayout =
                (TableLayout) findViewById(R.id.queryTableLayout);

        // get references to the two EditTexts and the Save Button
        queryEditText = (EditText) findViewById(R.id.queryEditText);
        tagEditText = (EditText) findViewById(R.id.tagEditText);
        dictionarySearchEditText = findViewById(R.id.dictionarySearchEditText);
        dictionaryResultTextView = findViewById(R.id.dictionaryResultTextView);
        englishWordEditText = findViewById(R.id.englishWordEditText);
        macedonianTranslationEditText = findViewById(R.id.macedonianTranslationEditText);

        // register listeners for the Save and Clear Tags Buttons
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);
        Button clearTagsButton =
                (Button) findViewById(R.id.clearTagsButton);
        clearTagsButton.setOnClickListener(clearTagsButtonListener);

        Button addWordButton = findViewById(R.id.addWordButton);

        addWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the English word and Macedonian translation entered by the user
                String englishWord = englishWordEditText.getText().toString().trim();
                String macedonianTranslation = macedonianTranslationEditText.getText().toString().trim();

                // Add the word to the dictionary file
                addWordToDictionary(englishWord, macedonianTranslation);

                // Optionally, you can clear the EditText fields after adding the word
                englishWordEditText.setText("");
                macedonianTranslationEditText.setText("");
            }
        });

        loadDictionaryData();

        Button dictionarySearchButton = findViewById(R.id.dictionarySearchButton);
        dictionarySearchButton.setOnClickListener(dictionarySearchButtonListener);

        refreshButtons(null); // add previously saved searches to GUI
    } //end method onCreate

    // recreate search tag and edit Buttons for all saved searches;
    // pass null to create all the tag and edit Buttons.
    private void refreshButtons(String newTag) {
    // store saved tags in the tags array
        String[] tags =
                savedSearches.getAll().keySet().toArray(new String[0]);
        Arrays.sort(tags, String.CASE_INSENSITIVE_ORDER); // sort by tag

    // if a new tag was added, insert in GUI at the appropriate location
        if (newTag != null) {
            makeTagGUI(newTag, Arrays.binarySearch(tags, newTag));
        } // end if
        else // display GUI for all tags
        {
    // display all saved searches
            for (int index = 0; index < tags.length; ++index)
                makeTagGUI(tags[index], index);
        } // end else
    } //end method refreshButtons

    // add new search to the save file, then refresh all Buttons
    private void makeTag(String query, String tag) {
    // originalQuery will be null if we're modifying an existing search
        String originalQuery = savedSearches.getString(tag, null);

    // get a SharedPreferences.Editor to store new tag/query pair
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // store current search
        preferencesEditor.apply(); // store the updated preferences

    // if this is a new query, add its GUI
        if (originalQuery == null)
            refreshButtons(tag); // adds a new button for this tag
    } //end method makeTag

    // add a new tag button and corresponding edit button to the GUI
    private void makeTagGUI(String tag, int index) {
    // get a reference to the LayoutInflater service
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

    // inflate new_tag_view.xml to create new tag and edit Buttons
        @SuppressLint("InflateParams") View newTagView = inflater.inflate(R.layout.new_tag_view, null);

    // get newTagButton, set its text and register its listener
        Button newTagButton =
                (Button) newTagView.findViewById(R.id.newTagButton);
        newTagButton.setText(tag);
        newTagButton.setOnClickListener(queryButtonListener);

    // get newEditButton and register its listener
        Button newEditButton =
                (Button) newTagView.findViewById(R.id.newEditButton);
        newEditButton.setOnClickListener(editButtonListener);

    // add new tag and edit buttons to queryTableLayout
        queryTableLayout.addView(newTagView, index);
    } //end makeTagGUI

    // remove all saved search Buttons from the app
    private void clearButtons() {
    // remove all saved search Buttons
        queryTableLayout.removeAllViews();
    } //end method clearButtons

    // create a new Button and add it to the ScrollView
    public OnClickListener saveButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
    // create tag if both queryEditText and tagEditText are not empty
            if (queryEditText.getText().length() > 0 &&
                    tagEditText.getText().length() > 0) {
                makeTag(queryEditText.getText().toString(),
                        tagEditText.getText().toString());
                queryEditText.setText(""); // clear queryEditText
                tagEditText.setText(""); // clear tagEditText

    // hide the soft keyboard
                ((InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tagEditText.getWindowToken(), 0);
            } // end if
            else // display message asking user to provide a query and a tag
            {
    // create a new AlertDialog Builder
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(FavoriteTwitterSearches.this);

                builder.setTitle(R.string.missingTitle); // title bar string

    // provide an OK button that simply dismisses the dialog
                builder.setPositiveButton(R.string.OK, null);

    // set the message to display
                builder.setMessage(R.string.missingMessage);

    // create AlertDialog from the AlertDialog.Builder
                AlertDialog errorDialog = builder.create();
                errorDialog.show(); // display the Dialog
            } //end else
        } //end method onClick
    }; //end OnClickListener anonymous inner class

    // clears all saved searches
    public OnClickListener clearTagsButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
    // create a new AlertDialog Builder
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(FavoriteTwitterSearches.this);

            builder.setTitle(R.string.confirmTitle); // title bar string

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.erase,
                    (dialog, button) -> {
                        clearButtons(); // clear all saved searches from the map

                        // get a SharedPreferences. Editor to clear searches
                        SharedPreferences.Editor preferencesEditor =
                                savedSearches.edit();

                        preferencesEditor.clear(); // remove all tag/query pairs
                        preferencesEditor.apply(); // commit the changes
                    } //end anonymous inner class
            ); // end call to method setPositiveButton

            builder.setCancelable(true);
            builder.setNegativeButton(R.string.cancel, null);

    // set the message to display
            builder.setMessage(R.string.confirmMessage);

    // create AlertDialog from the AlertDialog.Builder
            AlertDialog confirmDialog = builder.create();
            confirmDialog.show(); // display the Dialog
        } //end method onClick
    }; //end OnClickListener anonymous inner class

    //load selected search in a web browser
    public OnClickListener queryButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
    // get the query
            String buttonText = ((Button) v).getText().toString();
            String query = savedSearches.getString(buttonText, null);

    // create the URL corresponding to the touched Button's query
            String urlString = getString(R.string.searchURL) + query;

    // create an Intent to launch a web browser
            Intent getURL = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(urlString));

            startActivity(getURL); // execute the Intent

            String searchText = queryEditText.getText().toString().trim();
            String translation = findTranslation(searchText); // Find translation from text document
            displayTranslation(translation); // Display the translation
        } //end method onClick
    }; //end OnClickListener anonymous inner class

    // edit selected search
    public OnClickListener editButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
    // get all necessary GUI components
            TableRow buttonTableRow = (TableRow) v.getParent();
            Button searchButton =
                    (Button) buttonTableRow.findViewById(R.id.newTagButton);

            String tag = searchButton.getText().toString();

    // set EditTexts to match the chosen tag and query
            tagEditText.setText(tag);
            queryEditText.setText(savedSearches.getString(tag, null));
        } //end method onClick
    }; //end OnClickListener anonymous inner class

    private OnClickListener dictionarySearchButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String searchWord = dictionarySearchEditText.getText().toString();
            String translation = findTranslation(searchWord, true); // English to Macedonian
            if (translation == null) {
                translation = findTranslation(searchWord, false); // Macedonian to English
            }
            displayTranslation(translation);
        }
    };

    private void loadDictionaryData() {
        try {
            // Open the text file from internal storage
            FileInputStream fileInputStream = openFileInput("dictionary.txt");
            Log.d(TAG, "Loading dictionary from internal storage...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            dictionary = new HashMap<>();

            String line;
            int lineNumber = 0;
            // Read each line from the file
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                Log.d(TAG, "Reading line " + lineNumber + ": " + line);

                // Split the line into English word and Macedonian translation
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String englishWord = parts[0].trim();
                    String macedonianTranslation = parts[1].trim();
                    Log.d(TAG, "Adding word to dictionary: " + englishWord + " - " + macedonianTranslation);
                    dictionary.put(englishWord, macedonianTranslation);
                } else {
                    Log.e(TAG, "Invalid line format at line " + lineNumber + ": " + line);
                }
            }

            // Close the reader
            reader.close();
            Log.d(TAG, "Dictionary loaded successfully: " + dictionary.size() + " words.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading dictionary: " + e.getMessage());
        }
    }

    private void addWordToDictionary(String englishWord, String macedonianTranslation) {
        try {
            // Open the file for writing in append mode
            FileOutputStream fileOutputStream = openFileOutput("dictionary.txt", Context.MODE_APPEND);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            // Write the new word to the file
            String newEntry = englishWord + "," + macedonianTranslation;
            writer.write(newEntry);
            writer.newLine();

            // Close the writer
            writer.close();

            // Optionally, you can update the dictionary HashMap in memory as well
            dictionary.put(englishWord, macedonianTranslation);

            // Notify the user that the word has been added
            Toast.makeText(getApplicationContext(), "Word added to dictionary", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error adding word to dictionary: " + e.getMessage());
            // Handle error
        }
    }

    // Method to find the translation of a word from the text document
    private String findTranslation(String word, boolean isMacedonianToEnglish) {
        if (isMacedonianToEnglish) {
            // Search for the translation of the given Macedonian word in the values of the dictionary
            for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(word)) {
                    // Return the corresponding English word if found
                    return entry.getKey();
                }
            }
        } else {
            // Search for the translation of the given English word in the dictionary
            return dictionary.get(word);
        }
        // Return null if the translation is not found
        return null;
    }

    // Method to display the translation in the UI
    private void displayTranslation(String translation) {
        if (translation != null) {
            dictionaryResultTextView.setText(translation);
        } else {
            dictionaryResultTextView.setText("Translation not found");
        }
    }

    private void addWordToDictionaryFile(String englishWord, String macedonianTranslation) {
        try {
            FileOutputStream fos = openFileOutput("dictionary.txt", Context.MODE_APPEND);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
            outputStreamWriter.write(englishWord + ", " + macedonianTranslation + "\n");
            outputStreamWriter.close();
            Toast.makeText(getApplicationContext(), "Word added to dictionary", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

} // end class FavoriteTwitterSearches