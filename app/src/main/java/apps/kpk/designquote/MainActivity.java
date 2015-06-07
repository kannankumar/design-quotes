package apps.kpk.designquote;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends Activity {
    String[] quotesList = {
            "Well established hierarchies are not easily uprooted",
            "A computer without Photoshop is like a dog with no legs. Sure is fun, but you can&#8217;t really do anything with it. ",
            "If there is one word I&#8217;d like to remove from any conversation about design, it&#8217;s &#8220;pretty.&#8221;",
            "There&#8217;s a point when you’re done simplifying. Otherwise, things get really complicated. ",
            "Web design is art wrapped in technology.",
            "The best and most beautiful things in the world cannot be seen or even touched. They must be felt with the heart.",
            "A customer is the most important visitor on our premises.<br /> He is not dependent on us. We are dependent on him.<br /> He is not an interruption of our work. He is the purpose of it.<br /> He is not an outsider to our business. He is part of it.<br /> We are not doing him a favour by serving him.<br /> He is doing us a favour by giving us the opportunity to do so.",
            "A work of art is one of mystery, the one extreme magic; everything else is either arithmetic or biology."};
    String[] authorList = {
            "Tao Te Ching",
            "Benjamin Cavanagh",
            "Aarron Walter",
            "Frank Chimero",
            "James Weaver",
            "Helen Keller",
            "Mahatma Gandhi",
            "Truman Capote"
    };

    String curr_quote;
    String curr_author;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home_v2);

        // Google AdMobs
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        final TextView quote_tv = (TextView) findViewById(R.id.quote_tv);
        final TextView author_tv = (TextView) findViewById(R.id.author_tv);

        Random random = new Random(); // or create a static random field...
        int randIndex = random.nextInt(quotesList.length);
        String randQuote = quotesList[randIndex];
        String randAuthor = "\u2014 " + authorList[randIndex];

        randQuote = Html.fromHtml((String) randQuote).toString(); //Convert HTML special characters to string

        quote_tv.setText(randQuote);
        author_tv.setText(randAuthor);
        curr_quote = randQuote;
        curr_author = randAuthor;
        ImageButton next_btn = (ImageButton) findViewById(R.id.imageButton2);
        next_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                FetchQuoteTask task = new FetchQuoteTask();
                task.execute();
            }

        });

        ImageButton send_btn = (ImageButton) findViewById(R.id.sendButton);
        send_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                String share_str = "\""+curr_quote+"\" "+curr_author+"\n(sent from DesignQuote - Android app)";

               //Working snippet for Sharing quote text
               Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                share.putExtra(Intent.EXTRA_TEXT,share_str );
                startActivity(Intent.createChooser(share, "Share this via"));
            }

        });
    }

    public class FetchQuoteTask extends AsyncTask<Void, Void, String> {

        private final String LOG_TAG = FetchQuoteTask.class.getSimpleName();
        final Dialog dialog = new Dialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_loading);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String quoteJsonStr = null;

            try {
                URL url = new URL("http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1");

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return "null";
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.i(LOG_TAG, line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return "null";
                }
                quoteJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return "null";
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return quoteJsonStr;
        }

        protected void onPostExecute(String result) {
            int i, resId;
            String resAuthor="",resQuote="";

            //parse JSON data
            try {
                JSONArray jArray = new JSONArray(result);
                for(i=0; i < jArray.length(); i++) {

                    JSONObject jObject = jArray.getJSONObject(i);

                    resAuthor = jObject.getString("title");
                    resQuote = jObject.getString("content");
                    resId = jObject.getInt("ID");

                } // End Loop
            } catch (JSONException e) {
                Log.e("JSONException", "Error: " + e.toString());
            } // catch (JSONException e)



            final TextView quote_tv = (TextView) findViewById(R.id.quote_tv);
            final TextView author_tv = (TextView) findViewById(R.id.author_tv);
            resQuote = Html.fromHtml((String) resQuote).toString();   //Convert HTML special characters to string
            resQuote = resQuote.replace("\n", "").replace("\r", "");  //Remove line breaks

            resAuthor = Html.fromHtml((String) resAuthor).toString();
            resAuthor = "\u2014 " + resAuthor;
            quote_tv.setText(resQuote);
            author_tv.setText(resAuthor);

            curr_quote = resQuote;
            curr_author = resAuthor;
            dialog.dismiss();
        }
    }

}
