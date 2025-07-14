import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherApp extends JFrame {
    private JTextField cityField;
    private JTextField tempField;
    private JLabel conditionLabel;

    public WeatherApp() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(new JLabel("City:"));
        cityField = new JTextField(15);
        add(cityField);

        JButton fetchBtn = new JButton("Fetch");
        add(fetchBtn);

        add(new JLabel("Temp:"));
        tempField = new JTextField(15);
        tempField.setEditable(false);
        add(tempField);

        add(new JLabel("Condition:"));
        conditionLabel = new JLabel();
        add(conditionLabel);

        fetchBtn.addActionListener(e -> fetchWeather(cityField.getText().trim()));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void fetchWeather(String city) {
        if (city.isEmpty()) return;
        new Thread(() -> {
            try {
                String apiKey = "aff8d712f5876f77a8f28740454f53a5";
                String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="
                        + city + "&appid=" + apiKey;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String s;
                    while ((s = in.readLine()) != null) sb.append(s);
                    in.close();

                    String json = sb.toString();
                    String cond = parse(json, "\"main\":\"", "\"");
                    double kelvin = Double.parseDouble(parse(json, "\"temp\":", ","));
                    double celsius = kelvin - 273.15;

                    SwingUtilities.invokeLater(() -> {
                        tempField.setText(String.format("%.2f °C", celsius));
                        conditionLabel.setText(cond);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> tempField.setText("Error"));
                }
                conn.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private String parse(String src, String startDelim, String endDelim) {
        int s = src.indexOf(startDelim) + startDelim.length();
        int e = src.indexOf(endDelim, s);
        return src.substring(s, e);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::new);
    }
}
